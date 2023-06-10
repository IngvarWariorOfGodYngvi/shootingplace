package com.shootingplace.shootingplace.BookOfRegistrationOfStayAtTheShootingPlace;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonService;
import com.shootingplace.shootingplace.wrappers.ImageOtherPersonWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class RegistrationRecordsService {

    private final Logger LOG = LogManager.getLogger(getClass());

    private final MemberRepository memberRepo;
    private final RegistrationRecordRepository registrationRepo;
    private final OtherPersonRepository othersRepo;
    private final OtherPersonService otherPersonService;

    public RegistrationRecordsService(MemberRepository memberRepo, RegistrationRecordRepository registrationRepo, OtherPersonRepository othersRepo, OtherPersonService otherPersonService) {
        this.memberRepo = memberRepo;
        this.registrationRepo = registrationRepo;
        this.othersRepo = othersRepo;
        this.otherPersonService = otherPersonService;
    }

    public ResponseEntity<?> createRecordInBook(String pesel, String imageUUID) {
        RegistrationRecordEntity r = new RegistrationRecordEntity();
        MemberEntity member = memberRepo.findByPesel(pesel).orElse(null);
        if (member != null && !member.getErased()) {
            boolean b = registrationRepo.findAll().stream().anyMatch(f ->
                    LocalDate.of(f.getDate().getYear(), f.getDate().getMonth(), f.getDate().getDayOfMonth()).equals(LocalDate.now()) && f.getPeselOrID().equals(member.getPesel())
            );
            if (b) {
                LOG.info("Osoba znajduje się już na liście");
                return ResponseEntity.badRequest().body("Osoba znajduje się już na liście");
            } else {
                r.setFirstName(member.getFirstName());
                r.setSecondName(member.getSecondName());
                r.setDataProcessingAgreement(true);
                r.setStatementOnReadingTheShootingPlaceRegulations(true);

                if (member.getWeaponPermission().getNumber() == null) {
                    r.setAddress(member.getAddress().toString());
                } else {
                    r.setWeaponPermission(member.getWeaponPermission().getNumber());
                }

                r.setDate(LocalDateTime.now());
                r.setImageUUID(imageUUID);
                r.setPeselOrID(member.getPesel());
                int dayIndex = 0;
                RegistrationRecordEntity registrationRecordEntity = registrationRepo.findAll().stream().filter(f -> f.getDate().equals(LocalDateTime.now())).max(Comparator.comparing(RegistrationRecordEntity::getDayIndex)).orElse(null);
                if (registrationRecordEntity != null) {
                    dayIndex = registrationRecordEntity.getDayIndex();
                }
                r.setDayIndex(dayIndex + 1);
                LOG.info("Zapisano do książki");
                registrationRepo.save(r);
                return ResponseEntity.ok("Zapisano do książki");
            }
        } else {
            return ResponseEntity.badRequest().body("Brak osoby w bazie");
        }
    }

    public ResponseEntity<?> createRecordInBook(String imageUUID, String phone, ImageOtherPersonWrapper other, String club, Boolean rememberMe) {
        phone = phone.replaceAll(" ", "");
        RegistrationRecordEntity r = new RegistrationRecordEntity();
        OtherPersonEntity otherPerson = null;
        System.out.println("phone : " + phone);
        System.out.println("first : " + other.getOther().getFirstName());
        if (!phone.isEmpty()) {
            otherPerson = othersRepo.findByPhoneNumber(phone).orElse(null);
        }
        if (otherPerson != null) {
            OtherPersonEntity finalOtherPerson = otherPerson;
            boolean b = registrationRepo.findAll().stream().anyMatch(f ->
                    LocalDate.of(f.getDate().getYear(), f.getDate().getMonth(), f.getDate().getDayOfMonth()).equals(LocalDate.now()) && f.getPeselOrID().equals(String.valueOf(finalOtherPerson.getId())));
            if (b) {
                LOG.info("Osoba znajduje się już na liście");
                return ResponseEntity.badRequest().body("Osoba znajduje się już na liście");
            } else {
                r.setFirstName(otherPerson.getFirstName());
                r.setSecondName(otherPerson.getSecondName());
                r.setDataProcessingAgreement(true);
                r.setStatementOnReadingTheShootingPlaceRegulations(true);

                if (otherPerson.getWeaponPermissionNumber() == null) {
                    r.setAddress(otherPerson.getAddress().toString());
                } else {
                    r.setWeaponPermission(otherPerson.getWeaponPermissionNumber());
                }

                r.setDate(LocalDateTime.now());
                r.setImageUUID(imageUUID);
                r.setPeselOrID(String.valueOf(otherPerson.getId()));
                int dayIndex = 0;
                RegistrationRecordEntity registrationRecordEntity = registrationRepo.findAll().stream().filter(f -> f.getDate().equals(LocalDateTime.now())).max(Comparator.comparing(RegistrationRecordEntity::getDayIndex)).orElse(null);
                if (registrationRecordEntity != null) {
                    dayIndex = registrationRecordEntity.getDayIndex();
                }
                r.setDayIndex(dayIndex + 1);
                LOG.info("Zapisano do książki");
                registrationRepo.save(r);
                return ResponseEntity.ok("Zapisano do książki");
            }
        }
        if (other.getOther() != null) {
            if (rememberMe) {
                    OtherPersonEntity otherPerson1 = otherPersonService.addPerson(club, other.getOther());

                r.setFirstName(otherPerson1.getFirstName());
                r.setSecondName(otherPerson1.getSecondName());
                r.setDataProcessingAgreement(false);
                r.setStatementOnReadingTheShootingPlaceRegulations(true);
                String weaponPermissionNumber = otherPerson1.getWeaponPermissionNumber();
                weaponPermissionNumber = weaponPermissionNumber!=null? weaponPermissionNumber.equals("")? null : weaponPermissionNumber : null;

                if (weaponPermissionNumber != null) {
                    r.setWeaponPermission(weaponPermissionNumber.toUpperCase(Locale.ROOT));
                } else {
                    r.setAddress(otherPerson1.getAddress().fullAddress());
                }

                r.setDate(LocalDateTime.now());
                r.setImageUUID(imageUUID);
                r.setPeselOrID(String.valueOf(otherPerson1.getId()));
                int dayIndex = 0;
                RegistrationRecordEntity registrationRecordEntity = registrationRepo.findAll().stream().filter(f -> f.getDate().equals(LocalDateTime.now())).max(Comparator.comparing(RegistrationRecordEntity::getDayIndex)).orElse(null);
                if (registrationRecordEntity != null) {
                    dayIndex = registrationRecordEntity.getDayIndex();
                }
                r.setDayIndex(dayIndex + 1);
            } else {
                r.setFirstName(other.getOther().getFirstName().substring(0, 1).toUpperCase() + other.getOther().getFirstName().substring(1).toLowerCase());
                r.setSecondName(other.getOther().getSecondName().toUpperCase());
                r.setDataProcessingAgreement(false);
                r.setStatementOnReadingTheShootingPlaceRegulations(true);

                String weaponPermissionNumber = other.getOther().getWeaponPermissionNumber();
                weaponPermissionNumber = weaponPermissionNumber!=null? weaponPermissionNumber.equals("")? null : weaponPermissionNumber : null;

                if (weaponPermissionNumber != null) {
                    r.setWeaponPermission(weaponPermissionNumber.toUpperCase(Locale.ROOT));
                } else {
                    r.setAddress(other.getOther().getAddress().fullAddress());
                }
                r.setDate(LocalDateTime.now());
                r.setImageUUID(imageUUID);
                r.setPeselOrID("");
                int dayIndex = 0;
                RegistrationRecordEntity registrationRecordEntity = registrationRepo.findAll().stream().filter(f -> f.getDate().equals(LocalDateTime.now())).max(Comparator.comparing(RegistrationRecordEntity::getDayIndex)).orElse(null);
                if (registrationRecordEntity != null) {
                    dayIndex = registrationRecordEntity.getDayIndex();
                }
                r.setDayIndex(dayIndex + 1);
                r.setDayIndex(dayIndex + 1);
            }
            LOG.info("Zapisano do książki");
            registrationRepo.save(r);
            return ResponseEntity.ok("Zapisano do książki");
        } else {
            return ResponseEntity.badRequest().body("Brak osoby w bazie");
        }
    }

    public ResponseEntity<?> getRecordsFromDate(LocalDate firstDate, LocalDate secondDate) {
        List<RegistrationRecordEntity> collect = registrationRepo.findAll().stream().filter(f -> f.getDate().toLocalDate().isAfter(firstDate.minusDays(1)) && f.getDate().toLocalDate().isBefore(secondDate.plusDays(1))).sorted(Comparator.comparing(RegistrationRecordEntity::getDate).reversed()).collect(Collectors.toList());
        return ResponseEntity.ok(collect);

    }

    public void setEndTimeToAllRegistrationRecordEntity() {
        List<RegistrationRecordEntity> collect = registrationRepo.findAllByEndDateTimeNull();
        collect.forEach(e-> {
            LocalDate localDate = e.getDateTime().toLocalDate();
            LocalTime localTime = LocalTime.of(20, 0, 0);
            LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
            e.setEndDateTime(localDateTime);
        });
        LOG.info("Ustawiono datę wyjścia wszystkim na godzinę 20:00");
    }
}
