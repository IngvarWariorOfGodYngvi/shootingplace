package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.ErasedEntity;
import com.shootingplace.shootingplace.domain.entities.LicenseEntity;
import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.enums.ErasedType;
import com.shootingplace.shootingplace.domain.models.Member;
import com.shootingplace.shootingplace.domain.models.MemberDTO;
import com.shootingplace.shootingplace.repositories.ClubRepository;
import com.shootingplace.shootingplace.repositories.ErasedRepository;
import com.shootingplace.shootingplace.repositories.LicenseRepository;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final AddressService addressService;
    private final LicenseService licenseService;
    private final LicenseRepository licenseRepository;
    private final ShootingPatentService shootingPatentService;
    private final ContributionService contributionService;
    private final HistoryService historyService;
    private final WeaponPermissionService weaponPermissionService;
    private final MemberPermissionsService memberPermissionsService;
    private final PersonalEvidenceService personalEvidenceService;
    private final ClubRepository clubRepository;
    private final ChangeHistoryService changeHistoryService;
    private final ErasedRepository erasedRepository;
    private final Logger LOG = LogManager.getLogger();


    public MemberService(MemberRepository memberRepository,
                         AddressService addressService,
                         LicenseService licenseService, LicenseRepository licenseRepository,
                         ShootingPatentService shootingPatentService,
                         ContributionService contributionService,
                         HistoryService historyService,
                         WeaponPermissionService weaponPermissionService,
                         MemberPermissionsService memberPermissionsService,
                         PersonalEvidenceService personalEvidenceService,
                         ClubRepository clubRepository,
                         ChangeHistoryService changeHistoryService,
                         ErasedRepository erasedRepository) {
        this.memberRepository = memberRepository;
        this.addressService = addressService;
        this.licenseService = licenseService;
        this.licenseRepository = licenseRepository;
        this.shootingPatentService = shootingPatentService;
        this.contributionService = contributionService;
        this.historyService = historyService;
        this.weaponPermissionService = weaponPermissionService;
        this.memberPermissionsService = memberPermissionsService;
        this.personalEvidenceService = personalEvidenceService;
        this.clubRepository = clubRepository;
        this.changeHistoryService = changeHistoryService;
        this.erasedRepository = erasedRepository;
    }


    //--------------------------------------------------------------------------

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

    public void checkMembers() {
        // dorośli
        List<MemberEntity> adultMembers = memberRepository
                .findAll()
                .stream()
                .filter(f -> !f.getErased())
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
        MemberEntity memberEntity;

        List<MemberEntity> memberEntityList = memberRepository.findAll();
        if (memberRepository.findByPesel(member.getPesel()).isPresent()) {
            LOG.error("Ktoś już ma taki numer PESEL");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Ktoś już ma taki numer PESEL\"");
        }
        String finalEmail = member.getEmail();
        boolean anyMatch = memberEntityList.stream()
//                .filter(f -> !f.getErased())
                .filter(f -> f.getEmail() != null)
                .filter(f -> !f.getEmail().isEmpty())
                .anyMatch(f -> f.getEmail().equals(finalEmail));
        if (anyMatch) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Ktoś już ma taki e-mail\" " + finalEmail);
        }
        if (member.getLegitimationNumber() != null) {
            if (memberRepository.findByLegitimationNumber(member.getLegitimationNumber()).isPresent()) {
                if (memberEntityList.stream().filter(MemberEntity::getErased).anyMatch(e -> e.getLegitimationNumber().equals(member.getLegitimationNumber()))) {
                    LOG.error("Ktoś już ma taki numer legitymacji");
                    return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Ktoś wśród skreślonych już ma taki numer legitymacji\"");
                } else {
                    LOG.error("Ktoś już ma taki numer legitymacji");
                    return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Ktoś już ma taki numer legitymacji\"");
                }
            }
        }
        if (memberEntityList.stream().filter(f -> !f.getErased()).anyMatch(e -> e.getIDCard().trim().toUpperCase().equals(member.getIDCard()))) {
            LOG.error("Ktoś już ma taki numer dowodu osobistego");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Ktoś już ma taki numer dowodu osobistego\"");
        } else {
            String email = member.getEmail();
            if (member.getEmail() == null || member.getEmail().isEmpty()) {
                email = "";
            }
            LocalDate joinDate;
            int legitimationNumber;
            if (member.getJoinDate() == null) {
                joinDate = LocalDate.now();
                LOG.info("ustawiono domyślną datę zapisu " + joinDate);
            } else {
                joinDate = member.getJoinDate();
                LOG.info("ustawiono datę zapisu na " + joinDate);
            }
            if (member.getLegitimationNumber() == null) {
                int number;
                if (memberEntityList.isEmpty()) {
                    number = 1;
                } else {
                    number = memberEntityList.stream().filter(f -> f.getLegitimationNumber() != null).max(Comparator.comparing(MemberEntity::getLegitimationNumber)).orElseThrow(EntityNotFoundException::new).getLegitimationNumber() + 1;
                }
                legitimationNumber = number;
                LOG.info("ustawiono domyślny numer legitymacji : " + legitimationNumber);

            } else {
                legitimationNumber = member.getLegitimationNumber();
            }
            String s = "+48";
            String phone = null;
            if (member.getPhoneNumber() != null) {
                phone = (s + member.getPhoneNumber().replaceAll("\\s", ""));
            }
            boolean adult;
            if (!member.getAdult()) {
                LOG.info("Klubowicz należy do młodzieży");
                adult = false;
            } else {
                LOG.info("Klubowicz należy do grupy dorosłej");
                adult = true;
            }
            String[] s1 = member.getFirstName().split(" ");
            StringBuilder firstNames = new StringBuilder();
            for (String value : s1) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                firstNames.append(splinted);
            }
            member.setFirstName(firstNames.toString());
            member.setSecondName(member.getSecondName().toUpperCase());
            member.setEmail(email.toLowerCase());
            member.setJoinDate(joinDate);
            member.setLegitimationNumber(legitimationNumber);
            member.setPhoneNumber(phone);
            member.setAdult(adult);
            member.setAddress(addressService.getAddress());
            member.setIDCard(member.getIDCard().trim().toUpperCase());
            member.setPesel(member.getPesel());
            member.setClub(clubRepository.findById(1).orElseThrow(EntityNotFoundException::new));
            member.setShootingPatent(shootingPatentService.getShootingPatent());
            member.setLicense(licenseService.getLicense());
            member.setHistory(historyService.getHistory());
            member.setWeaponPermission(weaponPermissionService.getWeaponPermission());
            member.setMemberPermissions(memberPermissionsService.getMemberPermissions());
            member.setPersonalEvidence(personalEvidenceService.getPersonalEvidence());
            member.setPzss(false);
            member.setErasedEntity(null);
            member.setActive(true);
            memberEntity = memberRepository.save(Mapping.map(member));
            historyService.addContribution(memberEntity.getUuid(),
                    contributionService.addFirstContribution(memberEntity.getUuid(), LocalDate.now()));
        }
        changeHistoryService.addRecordToChangeHistory(pinCode, memberEntity.getClass().getSimpleName() + " addNewMember", memberEntity.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("\"" + memberEntity.getUuid() + "\"");


    }


    //--------------------------------------------------------------------------
    public ResponseEntity<?> activateOrDeactivateMember(String memberUUID, String pinCode) {
        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("\"Nie znaleziono Klubowicza\"");
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        memberEntity.toggleActive();
        memberRepository.saveAndFlush(memberEntity);
        LOG.info("Zmieniono status");
        changeHistoryService.addRecordToChangeHistory(pinCode, memberEntity.getClass().getSimpleName() + " activateOrDeactivateMember", memberEntity.getUuid());
        return ResponseEntity.ok("\"Zmieniono status aktywny/nieaktywny\"");
    }

    public ResponseEntity<?> changeAdult(String memberUUID, String pinCode) {
        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("\"Nie znaleziono Klubowicza\"");
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        if (memberEntity.getAdult()) {
            LOG.info("Klubowicz należy już do grupy powszechnej");
            return ResponseEntity.badRequest().body("\"Klubowicz należy już do grupy powszechnej\"");
        }
        if (LocalDate.now().minusYears(1).minusDays(1).isBefore(memberEntity.getJoinDate())) {
            LOG.info("Klubowicz ma za krótki staż jako młodzież");
            return ResponseEntity.badRequest().body("\"Klubowicz ma za krótki staż jako młodzież\"");
        }
        memberEntity.setAdult(true);
        memberRepository.saveAndFlush(memberEntity);
        historyService.changeContributionTime(memberUUID);
        LOG.info("Klubowicz należy od teraz do grupy dorosłej : " + LocalDate.now());
        changeHistoryService.addRecordToChangeHistory(pinCode, memberEntity.getClass().getSimpleName() + " changeAdult", memberEntity.getUuid());
        return ResponseEntity.ok("\"Klubowicz należy od teraz do grupy dorosłej\"");
    }

    public ResponseEntity<?> eraseMember(String memberUUID, String erasedType, LocalDate erasedDate, String additionalDescription, String pinCode) {
        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("\"Nie znaleziono Klubowicza\"");
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        if (!memberEntity.getErased()) {
            ErasedEntity build = ErasedEntity.builder()
                    .erasedType(erasedType)
                    .date(erasedDate)
                    .additionalDescription(additionalDescription)
                    .date(LocalDate.now())
                    .build();
            erasedRepository.save(build);
            memberEntity.setErasedEntity(build);
            memberEntity.toggleErase();
            memberEntity.setPzss(false);
            LOG.info("Klubowicz skreślony : " + LocalDate.now());
        }
        memberRepository.saveAndFlush(memberEntity);
        changeHistoryService.addRecordToChangeHistory(pinCode, memberEntity.getClass().getSimpleName() + " eraseMember", memberEntity.getUuid());
        return ResponseEntity.ok("\"Usunięto Klubowicza\"");
    }

    //--------------------------------------------------------------------------
    public ResponseEntity<?> updateMember(String memberUUID, Member member) {

        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().build();
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
            LOG.info("Zaktualizowano pomyślnie Imię");
        }
        if (member.getSecondName() != null && !member.getSecondName().isEmpty()) {
            memberEntity.setSecondName(member.getSecondName().toUpperCase());
            LOG.info("Zaktualizowano pomyślnie Nazwisko");

        }
        if (member.getJoinDate() != null) {
            memberEntity.setJoinDate(member.getJoinDate());
            LOG.info("Zaktualizowano pomyślnie Data przystąpienia do klubu");
        }
        if (member.getLegitimationNumber() != null) {
            if (memberRepository.findByLegitimationNumber(member.getLegitimationNumber()).isPresent()) {
                LOG.warn("Już ktoś ma taki numer legitymacji");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Już ktoś ma taki numer legitymacji\"");
            } else {
                memberEntity.setLegitimationNumber(member.getLegitimationNumber());
                LOG.info("Zaktualizowano pomyślnie Numer Legitymacji");
            }
        }
        if (member.getEmail() != null && !member.getEmail().isEmpty()) {
            if (memberRepository.findByEmail(member.getEmail()).isPresent() && !memberEntity.getEmail().equals(member.getEmail())) {
                LOG.error("Już ktoś ma taki sam e-mail");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Uwaga! Już ktoś ma taki sam e-mail\"");
            } else {
                memberEntity.setEmail(member.getEmail().trim().toLowerCase());
                LOG.info("Zaktualizowano pomyślnie Email");
            }
        }
        if (member.getPesel() != null && !member.getPesel().isEmpty()) {
            if (memberRepository.findByPesel(member.getPesel()).isPresent()) {
                LOG.error("Już ktoś ma taki sam numer PESEL");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Już ktoś ma taki sam numer PESEL\"");
            } else {
                memberEntity.setPesel(member.getPesel());
                LOG.info("Zaktualizowano pomyślnie Numer PESEL");
            }
        }
        if (member.getPhoneNumber() != null && !member.getPhoneNumber().isEmpty()) {
            if (member.getPhoneNumber().replaceAll("\\s-", "").length() != 9 && !member.getPhoneNumber().isEmpty()) {
                LOG.error("Żle podany numer");
            }
            String s = "+48";
            memberEntity.setPhoneNumber((s + member.getPhoneNumber()).replaceAll("\\s", ""));
            if (memberRepository.findByPhoneNumber((s + member.getPhoneNumber()).replaceAll("\\s", "")).isPresent() && !memberEntity.getPhoneNumber().equals(member.getPhoneNumber())) {
                LOG.error("Ktoś już ma taki numer telefonu");
            }
            if (member.getPhoneNumber().equals(memberEntity.getPhoneNumber())) {
                memberEntity.setPhoneNumber(member.getPhoneNumber());
                LOG.info("Zaktualizowano pomyślnie Numer Telefonu");
            }
        }
        if (member.getIDCard() != null && !member.getIDCard().isEmpty()) {
            if (memberRepository.findByIDCard(member.getIDCard().trim()).isPresent()&& !memberEntity.getIDCard().equals(member.getIDCard())) {
                LOG.error("Ktoś już ma taki numer dowodu");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("\"Ktoś już ma taki numer dowodu\"");
            } else {
                memberEntity.setIDCard(member.getIDCard().trim().toUpperCase());
                LOG.info("Zaktualizowano pomyślnie Numer Dowodu");
            }
        }

        memberRepository.saveAndFlush(memberEntity);

        return ResponseEntity.ok("\"Zaktualizowano dane klubowicza\"");
    }


    public MemberEntity getMember(int number) {
        LOG.info("Wywołano Klubowicza");
        return memberRepository.findByLegitimationNumber(number).orElseThrow(EntityNotFoundException::new);

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
                .filter(f -> !f.getErased())
                .filter(MemberEntity::getAdult)
                .filter(MemberEntity::getActive)
                .filter(f -> f.getShootingPatent() != null)
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
                .filter(f -> !f.getErased())
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

        list.sort(Comparator.comparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName));
        return list;
    }

    public ResponseEntity<?> changePzss(String uuid) {
        if (memberRepository.existsById(uuid)) {
            MemberEntity memberEntity = memberRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);
            memberEntity.setPzss(true);
            memberRepository.saveAndFlush(memberEntity);
            return ResponseEntity.ok("\"Wskazano, że Klubowicz jest wpisany do Portalu PZSS\"");
        } else {
            return ResponseEntity.badRequest().body("\"Nie znaleziono Klubowicza\"");
        }

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
                .filter(f -> f.getHistory().getContributionList().isEmpty() || f.getHistory().getContributionList().get(0).getValidThru().minusDays(1).isBefore(notValidContribution))
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
                .filter(f -> f.getHistory().getContributionList().isEmpty() || f.getHistory().getContributionList().get(0).getValidThru().minusDays(1).isBefore(notValidContribution))
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
