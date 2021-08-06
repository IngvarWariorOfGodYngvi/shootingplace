package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.*;
import com.shootingplace.shootingplace.domain.enums.ArbiterClass;
import com.shootingplace.shootingplace.domain.enums.ErasedType;
import com.shootingplace.shootingplace.domain.models.History;
import com.shootingplace.shootingplace.domain.models.Member;
import com.shootingplace.shootingplace.domain.models.MemberDTO;
import com.shootingplace.shootingplace.repositories.*;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final LicenseRepository licenseRepository;
    private final ShootingPatentRepository shootingPatentRepository;
    private final ContributionService contributionService;
    private final HistoryService historyService;
    private final WeaponPermissionRepository weaponPermissionRepository;
    private final MemberPermissionsRepository memberPermissionsRepository;
    private final PersonalEvidenceRepository personalEvidenceRepository;
    private final ClubRepository clubRepository;
    private final ChangeHistoryService changeHistoryService;
    private final ErasedRepository erasedRepository;
    private final ContributionRepository contributionRepository;
    private final Logger LOG = LogManager.getLogger();


    public MemberService(MemberRepository memberRepository,
                         AddressRepository addressRepository,
                         LicenseRepository licenseRepository,
                         ShootingPatentRepository shootingPatentRepository,
                         ContributionService contributionService,
                         HistoryService historyService,
                         WeaponPermissionRepository weaponPermissionRepository,
                         MemberPermissionsRepository memberPermissionsRepository,
                         PersonalEvidenceRepository personalEvidenceRepository,
                         ClubRepository clubRepository,
                         ChangeHistoryService changeHistoryService,
                         ErasedRepository erasedRepository,
                         ContributionRepository contributionRepository) {
        this.memberRepository = memberRepository;
        this.addressRepository = addressRepository;
        this.licenseRepository = licenseRepository;
        this.shootingPatentRepository = shootingPatentRepository;
        this.contributionService = contributionService;
        this.historyService = historyService;
        this.weaponPermissionRepository = weaponPermissionRepository;
        this.memberPermissionsRepository = memberPermissionsRepository;
        this.personalEvidenceRepository = personalEvidenceRepository;
        this.clubRepository = clubRepository;
        this.changeHistoryService = changeHistoryService;
        this.erasedRepository = erasedRepository;
        this.contributionRepository = contributionRepository;
    }


    //--------------------------------------------------------------------------
//    public List<MemberEntity> getMembersList(Boolean active, Boolean adult, Boolean erase) {
//        checkMembers();
//
//
//        List<MemberEntity> list = new ArrayList<>(memberRepository.findAllByActiveAndAdultAndErased(active, adult, erase));
//        String c = "aktywnych";
//        if (!active) {
//            c = "nieaktywnych";
//        }
//        LOG.info("wyświetlono listę osób " + c);
//        LOG.info("ilość osób " + c + " : " + list.size());
//        list.sort(Comparator.comparing(MemberEntity::getSecondName));
//        return list;
//    }

    public List<Member> getMembersWithPermissions() {
        List<Member> list = new ArrayList<>();

        memberRepository.findAll().stream().filter(f -> f.getMemberPermissions() != null).forEach(e -> {
            if (
                    (e.getMemberPermissions().getShootingLeaderNumber() != null)

                            || (e.getMemberPermissions().getArbiterNumber() != null)

                            || (e.getMemberPermissions().getInstructorNumber() != null)) {
                list.add(Mapping.map(e));
            }
        });
        list.sort(Comparator.comparing(Member::getSecondName));
        LOG.info("Wywołano listę osób z uprawnieniami");
        return list;
    }

    public List<String> getArbiters() {
        List<String> list = new ArrayList<>();
        memberRepository.findAll().stream()
                .filter(e -> e.getMemberPermissions() != null)
                .filter(e -> e.getMemberPermissions().getArbiterNumber() != null)
                .forEach(e -> list.add(e.getSecondName().concat(" " + e.getFirstName() + " " + e.getMemberPermissions().getArbiterClass() + " leg. " + e.getLegitimationNumber())));
        list.sort(Comparator.comparing(String::new));
        return list;
    }

//    public List<String> getMembersNameAndLegitimationNumber(Boolean active, Boolean adult, Boolean erase) {
//        checkMembers();
//
//        List<String> list = new ArrayList<>();
//        memberRepository.findAllByActiveAndAdultAndErased(active, adult, erase)
//                .forEach(e ->
//                        list.add(e.getSecondName().concat(" " + e.getFirstName() + " leg. " + e.getLegitimationNumber())));
//        list.sort(Comparator.comparing(String::new));
//        LOG.info("Lista nazwisk z identyfikatorem");
//        return list;
//    }

    public void checkMembers() {
        // dorośli
        List<MemberEntity> adultMembers = memberRepository
                .findAll()
                .stream()
                .filter(MemberEntity::getAdult)
                .filter(MemberEntity::getActive)
                .collect(Collectors.toList());
        // nie ma żadnych składek
        adultMembers.forEach(e -> {
            if (e.getHistory().getContributionList().isEmpty() || e.getHistory().getContributionList() == null) {
                e.setActive(false);
                memberRepository.saveAndFlush(e);
            }
            //dzisiejsza data jest później niż składka + 3 miesiące
            else {
                if (e.getHistory().getContributionList().get(0).getValidThru().plusMonths(3).isBefore(LocalDate.now())) {
                    e.setActive(false);
                    LOG.info("zmieniono " + e.getSecondName());
                    memberRepository.saveAndFlush(e);

                } else {
                    e.setActive(true);
                }
            }
            if (e.getLicense().getNumber() != null) {
                LicenseEntity license = e.getLicense();
                license.setValid(!e.getLicense().getValidThru().isBefore(LocalDate.now()));
                licenseRepository.saveAndFlush(license);
            }
        });
        //młodzież
        List<MemberEntity> nonAdultMembers = memberRepository.findAll().stream().filter(f -> !f.getAdult()).filter(MemberEntity::getActive).collect(Collectors.toList());
        // nie ma żadnych składek
        nonAdultMembers.forEach(e -> {
            if (e.getHistory().getContributionList().isEmpty() || e.getHistory().getContributionList() == null) {
                e.setActive(false);
                memberRepository.saveAndFlush(e);
            } else {
                //dzisiejsza data jest później niż składka + 1 || 2 miesiące
                LocalDate validThru = e.getHistory().getContributionList().get(0).getValidThru();
                if ((validThru.equals(LocalDate.of(validThru.getYear(), 2, 28)) && validThru.plusMonths(1).isBefore(LocalDate.now()))
                        || (validThru.equals(LocalDate.of(validThru.getYear(), 8, 31)) && validThru.plusMonths(2).isBefore(LocalDate.now()))) {
                    e.setActive(false);
                    LOG.info("zmieniono " + e.getSecondName());
                    memberRepository.saveAndFlush(e);

                }
            }
            if (e.getLicense().getNumber() != null) {
                LicenseEntity license = e.getLicense();
                license.setValid(!e.getLicense().getValidThru().isBefore(LocalDate.now()));
                licenseRepository.saveAndFlush(license);
            }
        });
    }

    //--------------------------------------------------------------------------
    public ResponseEntity<?> addNewMember(Member member, String pinCode) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setActive(true);

        List<MemberEntity> memberEntityList = memberRepository.findAll();
        if (memberEntityList.stream().filter(f -> !f.getErased()).anyMatch(a -> a.getPesel().equals(member.getPesel()))) {
            LOG.error("Ktoś już ma taki numer PESEL");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Ktoś już ma taki numer PESEL\"");
        }
        if (member.getEmail() == null || member.getEmail().isEmpty()) {
            memberEntity.setEmail("");
        }
        memberEntity.setEmail(member.getEmail().toLowerCase());
        if (memberEntityList.stream().filter(f -> !f.getErased()).filter(f -> f.getEmail().equals(" ") || f.getEmail() == null).anyMatch(e -> e.getEmail().equals(member.getEmail()))) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Ktoś już ma taki e-mail\"" + member.getEmail());
        }
        if (memberEntityList.stream().anyMatch(e -> e.getLegitimationNumber().equals(member.getLegitimationNumber()))) {
            if (memberEntityList.stream().filter(MemberEntity::getErased).anyMatch(e -> e.getLegitimationNumber().equals(member.getLegitimationNumber()))) {
                LOG.error("Ktoś już ma taki numer legitymacji");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Ktoś wśród skreślonych już ma taki numer legitymacji\"");
            } else {
                LOG.error("Ktoś już ma taki numer legitymacji");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Ktoś już ma taki numer legitymacji\"");
            }
        }

        if (memberEntityList.stream().filter(f -> !f.getErased()).anyMatch(e -> e.getIDCard().trim().toUpperCase().equals(member.getIDCard()))) {
            LOG.error("Ktoś już ma taki numer dowodu osobistego");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Ktoś już ma taki numer dowodu osobistego\"");
        } else {
            if (member.getJoinDate() == null) {
                memberEntity.setJoinDate(LocalDate.now());
                LOG.info("ustawiono domyślną datę zapisu " + memberEntity.getJoinDate());
            } else {
                memberEntity.setJoinDate(member.getJoinDate());
                LOG.info("ustawiono datę zapisu na w" + memberEntity.getJoinDate());
            }
            if (member.getLegitimationNumber() == null) {
                int number;
                if (memberEntityList.isEmpty()) {
                    memberEntity.setLegitimationNumber(1);
                    number = 1;
                } else {
                    memberEntityList.sort(Comparator.comparing(MemberEntity::getLegitimationNumber));
                    Collections.reverse(memberEntityList);
                    number = memberEntityList.get(0).getLegitimationNumber() + 1;
                }
                if (memberEntityList.stream().anyMatch(e -> e.getLegitimationNumber().equals(member.getLegitimationNumber()))) {
                    LOG.error("Ktoś już ma taki numer legitymacji");
                    return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Ktoś już ma taki numer legitymacji\"");
                } else {
                    memberEntity.setLegitimationNumber(number);
                    LOG.info("ustawiono domyślny numer legitymacji : " + memberEntity.getLegitimationNumber());
                }
            } else {
                memberEntity.setLegitimationNumber(member.getLegitimationNumber());
            }
            String s = "+48";
            if (member.getPhoneNumber() != null) {
                memberEntity.setPhoneNumber(s + member.getPhoneNumber().replaceAll("\\s", ""));
            }
            if (!member.getAdult()) {
                LOG.info("Klubowicz należy do młodzieży");
                memberEntity.setAdult(false);
            } else {
                LOG.info("Klubowicz należy do grupy dorosłej");
            }
            String[] s1 = member.getFirstName().split(" ");
            StringBuilder firstNames = new StringBuilder();
            for (String value : s1) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                firstNames.append(splinted);
            }
            memberEntity.setFirstName(firstNames.toString());
            memberEntity.setSecondName(member.getSecondName().toUpperCase());
            LOG.info("Dodano nowego członka Klubu " + member.getFirstName());
            AddressEntity address = AddressEntity.builder()
                    .zipCode(null)
                    .postOfficeCity(null)
                    .street(null)
                    .streetNumber(null)
                    .flatNumber(null)
                    .build();
            addressRepository.save(address);
            memberEntity.setAddress(address);
            LicenseEntity license = LicenseEntity.builder()
                    .number(null)
                    .validThru(null)
                    .pistolPermission(false)
                    .riflePermission(false)
                    .shotgunPermission(false)
                    .valid(false)
                    .canProlong(false)
                    .paid(false)
                    .build();
            licenseRepository.save(license);
            ShootingPatentEntity shootingPatent = ShootingPatentEntity.builder()
                    .patentNumber(null)
                    .dateOfPosting(null)
                    .pistolPermission(false)
                    .riflePermission(false)
                    .shotgunPermission(false)
                    .build();
            shootingPatentRepository.save(shootingPatent);

            History history = History.builder()
                    .licenseHistory(new String[]{})
                    .patentDay(new LocalDate[3])
                    .licensePaymentHistory(null)
                    .patentFirstRecord(false).build();
            HistoryEntity historyEntity = historyService.createHistory(history);


            WeaponPermissionEntity weaponPermission = WeaponPermissionEntity.builder()
                    .number(null)
                    .isExist(false)
                    .build();
            weaponPermissionRepository.save(weaponPermission);

            MemberPermissionsEntity memberPermissions = MemberPermissionsEntity.builder()
                    .instructorNumber(null)
                    .shootingLeaderNumber(null)
                    .arbiterClass(ArbiterClass.NONE.getName())
                    .arbiterNumber(null)
                    .arbiterPermissionValidThru(null)
                    .build();
            memberPermissionsRepository.save(memberPermissions);
            PersonalEvidenceEntity personalEvidence = PersonalEvidenceEntity.builder()
                    .ammoList(new ArrayList<>())
                    .build();
            personalEvidenceRepository.save(personalEvidence);

            memberEntity.setIDCard(member.getIDCard().trim().toUpperCase());
            memberEntity.setPesel(member.getPesel());
            memberEntity.setClub(clubRepository.findById(1).orElseThrow(EntityNotFoundException::new));
            memberEntity.setShootingPatent(shootingPatent);
            memberEntity.setLicense(license);
            memberEntity.setHistory(historyEntity);
            memberEntity.setWeaponPermission(weaponPermission);
            memberEntity.setMemberPermissions(memberPermissions);
            memberEntity.setPersonalEvidence(personalEvidence);
            memberEntity.setPzss(false);
            memberEntity.setErasedEntity(null);
            memberEntity = memberRepository.save(memberEntity);
            ContributionEntity contributionEntity = contributionService.addFirstContribution(memberEntity.getUuid(), LocalDate.now());
            historyService.addContribution(memberEntity.getUuid(), contributionEntity);

        }
        changeHistoryService.addRecordToChangeHistory(pinCode, memberEntity.getClass().getSimpleName() + " addNewMember", memberEntity.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("\"" + memberEntity.getUuid() + "\"");


    }


    //--------------------------------------------------------------------------
    // @Patch
    public ResponseEntity<?> activateOrDeactivateMember(String memberUUID, String pinCode) {
        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.notFound().build();
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        memberEntity.toggleActive();
        memberRepository.saveAndFlush(memberEntity);
        LOG.info("Zmieniono status");
        changeHistoryService.addRecordToChangeHistory(pinCode, memberEntity.getClass().getSimpleName() + " activateOrDeactivateMember", memberEntity.getUuid());
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> changeAdult(String memberUUID, String pinCode) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.notFound().build();
        }
        if (memberEntity.getAdult()) {
            LOG.info("Klubowicz należy już do grupy powszechnej");
            return ResponseEntity.badRequest().build();
        }
        if (!LocalDate.now().equals(memberEntity.getJoinDate().plusYears(1))) {
            LOG.info("Klubowicz ma za krótki staż jako młodzież");
            return ResponseEntity.badRequest().body("Klubowicz ma za krótki staż jako młodzież");
        }
        memberEntity.setAdult(true);
        memberRepository.saveAndFlush(memberEntity);
        LocalDate date;
        if (memberEntity.getHistory().getContributionList().get(0).getValidThru().getDayOfMonth() == 28) {
            date = LocalDate.of(LocalDate.now().getYear(), 6, 30);
        } else {
            date = memberEntity.getHistory().getContributionList().get(0).getValidThru().plusMonths(4);
        }
        ContributionEntity contribution = ContributionEntity.builder()
                .paymentDay(LocalDate.now())
                .validThru(date)
                .historyUUID(memberEntity.getHistory().getUuid())
                .build();
        contributionRepository.saveAndFlush(contribution);
        historyService.addContribution(memberUUID, contribution);
        LOG.info("Klubowicz należy od teraz do grupy dorosłej : " + LocalDate.now());
        changeHistoryService.addRecordToChangeHistory(pinCode, memberEntity.getClass().getSimpleName() + " changeAdult", memberEntity.getUuid());
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<?> eraseMember(String memberUUID, String erasedType, LocalDate erasedDate, String additionalDescription, String pinCode) {
        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.notFound().build();
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        if (!memberEntity.getErased()) {
            ErasedEntity build = ErasedEntity.builder()
                    .erasedType(erasedType)
                    .date(erasedDate)
                    .additionalDescription(additionalDescription)
                    .build();
            erasedRepository.save(build);
            memberEntity.setErasedEntity(build);
            memberEntity.toggleErase();
            memberEntity.setPzss(false);
            LOG.info("Klubowicz skreślony : " + LocalDate.now());
        }
        memberRepository.saveAndFlush(memberEntity);
        changeHistoryService.addRecordToChangeHistory(pinCode, memberEntity.getClass().getSimpleName() + " eraseMember", memberEntity.getUuid());
        return ResponseEntity.noContent().build();
    }

    //--------------------------------------------------------------------------
    @SneakyThrows
    public ResponseEntity<?> updateMember(String memberUUID, Member member) {

        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.notFound().build();
        }

        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

        if (member.getFirstName() != null && !member.getFirstName().isEmpty()) {
            String[] s1 = member.getFirstName().split(" ");
            StringBuilder firstNames = new StringBuilder();
            for (String value : s1) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                firstNames.append(splinted);
            }
            memberEntity.setFirstName(firstNames.toString());
            LOG.info(goodMessage() + "Imię");
        }
        if (member.getSecondName() != null && !member.getSecondName().isEmpty()) {
            memberEntity.setSecondName(member.getSecondName().toUpperCase());
            LOG.info(goodMessage() + "Nazwisko");

        }
        if (member.getJoinDate() != null) {
            memberEntity.setJoinDate(member.getJoinDate());
            LOG.info(goodMessage() + "Data przystąpienia do klubu");
        }
        if (member.getLegitimationNumber() != null) {
            if (memberRepository.findByLegitimationNumber(member.getLegitimationNumber()).isPresent()) {
                LOG.warn("Już ktoś ma taki numer legitymacji");
            } else {
                memberEntity.setLegitimationNumber(member.getLegitimationNumber());
                LOG.info(goodMessage() + "numer legitymacji");
            }
        }
        if (member.getEmail() != null && !member.getEmail().isEmpty()) {
            if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
                LOG.error("Już ktoś ma taki sam e-mail");
            } else {
                memberEntity.setEmail(member.getEmail().trim().toLowerCase());
                LOG.info(goodMessage() + "Email");
            }
        }
        if (member.getPesel() != null && !member.getPesel().isEmpty()) {
            if (memberRepository.findByPesel(member.getPesel()).isPresent()) {
                LOG.error("Już ktoś ma taki sam numer PESEL");
            } else {
                memberEntity.setPesel(member.getPesel());
                LOG.info(goodMessage() + "Numer PESEL");
            }
        }
        if (member.getPhoneNumber() != null && !member.getPhoneNumber().isEmpty()) {
            if (member.getPhoneNumber().replaceAll("\\s-", "").length() != 9 && !member.getPhoneNumber().isEmpty()) {
                LOG.error("Żle podany numer");
            }
            String s = "+48";
            memberEntity.setPhoneNumber((s + member.getPhoneNumber()).replaceAll("\\s", ""));
            if (memberRepository.findByPhoneNumber((s + member.getPhoneNumber()).replaceAll("\\s", "")).isPresent()) {
                LOG.error("Ktoś już ma taki numer telefonu");
            }
            if (member.getPhoneNumber().equals(memberEntity.getPhoneNumber())) {
                memberEntity.setPhoneNumber(member.getPhoneNumber());
                LOG.info(goodMessage() + "Numer Telefonu");
            }
        }
        if (member.getIDCard() != null && !member.getIDCard().isEmpty()) {
            if (memberRepository.findByIDCard(member.getIDCard().trim()).isPresent()) {
                LOG.error("Ktoś już ma taki numer dowodu");
            } else {
                memberEntity.setIDCard(member.getIDCard().trim().toUpperCase());
                LOG.info(goodMessage() + "Numer Dowodu");
            }
        }

        memberRepository.saveAndFlush(memberEntity);

        return ResponseEntity.ok().build();
    }

    private String goodMessage() {
        return "Zaktualizowano pomyślnie : ";
    }


    public MemberEntity getMember(int number) {
        LOG.info("Wywołano Klubowicza");
        MemberEntity memberEntity = memberRepository.findAll()
                .stream()
                .filter(f -> f.getLegitimationNumber().equals(number))
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);

        return memberEntity;

    }

    public List<MemberEntity> getErasedMembers() {
        LOG.info("Wyświetlono osoby skreślone z listy członków");
        return memberRepository.findAllByErasedIsTrue();
    }

    public List<String> getMembersEmails(Boolean condition) {
        List<String> list = new ArrayList<>();
        List<MemberEntity> all = memberRepository.findAll();
        all.sort(Comparator.comparing(MemberEntity::getSecondName).thenComparing(MemberEntity::getFirstName));
        all.forEach(e -> {
            if ((e.getEmail() != null && !e.getEmail().isEmpty()) && !e.getErased() && e.getAdult() == condition) {
                list.add(e.getEmail().concat(";"));
            }
        });
        return list;
    }

    public List<String> getMembersEmailsAdultActiveWithNoPatent() {
        List<String> list = new ArrayList<>();
        List<MemberEntity> all = memberRepository.findAll();
        all.sort(Comparator.comparing(MemberEntity::getSecondName).thenComparing(MemberEntity::getFirstName));
        all.stream()
                .filter(f->!f.getErased())
                .filter(MemberEntity::getAdult)
                .filter(MemberEntity::getActive)
                .filter(f->f.getShootingPatent()!=null)
                .filter(f -> f.getShootingPatent().getPatentNumber() == null || f.getShootingPatent().getPatentNumber().isEmpty())
                .forEach(e -> {
            if ((e.getEmail() != null && !e.getEmail().isEmpty())) {
                list.add(e.getEmail().concat(";"));
            }
        });
        return list;
    }

    public List<String> getMembersPhoneNumbersWithNoPatent() {
        List<String> list = new ArrayList<>();
        List<MemberEntity> all = memberRepository.findAll();
        all.sort(Comparator.comparing(MemberEntity::getSecondName).thenComparing(MemberEntity::getFirstName));
        all.stream()
                .filter(f->!f.getErased())
                .filter(MemberEntity::getAdult)
                .filter(MemberEntity::getActive)
                .filter(f -> f.getShootingPatent().getPatentNumber() == null || f.getShootingPatent().getPatentNumber().isEmpty())
                .forEach(e -> {
            if ((e.getPhoneNumber() != null && !e.getPhoneNumber().isEmpty())) {
                String phone = e.getPhoneNumber();
                String split = phone.substring(0, 3) + " ";
                String split1 = phone.substring(3, 6) + " ";
                String split2 = phone.substring(6, 9) + " ";
                String split3 = phone.substring(9, 12) + " ";
                String phoneSplit = split + split1 + split2 + split3;
                list.add(phoneSplit.concat(" " + e.getSecondName() + " " + e.getFirstName() + ";"));
            }
        });
        return list;
    }

    public List<String> getMembersPhoneNumbers(Boolean condition) {
        List<String> list = new ArrayList<>();
        List<MemberEntity> all = memberRepository.findAll();
        all.sort(Comparator.comparing(MemberEntity::getSecondName).thenComparing(MemberEntity::getFirstName));
        all.forEach(e -> {
            if ((e.getPhoneNumber() != null && !e.getPhoneNumber().isEmpty()) && !e.getErased() && e.getAdult() == condition) {
                String phone = e.getPhoneNumber();
                String split = phone.substring(0, 3) + " ";
                String split1 = phone.substring(3, 6) + " ";
                String split2 = phone.substring(6, 9) + " ";
                String split3 = phone.substring(9, 12) + " ";
                String phoneSplit = split + split1 + split2 + split3;
                list.add(phoneSplit.concat(" " + e.getSecondName() + " " + e.getFirstName() + ";"));
            }
        });
        return list;
    }


    public ResponseEntity<?> updateJoinDate(String memberUUID, String date) {

        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.notFound().build();
        }
        MemberEntity toUpdate = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        LocalDate newDate = LocalDate.parse(date);
        toUpdate.setJoinDate(newDate);
        memberRepository.saveAndFlush(toUpdate);
        LOG.info(goodMessage() + "Data przystąpienia do klubu");
        return ResponseEntity.ok().build();

    }

    public List<String> getMembersWithLicense(Boolean license) {

        List<String> list = new ArrayList<>();
        if (license) {
            memberRepository.findAll()
                    .stream()
                    .filter(e -> e.getLicense().getNumber() != null && e.getLicense().isValid())
                    .forEach(e -> list.add(e.getFirstName() + " " + e.getSecondName() + " " + e.getLicense().getNumber()));
        } else {
            memberRepository.findAll()
                    .stream()
                    .filter(e -> e.getLicense().getNumber() != null && !e.getLicense().isValid())
                    .forEach(e -> list.add(e.getFirstName() + " " + e.getSecondName() + " " + e.getLicense().getNumber()));
        }
        return list;
    }

//    public List<String> getAllActiveMembersNames() {
//        checkMembers();
//
//        List<String> list = new ArrayList<>();
//        memberRepository.findAll().stream().filter(MemberEntity::getActive)
//                .forEach(e ->
//                        list.add(e.getSecondName().concat(" " + e.getFirstName() + " leg. " + e.getLegitimationNumber())));
//        list.sort(Comparator.comparing(String::new));
//        LOG.info("Lista nazwisk z identyfikatorem");
//        return list;
//    }

    public List<String> getAllNames() {
        checkMembers();


        List<String> list = new ArrayList<>();
        memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .forEach(e -> {
                    if (!e.getActive()) {
                        list.add(e.getSecondName().concat(" " + e.getFirstName() + " BRAK SKŁADEK " + " leg. " + e.getLegitimationNumber()));
                    } else {
                        list.add(e.getSecondName().concat(" " + e.getFirstName() + " leg. " + e.getLegitimationNumber()));
                    }
                });
        list.sort(Comparator.comparing(String::new));
        LOG.info("Lista nazwisk z identyfikatorem");
        return list;

    }

    public List<Long> getMembersQuantity() {
        List<Long> list = new ArrayList<>();
//      whole adult
        long count = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(MemberEntity::getAdult)
                .count();
//      adult active
        long count1 = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(MemberEntity::getAdult)
                .filter(MemberEntity::getActive)
                .count();

//      adult not active
        long count2 = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(MemberEntity::getAdult)
                .filter(f -> !f.getActive())
                .count();

//      whole not adult
        long count3 = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getAdult())
                .count();
//      not adult active
        long count4 = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getAdult())
                .filter(MemberEntity::getActive)
                .count();
//      not adult not active
        long count5 = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getAdult())
                .filter(f -> !f.getActive())
                .count();

//      adult erased
        long count6 = memberRepository.findAll().stream()
                .filter(MemberEntity::getErased)
                .filter(MemberEntity::getAdult)
                .count();
//      not adult erased
        long count7 = memberRepository.findAll().stream()
                .filter(MemberEntity::getErased)
                .filter(f -> !f.getAdult())
                .count();
        list.add(count);
        list.add(count1);
        list.add(count2);
        list.add(count3);
        list.add(count4);
        list.add(count5);
        list.add(count6);
        list.add(count7);


        return list;
    }

    public List<MemberDTO> getAllMemberDTO() {
        checkMembers();

        List<MemberDTO> list = new ArrayList<>();

        memberRepository.findAll().stream().filter(f -> !f.getErased()).forEach(e -> list.add(Mapping.map2DTO(e)));
        list.sort(Comparator.comparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName));
        return list;
    }

    public List<MemberDTO> getAllMemberDTO(Boolean adult, Boolean active, Boolean erase) {

        List<MemberDTO> list = new ArrayList<>();
        if (!erase) {
            if (adult == null && active == null) {
                memberRepository.findAll().stream()
                        .filter(f -> !f.getErased())
                        .forEach(e -> list.add(Mapping.map2DTO(e)));
            }
            if (adult != null && active == null) {
                memberRepository.findAll().stream()
                        .filter(f -> !f.getErased())
                        .filter(f -> f.getAdult().equals(adult))
                        .forEach(e -> list.add(Mapping.map2DTO(e)));
            }
            if (adult == null && active != null) {
                memberRepository.findAll().stream()
                        .filter(f -> !f.getErased())
                        .filter(f -> f.getActive().equals(active))
                        .forEach(e -> list.add(Mapping.map2DTO(e)));
            }
            if (adult != null && active != null) {
                memberRepository.findAll().stream()
                        .filter(f -> !f.getErased())
                        .filter(f -> f.getAdult().equals(adult))
                        .filter(f -> f.getActive().equals(active))
                        .forEach(e -> list.add(Mapping.map2DTO(e)));
            }
        } else {
            if (adult == null) {
                memberRepository.findAll().stream()
                        .filter(MemberEntity::getErased)
                        .forEach(e -> list.add(Mapping.map2DTO(e)));
            }
            if (adult != null) {
                memberRepository.findAll().stream()
                        .filter(f -> f.getAdult().equals(adult))
                        .filter(MemberEntity::getErased)
                        .forEach(e -> list.add(Mapping.map2DTO(e)));
            }
        }

//        if (adult == null && !erase) {
//            if (active != null) {
//                memberRepository.findAll().stream()
//                        .filter(f -> !f.getErased())
//                        .filter(f -> f.getActive().equals(active))
//                        .forEach(e -> list.add(Mapping.map2DTO(e)));
//            } else {
//                memberRepository.findAll().stream()
//                        .filter(f -> !f.getErased())
//                        .forEach(e -> list.add(Mapping.map2DTO(e)));
//            }
//        }
//        if (active == null && !erase) {
//            if (adult != null) {
//                memberRepository.findAll().stream()
//                        .filter(f -> !f.getErased())
//                        .filter(f -> f.getAdult().equals(adult))
//                        .forEach(e -> list.add(Mapping.map2DTO(e)));
//            } else {
//                memberRepository.findAll().stream()
//                        .filter(f -> !f.getErased())
//                        .forEach(e -> list.add(Mapping.map2DTO(e)));
//            }
//        }
//        if (erase) {
//            if (adult != null) {
//                memberRepository.findAll().stream()
//                        .filter(MemberEntity::getErased)
//                        .filter(f -> f.getAdult().equals(adult))
//                        .forEach(e -> list.add(Mapping.map2DTO(e)));
//            } else {
//                memberRepository.findAll().stream()
//                        .filter(MemberEntity::getErased)
//                        .forEach(e -> list.add(Mapping.map2DTO(e)));
//            }
//        } else {
//            memberRepository.findAll().stream()
//                    .filter(f -> !f.getErased())
//                    .filter(f -> f.getActive().equals(active))
//                    .filter(f -> f.getAdult().equals(adult))
//                    .forEach(e -> list.add(Mapping.map2DTO(e)));
//        }
        list.sort(Comparator.comparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName));
        return list;
    }

    public boolean changePzss(String uuid) {
        MemberEntity memberEntity = memberRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);
        memberEntity.setPzss(true);
        memberRepository.saveAndFlush(memberEntity);
        return true;

    }

    public List<String> getErasedType() {

        List<String> list = new ArrayList<>();
        ErasedType[] values = ErasedType.values();
        for (int i = 1; i < values.length; i++) {
            list.add(values[i].getName());
        }
        return list;
    }

    public List<String> getMembersToEraseEmails() {
        LocalDate notValidContribution = LocalDate.of(LocalDate.now().getYear(), 12, 31).minusYears(2);

        List<String> list = new ArrayList<>();
        List<MemberEntity> collect = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getActive())
                .filter(f -> (!f.getHistory().getContributionList().isEmpty() && f.getHistory().getContributionList().get(0).getValidThru().minusDays(1).isBefore(notValidContribution)) || f.getHistory().getContributionList().isEmpty())
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        collect.forEach(e -> {
            if ((e.getEmail() != null && !e.getEmail().isEmpty()) && !e.getErased()) {
                list.add(e.getEmail().concat(";"));
            }
        });
        return list;

    }

    public List<String> getMembersToErasePhoneNumbers() {

        LocalDate notValidContribution = LocalDate.of(LocalDate.now().getYear(), 12, 31).minusYears(2);

        List<String> list = new ArrayList<>();
        List<MemberEntity> collect = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getActive())
                .filter(f -> (!f.getHistory().getContributionList().isEmpty() && f.getHistory().getContributionList().get(0).getValidThru().minusDays(1).isBefore(notValidContribution)) || f.getHistory().getContributionList().isEmpty())
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        collect.forEach(e -> {
            if ((e.getPhoneNumber() != null && !e.getPhoneNumber().isEmpty()) && !e.getErased()) {
                String phone = e.getPhoneNumber();
                String split = phone.substring(0, 3) + " ";
                String split1 = phone.substring(3, 6) + " ";
                String split2 = phone.substring(6, 9) + " ";
                String split3 = phone.substring(9, 12) + " ";
                String phoneSplit = split + split1 + split2 + split3;
                list.add(phoneSplit.concat(" " + e.getSecondName() + " " + e.getFirstName() + ";"));
            }
        });
        return list;
    }

    public List<String> getMembersToPoliceEmails() {
        LocalDate notValidLicense = LocalDate.now().minusYears(1);

        List<String> list = new ArrayList<>();
        List<MemberEntity> collect = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(f -> !f.getLicense().isValid())
                .filter(f -> f.getLicense().getValidThru().isBefore(notValidLicense))
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        collect.forEach(e -> {
            if ((e.getEmail() != null && !e.getEmail().isEmpty()) && !e.getErased()) {
                list.add(e.getEmail().concat(";"));
            }
        });
        return list;
    }

    public List<String> getMembersToPolicePhoneNumbers() {

        LocalDate notValidLicense = LocalDate.now().minusYears(1);

        List<String> list = new ArrayList<>();
        List<MemberEntity> collect = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(f -> !f.getLicense().isValid())
                .filter(f -> f.getLicense().getValidThru().isBefore(notValidLicense))
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        collect.forEach(e -> {
            if ((e.getPhoneNumber() != null && !e.getPhoneNumber().isEmpty()) && !e.getErased()) {
                String phone = e.getPhoneNumber();
                String split = phone.substring(0, 3) + " ";
                String split1 = phone.substring(3, 6) + " ";
                String split2 = phone.substring(6, 9) + " ";
                String split3 = phone.substring(9, 12) + " ";
                String phoneSplit = split + split1 + split2 + split3;
                list.add(phoneSplit.concat(" " + e.getSecondName() + " " + e.getFirstName() + ";"));
            }
        });
        return list;
    }

    public MemberEntity getMemberByUUID(String uuid) {
        return memberRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);
    }

    public List<String> getMembersEmailsNoActive() {

        List<String> list = new ArrayList<>();
        List<MemberEntity> collect = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getActive())
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        collect.forEach(e -> {
            if ((e.getEmail() != null && !e.getEmail().isEmpty())) {
                list.add(e.getEmail().concat(";"));
            }
        });
        return list;
    }

    public List<String> getMembersPhoneNumbersNoActive() {

        List<String> list = new ArrayList<>();
        List<MemberEntity> collect = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getActive())
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        collect.forEach(e -> {
            if ((e.getPhoneNumber() != null && !e.getPhoneNumber().isEmpty())) {
                String phone = e.getPhoneNumber();
                String split = phone.substring(0, 3) + " ";
                String split1 = phone.substring(3, 6) + " ";
                String split2 = phone.substring(6, 9) + " ";
                String split3 = phone.substring(9, 12) + " ";
                String phoneSplit = split + split1 + split2 + split3;
                list.add(phoneSplit.concat(" " + e.getSecondName() + " " + e.getFirstName() + ";"));
            }
        });
        return list;
    }

    public Boolean getMemberPeselIsPresent(String pesel) {
        return memberRepository.findByPesel(pesel).isPresent();
    }

    public Boolean getMemberIDCardPresent(String idCard) {
        return memberRepository.findByIDCard(idCard).isPresent();
    }

    public Boolean getMemberEmailPresent(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }
}
