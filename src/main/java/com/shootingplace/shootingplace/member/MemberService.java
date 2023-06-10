package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.contributions.ContributionService;
import com.shootingplace.shootingplace.enums.ErasedType;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.history.LicensePaymentHistoryEntity;
import com.shootingplace.shootingplace.history.LicensePaymentHistoryRepository;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.license.LicenseRepository;
import com.shootingplace.shootingplace.license.LicenseService;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentService;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermissionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final LicenseService licenseService;
    private final LicenseRepository licenseRepository;
    private final ShootingPatentService shootingPatentService;
    private final ContributionService contributionService;
    private final HistoryService historyService;
    private final WeaponPermissionService weaponPermissionService;
    private final MemberPermissionsService memberPermissionsService;
    private final ClubRepository clubRepository;
    private final ChangeHistoryService changeHistoryService;
    private final ErasedRepository erasedRepository;
    private final LicensePaymentHistoryRepository licensePaymentHistoryRepository;

    private final Environment env;

    private final Logger LOG = LogManager.getLogger();


    public MemberService(MemberRepository memberRepository,
                         LicenseService licenseService, LicenseRepository licenseRepository,
                         ShootingPatentService shootingPatentService,
                         ContributionService contributionService,
                         HistoryService historyService,
                         WeaponPermissionService weaponPermissionService,
                         MemberPermissionsService memberPermissionsService,
                         ClubRepository clubRepository,
                         ErasedRepository erasedRepository, LicensePaymentHistoryRepository licensePaymentHistoryRepository, ChangeHistoryService changeHistoryService, Environment env) {
        this.memberRepository = memberRepository;
        this.licenseService = licenseService;
        this.licenseRepository = licenseRepository;
        this.shootingPatentService = shootingPatentService;
        this.contributionService = contributionService;
        this.historyService = historyService;
        this.weaponPermissionService = weaponPermissionService;
        this.memberPermissionsService = memberPermissionsService;
        this.clubRepository = clubRepository;
        this.erasedRepository = erasedRepository;
        this.licensePaymentHistoryRepository = licensePaymentHistoryRepository;
        this.changeHistoryService = changeHistoryService;
        this.env = env;
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
        list.sort(Comparator.comparing(Member::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))));
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
        LOG.info("Sprawdzam składki i licencje");
        if (env.getActiveProfiles()[0].equals("prod")) {
            // dorośli
            historyService.checkStarts();
            List<MemberEntity> adultMembers = memberRepository.findAllByErasedFalse();
            // nie ma żadnych składek
            adultMembers.forEach(e -> {
                if (e.getHistory().getContributionList().isEmpty() || e.getHistory().getContributionList() == null) {
                    e.setActive(false);
                    memberRepository.save(e);
                }
                //dzisiejsza data jest później niż składka + 3 miesiące
                else {
                    if (e.getAdult()) {
                        if (e.getHistory().getContributionList().get(0).getValidThru().plusMonths(3).isBefore(LocalDate.now())) {
                            if (e.getActive()) {
                                e.setActive(false);
                                LOG.info("zmieniono " + e.getSecondName());
                                memberRepository.save(e);
                            }
                        } else {
                            e.setActive(true);
                        }
                    } else {
                        LocalDate validThru = e.getHistory().getContributionList().get(0).getValidThru();
                        if ((validThru.equals(LocalDate.of(validThru.getYear(), 2, 28)) && validThru.plusMonths(1).isBefore(LocalDate.now()))
                                || (validThru.equals(LocalDate.of(validThru.getYear(), 8, 31)) && validThru.plusMonths(2).isBefore(LocalDate.now()))) {
                            if (e.getActive()) {
                                e.setActive(false);
                                LOG.info("zmieniono " + e.getSecondName());
                                memberRepository.save(e);
                            }
                        }
                    }
                }
                if (e.getLicense().getNumber() != null) {
                    LicenseEntity license = e.getLicense();
                    license.setValid(!e.getLicense().getValidThru().isBefore(LocalDate.now()));
                    licenseRepository.save(license);
                }
            });
            //młodzież
            List<MemberEntity> nonAdultMembers = memberRepository.findAllByErasedFalse()
                    .stream()
                    .filter(f -> !f.getAdult())
                    .collect(Collectors.toList());
            // nie ma żadnych składek
            nonAdultMembers.forEach(e -> {
                if (e.getHistory().getContributionList().isEmpty() || e.getHistory().getContributionList() == null) {
                    e.setActive(false);
                    memberRepository.save(e);
                } else {
                    //dzisiejsza data jest później niż składka + 1 || 2 miesiące
                    LocalDate validThru = e.getHistory().getContributionList().get(0).getValidThru();
                    if ((validThru.equals(LocalDate.of(validThru.getYear(), 2, 28)) && validThru.plusMonths(1).isBefore(LocalDate.now()))
                            || (validThru.equals(LocalDate.of(validThru.getYear(), 8, 31)) && validThru.plusMonths(2).isBefore(LocalDate.now()))) {
                        if (e.getActive()) {
                            e.setActive(false);
                            LOG.info("zmieniono " + e.getSecondName());
                            memberRepository.save(e);
                        }
                    }
                }
                if (e.getLicense().getNumber() != null) {
                    LicenseEntity license = e.getLicense();
                    license.setValid(!e.getLicense().getValidThru().isBefore(LocalDate.now()));
                    licenseRepository.save(license);
                }
            });
        }
        if (env.getActiveProfiles()[0].equals("rcs")) {
            historyService.checkStarts();
            List<MemberEntity> members = memberRepository.findAllByErasedFalse();
            members.forEach(e -> {
                if (e.getHistory().getContributionList().isEmpty() || e.getHistory().getContributionList() == null) {
                    e.setActive(false);
                    memberRepository.save(e);
                } else {
                    if (e.getAdult()) {
                        if (e.getHistory().getContributionList().get(0).getValidThru().isBefore(LocalDate.now())) {
                            if (e.getActive()) {
                                e.setActive(false);
                                LOG.info("zmieniono " + e.getSecondName());
                                memberRepository.save(e);
                            }
                        } else {
                            e.setActive(true);
                        }
                    }
                }
                if (e.getLicense().getNumber() != null) {
                    LicenseEntity license = e.getLicense();
                    license.setValid(!e.getLicense().getValidThru().isBefore(LocalDate.now()));
                    licenseRepository.save(license);
                }
            });
        }
    }

    //--------------------------------------------------------------------------
    public ResponseEntity<?> addNewMember(Member member, Address address, boolean returningToClub, String pinCode) {
        MemberEntity memberEntity;

        List<MemberEntity> memberEntityList = memberRepository.findAll();
        MemberEntity member1 = memberEntityList.stream().filter(f -> f.getPesel().equals(member.getPesel())).findFirst().orElse(null);
        if (member1 != null) {
            if (returningToClub && member1.getErased()) {
                LOG.info("Ktoś z usuniętych ma taki numer PESEL");
            } else {
                LOG.error("Ktoś już ma taki numer PESEL");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Ktoś już ma taki numer PESEL");
            }
        }
        String finalEmail = member.getEmail();
        boolean anyMatch = memberEntityList.stream()
                .filter(f -> f.getEmail() != null)
                .filter(f -> !f.getEmail().isEmpty())
                .anyMatch(f -> f.getEmail().equals(finalEmail));
        if (anyMatch) {
            if (returningToClub) {
                LOG.info("Ktoś z usuniętych już ma taki e-mail");
            } else {
                LOG.info("Ktoś już ma taki e-mail");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Ktoś już ma taki e-mail");
            }
        }
        if (member.getLegitimationNumber() != null) {
            if (memberRepository.findByLegitimationNumber(member.getLegitimationNumber()).isPresent()) {
                if (returningToClub) {
                    LOG.info("Będzie przyznany nowy numer legitymacji");
                } else {
                    if (memberEntityList.stream().filter(MemberEntity::getErased).anyMatch(e -> e.getLegitimationNumber().equals(member.getLegitimationNumber()))) {
                        LOG.error("Ktoś już ma taki numer legitymacji");
                        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Ktoś wśród skreślonych już ma taki numer legitymacji");
                    } else {
                        LOG.error("Ktoś już ma taki numer legitymacji");
                        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Ktoś już ma taki numer legitymacji");
                    }
                }
            }
        }
        if (memberEntityList.stream().filter(f -> !f.getErased()).anyMatch(e -> e.getIDCard().trim().toUpperCase().equals(member.getIDCard()))) {
            if (returningToClub) {
                LOG.info("Ktoś z usuniętych już ma taki numer dowodu osobistego");
            } else {
                LOG.error("Ktoś już ma taki numer dowodu osobistego");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Ktoś już ma taki numer dowodu osobistego");
            }
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
            PersonalEvidence peBuild = PersonalEvidence.builder()
                    .ammoList(new ArrayList<>())
                    .build();
            member.setFirstName(firstNames.toString().trim());
            member.setSecondName(member.getSecondName().toUpperCase());
            member.setEmail(email.toLowerCase());
            member.setJoinDate(joinDate);
            member.setLegitimationNumber(legitimationNumber);
            member.setPhoneNumber(phone);
            member.setAdult(adult);
            member.setAddress(address);
            member.setIDCard(member.getIDCard().trim().toUpperCase());
            member.setPesel(member.getPesel());
            member.setClub(clubRepository.findById(1).orElseThrow(EntityNotFoundException::new));
            member.setShootingPatent(shootingPatentService.getShootingPatent());
            member.setLicense(licenseService.getLicense());
            member.setHistory(historyService.getHistory());
            member.setWeaponPermission(weaponPermissionService.getWeaponPermission());
            member.setMemberPermissions(memberPermissionsService.getMemberPermissions());
            member.setPersonalEvidence(peBuild);
            member.setPzss(false);
            member.setErasedEntity(null);
            member.setActive(true);

            ResponseEntity<?> response = getStringResponseEntity(pinCode, Mapping.map(member), HttpStatus.CREATED, "addNewMember", "nowy Klubowicz");
            if (response.getStatusCode().equals(HttpStatus.CREATED)) {
                memberEntity = memberRepository.save(Mapping.map(member));
                historyService.addContribution(memberEntity.getUuid(),
                        contributionService.addFirstContribution(memberEntity.getUuid(), LocalDate.now()));
                response = ResponseEntity.status(201).body(memberEntity.getUuid());
            }
            return response;
        }
        return ResponseEntity.badRequest().body("Coś poszło nie tak");

    }


    //--------------------------------------------------------------------------
    public ResponseEntity<?> activateOrDeactivateMember(String memberUUID, String pinCode) {
        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);

        ResponseEntity<?> response = getStringResponseEntity(pinCode, memberEntity, HttpStatus.OK, "activateOrDeactivateMember", "Zmieniono status aktywny/nieaktywny");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            memberEntity.toggleActive();
            memberRepository.save(memberEntity);
            LOG.info("Zmieniono status");
        }

        return response;
    }

    public ResponseEntity<?> changeAdult(String memberUUID, String pinCode) {
        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        if (memberEntity.getAdult()) {
            LOG.info("Klubowicz należy już do grupy powszechnej");
            return ResponseEntity.badRequest().body("Klubowicz należy już do grupy powszechnej");
        }
        if (LocalDate.now().minusYears(1).minusDays(1).isBefore(memberEntity.getJoinDate())) {
            LOG.info("Klubowicz ma za krótki staż jako młodzież");
            return ResponseEntity.badRequest().body("Klubowicz ma za krótki staż jako młodzież");
        }
        ResponseEntity<?> response = getStringResponseEntity(pinCode, memberEntity, HttpStatus.OK, "changeAdult", "Klubowicz należy od teraz do grupy dorosłej");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            memberEntity.setAdult(true);
            memberRepository.save(memberEntity);
            historyService.changeContributionTime(memberUUID);
            LOG.info("Klubowicz należy od teraz do grupy dorosłej : " + LocalDate.now());
        }
        return response;
    }

    public ResponseEntity<?> eraseMember(String memberUUID, String erasedType, LocalDate erasedDate, String additionalDescription, String pinCode) {
        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
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
        ResponseEntity<?> response = getStringResponseEntity(pinCode, memberEntity, HttpStatus.OK, "eraseMember", "Usunięto Klubowicza");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            memberRepository.save(memberEntity);
        }
        return response;
    }

    //--------------------------------------------------------------------------
    public ResponseEntity<?> updateMember(String memberUUID, Member member, String pinCode) {
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
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Już ktoś ma taki numer legitymacji");
            } else {
                memberEntity.setLegitimationNumber(member.getLegitimationNumber());
                LOG.info("Zaktualizowano pomyślnie Numer Legitymacji");
            }
        }
        if (member.getEmail() != null && !member.getEmail().isEmpty()) {
            if (memberRepository.findByEmail(member.getEmail()).isPresent() && !memberEntity.getEmail().equals(member.getEmail())) {
                LOG.error("Już ktoś ma taki sam e-mail");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Już ktoś ma taki sam e-mail");
            } else {
                memberEntity.setEmail(member.getEmail().trim().toLowerCase());
                LOG.info("Zaktualizowano pomyślnie Email");
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
            if (memberRepository.findByIDCard(member.getIDCard().trim()).isPresent() && !memberEntity.getIDCard().equals(member.getIDCard())) {
                LOG.error("Ktoś już ma taki numer dowodu");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Ktoś już ma taki numer dowodu");
            } else {
                memberEntity.setIDCard(member.getIDCard().trim().toUpperCase());
                LOG.info("Zaktualizowano pomyślnie Numer Dowodu");
            }
        }
        ResponseEntity<?> response = getStringResponseEntity(pinCode, memberEntity, HttpStatus.OK, "update member", "Zaktualizowano dane klubowicza " + memberEntity.getMemberName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            memberRepository.save(memberEntity);
        }
        return response;

    }


    public ResponseEntity<?> getMember(int number) {
        System.out.println(number);
        if (memberRepository.existsByLegitimationNumber(number)) {
            MemberEntity memberEntity = memberRepository.findByLegitimationNumber(number).orElse(null);
            assert memberEntity != null;
            LOG.info("Wywołano Klubowicza " + memberEntity.getFirstName() + " " + memberEntity.getSecondName());
            return ResponseEntity.ok(memberEntity);
        } else {
            return ResponseEntity.badRequest().body("Klubowicz o podanym numerze legitymacji nie istnieje");
        }

    }

    public MemberEntity getMember(String uuid) {
        if (memberRepository.existsById(uuid)) {
            return memberRepository.getOne(uuid);
        } else {
            return null;
        }

    }

    public ResponseEntity<?> getMemberUUIDByLegitimationNumber(int number) {

        if (!memberRepository.existsByLegitimationNumber(number)) {
            return ResponseEntity.badRequest().body("Nie udało się znaleźć takiej osoby");
        }
        String uuid = memberRepository.findByLegitimationNumber(number).orElseThrow(EntityNotFoundException::new).getUuid();

        return ResponseEntity.ok(uuid);

    }

    public List<MemberInfo> getAllNames() {
        return memberRepository.findAllByErasedFalse().stream()
                .map(Mapping::map1)
                .sorted(Comparator.comparing(MemberInfo::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))).thenComparing(MemberInfo::getFirstName, Collator.getInstance(Locale.forLanguageTag("pl"))))
                .collect(Collectors.toList());

    }

    public List<Long> getMembersQuantity() {
        List<Long> list = new ArrayList<>();
//      whole adult
        List<MemberEntity> all = memberRepository.findAll();
        long count = all.stream()
                .filter(f -> !f.getErased())
                .filter(MemberEntity::getAdult)
                .count();
//      license valid
        long count1 = all.stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getClub().getId().equals(1))
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(MemberEntity::getPzss)
                .filter(f -> f.getLicense().isValid())
                .count();

//      license not valid
        long count2 = all.stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getClub().getId().equals(1))
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(MemberEntity::getPzss)
                .filter(f -> !f.getLicense().isValid())
                .count();

//      whole not adult
        long count3 = all.stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getAdult())
                .count();
//      not adult active
        long count4 = all.stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getAdult())
                .filter(MemberEntity::getActive)
                .count();
//      not adult not active
        long count5 = all.stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getAdult())
                .filter(f -> !f.getActive())
                .count();

//      adult erased
        long count6 = all.stream()
                .filter(MemberEntity::getErased)
                .filter(MemberEntity::getAdult)
                .count();
//      not adult erased
        long count7 = all.stream()
                .filter(MemberEntity::getErased)
                .filter(f -> !f.getAdult())
                .count();
        List<LicensePaymentHistoryEntity> allLicensePayment = licensePaymentHistoryRepository.findAll();
        long count8 = allLicensePayment.stream()
                .filter(f -> !f.isPayInPZSSPortal())
                .count();
        long count9 = allLicensePayment.stream()
                .filter(f -> f.getDate().getYear() == LocalDate.now().getYear())
                .filter(LicensePaymentHistoryEntity::isNew)
                .count();
        long count10 = all.stream()
                .filter(f -> !f.getErased())
                .filter(MemberEntity::getAdult)
                .filter(MemberEntity::getActive)
                .count();
        long count11 = all.stream()
                .filter(f -> !f.getErased())
                .filter(MemberEntity::getAdult)
                .filter(f -> !f.getActive())
                .count();
        list.add(count);
        list.add(count1);
        list.add(count2);
        list.add(count3);
        list.add(count4);
        list.add(count5);
        list.add(count6);
        list.add(count7);
        list.add(count8);
        list.add(count9);
        list.add(count10);
        list.add(count11);


        return list;
    }

    public List<MemberDTO> getAllMemberDTO() {
        Pageable page = PageRequest.of(0, memberRepository.findAll().size(), Sort.by("secondName").descending());
        return memberRepository.findAllByErasedFalse(page)
                .stream()
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))).thenComparing(MemberDTO::getFirstName, Collator.getInstance(Locale.forLanguageTag("pl"))))
                .collect(Collectors.toList());
    }

    public List<MemberDTO> getAllMemberDTO(Boolean adult, Boolean active, Boolean erase) {

        List<MemberDTO> list = new ArrayList<>();
        if (!erase) {
            if (adult == null && active == null) {
                list = memberRepository.findAllByErasedFalse().stream()
                        .map(Mapping::map2DTO)
                        .collect(Collectors.toList());
            }
            if (adult != null && active == null) {
                list = memberRepository.findAllByErasedFalse().stream()
                        .filter(f -> f.getAdult().equals(adult))
                        .map(Mapping::map2DTO)
                        .collect(Collectors.toList());
            }
            if (adult == null && active != null) {
                list = memberRepository.findAllByErasedFalse().stream()
                        .filter(f -> f.getActive().equals(active))
                        .map(Mapping::map2DTO)
                        .collect(Collectors.toList());
            }
            if (adult != null && active != null) {
                list = memberRepository.findAllByErasedFalse().stream()
                        .filter(f -> f.getAdult().equals(adult))
                        .filter(f -> f.getActive().equals(active))
                        .map(Mapping::map2DTO)
                        .collect(Collectors.toList());
            }
        } else {
            if (adult == null) {
                list = memberRepository.findAll().stream()
                        .filter(MemberEntity::getErased)
                        .map(Mapping::map2DTO)
                        .collect(Collectors.toList());
            }
            if (adult != null) {
                list = memberRepository.findAll().stream()
                        .filter(MemberEntity::getErased)
                        .filter(f -> f.getAdult().equals(adult))
                        .map(Mapping::map2DTO)
                        .collect(Collectors.toList());
            }
        }

        list.sort(Comparator.comparing(MemberDTO::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))).thenComparing(MemberDTO::getFirstName, Collator.getInstance(Locale.forLanguageTag("pl"))));
        return list;
    }

    public ResponseEntity<?> changePzss(String uuid) {
        if (memberRepository.existsById(uuid)) {
            MemberEntity memberEntity = memberRepository.getOne(uuid);
            if (!memberEntity.getPzss()) {
                memberEntity.setPzss(true);
                memberRepository.save(memberEntity);
                return ResponseEntity.ok("Wskazano, że Klubowicz jest wpisany do Portalu PZSS");
            } else {
                memberEntity.setPzss(false);
                memberRepository.save(memberEntity);
                return ResponseEntity.ok("Wskazano, że Klubowicz NIE jest wpisany do Portalu PZSS");
            }
        } else {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
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

    public MemberEntity getMemberByUUID(String uuid) {
        return memberRepository.getOne(uuid);
    }

    public Boolean getMemberPeselIsPresent(String pesel) {
        return memberRepository.findByPesel(pesel).isPresent();
    }

    public Boolean getMemberIDCardPresent(String idCard) {
        boolean present = memberRepository.findByIDCard(idCard).isPresent();
        if (present) {
            LOG.info("Znaleziono osobę w bazie");
        } else {
            LOG.info("Brak takiego numer w bazie");
        }
        return present;
    }

    public Boolean getMemberEmailPresent(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    public ResponseEntity<?> findMemberByBarCode(String barcode) {

        if (memberRepository.findByClubCardBarCode(barcode).isEmpty()) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        MemberEntity memberEntity = memberRepository.findByClubCardBarCode(barcode).orElseThrow(EntityNotFoundException::new);

        return ResponseEntity.ok(memberEntity);
    }

    public List<Member> getMembersToReportToThePolice() {
        LocalDate notValidLicense = LocalDate.now().minusYears(1);
        return memberRepository.findAllByErasedFalse().stream()
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(f -> !f.getLicense().isValid())
                .filter(f -> f.getLicense().getValidThru().isBefore(notValidLicense))
                .sorted(Comparator.comparing(MemberEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))))
                .map(Mapping::map).collect(Collectors.toList());
    }

    public List<Member> getMembersToErase() {
        LocalDate notValidContribution = LocalDate.of(LocalDate.now().getYear(), 12, 31).minusYears(2);
        return memberRepository.findAllByErasedFalseAndActiveFalse().stream()
                .filter(f -> f.getHistory().getContributionList().isEmpty() || f.getHistory().getContributionList().get(0).getValidThru().minusDays(1).isBefore(notValidContribution))
                .sorted(Comparator.comparing(MemberEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl")))).map(Mapping::map).collect(Collectors.toList());
    }

    public List<Member> getMembersErased() {
        return memberRepository.findAllByErasedTrue().stream()
                .sorted(Comparator.comparing(MemberEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))))
                .map(Mapping::map)
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> getStringResponseEntity(String pinCode, MemberEntity memberEntity, HttpStatus status, String methodName, String body) {
        ResponseEntity<String> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        if (memberEntity.getUuid() == null) {
            memberEntity.setUuid("nowy");
        }
        ResponseEntity<String> stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, memberEntity.getClass().getSimpleName() + " " + methodName + " ", memberEntity.getUuid());
        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }

    public ResponseEntity<?> getMemberByPESELNumber(String PESELNumber) {
        PESELNumber.replaceAll(" ", "");
        MemberEntity member = memberRepository.findByPesel(PESELNumber).orElse(null);
        return member != null ? ResponseEntity.ok(Mapping.map(member)) : ResponseEntity.badRequest().body("Brak numeru w Bazie");
    }

    public ResponseEntity<?> changeClub(String uuid, String clubName) {
        if (!memberRepository.existsById(uuid)) {
            return ResponseEntity.badRequest().body("Nie znaleziono klubowicza");
        }
        if (!clubRepository.existsByName(clubName)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubu");
        }
        MemberEntity member = memberRepository.getOne(uuid);

        ClubEntity club = clubRepository.findByName(clubName);
        member.setClub(clubRepository.getOne(club.getId()));
        memberRepository.save(member);
        return ResponseEntity.ok("Zmieniono Klub macierzysty zawodnika " + member.getMemberName() + " na: " + club.getName());
    }

    public ResponseEntity<?> deleteMember(String uuid) {
        try {
            if (memberRepository.existsById(uuid)) {
                MemberEntity one = memberRepository.getOne(uuid);
                one.setAddress(null);
                one.setClub(null);
                one.setErasedEntity(null);
                one.setWeaponPermission(null);
                one.setBarCodeCardList(null);
                one.setHistory(null);
                one.setLicense(null);
                one.setMemberPermissions(null);
                one.setPersonalEvidence(null);
                one.setShootingPatent(null);
                memberRepository.save(one);
                memberRepository.delete(one);
                return ResponseEntity.ok("usunięto osobę z bazy");
            } else {
                return ResponseEntity.badRequest().body("nie można wykonać polecenia");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error " + e);
        }
    }

    public ResponseEntity<?> getSingleMemberEmail(Integer number) {
        MemberEntity memberEntity = memberRepository.findByLegitimationNumber(number).orElse(null);
        if (memberEntity != null)
            if (!memberEntity.getErased()) return ResponseEntity.ok(memberEntity.getEmail());
            else return ResponseEntity.badRequest().body("Brak takiego Klubowicza");
        else return ResponseEntity.badRequest().body("Brak takiego Klubowicza");

    }
}
