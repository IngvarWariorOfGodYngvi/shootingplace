package com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace;

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

@Service
public class RegistrationRecordsService {

    private final Logger LOG = LogManager.getLogger(getClass());

    private final MemberRepository memberRepo;
    private final RegistrationRecordRepository registrationRepo;
    private final OtherPersonRepository otherPersonRepository;
    private final OtherPersonService otherPersonService;

    public RegistrationRecordsService(MemberRepository memberRepo, RegistrationRecordRepository registrationRepo, OtherPersonRepository otherPersonRepository, OtherPersonService otherPersonService) {
        this.memberRepo = memberRepo;
        this.registrationRepo = registrationRepo;
        this.otherPersonRepository = otherPersonRepository;
        this.otherPersonService = otherPersonService;
    }

    public ResponseEntity<?> createRecordInBook(String pesel, String imageUUID) {
        RegistrationRecordEntity r = new RegistrationRecordEntity();
        MemberEntity member = memberRepo.findByPesel(pesel).orElse(null);
        if (member != null && !member.getErased()) {
            if (registrationRepo.findAll().stream().anyMatch(f ->
                    LocalDate.of(f.getDateTime().getYear(), f.getDateTime().getMonth(), f.getDateTime().getDayOfMonth()).equals(LocalDate.now()) && f.getPeselOrID().equals(member.getPesel()))) {
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

                r.setDateTime(LocalDateTime.now());
                r.setImageUUID(imageUUID);
                r.setPeselOrID(member.getPesel());
                r.setDayIndex(getDayIndex() + 1);
                String name = r.getSecondName() + ' ' + r.getFirstName();
                LOG.info("Zapisano do książki " + name);
                registrationRepo.save(r);
                return ResponseEntity.ok("Zapisano do książki " + name);
            }
        } else {
            return ResponseEntity.badRequest().body("Brak osoby w bazie");
        }
    }

    public ResponseEntity<?> createRecordInBook(String imageUUID, String phone, ImageOtherPersonWrapper other, String club, Boolean rememberMe) {
        phone = phone.replaceAll(" ", "");
        RegistrationRecordEntity r = new RegistrationRecordEntity();
        OtherPersonEntity otherPerson = null;
        if (!phone.isEmpty()) {
            otherPerson = otherPersonRepository.findAllByPhoneNumber(phone.replaceAll(" ", "")).stream().filter(OtherPersonEntity::isActive).findFirst().orElse(null);
        }
        if (otherPerson != null) {
            OtherPersonEntity finalOtherPerson = otherPerson;
            if (registrationRepo.findAll().stream().anyMatch(f ->
                    LocalDate.of(f.getDateTime().getYear(), f.getDateTime().getMonth(), f.getDateTime().getDayOfMonth()).equals(LocalDate.now()) && f.getPeselOrID().equals(String.valueOf(finalOtherPerson.getId())))) {
                LOG.info("Osoba znajduje się już na liście");
                return ResponseEntity.badRequest().body("Osoba znajduje się już na liście");
            } else {
                r.setFirstName(firstLetterToUpperCase(finalOtherPerson.getFirstName()));
                r.setSecondName(finalOtherPerson.getSecondName().toUpperCase(Locale.ROOT));
                r.setDataProcessingAgreement(true);
                r.setStatementOnReadingTheShootingPlaceRegulations(true);
                if (finalOtherPerson.getWeaponPermissionNumber() == null) {
                    r.setAddress(finalOtherPerson.getAddress().toString());
                } else {
                    r.setWeaponPermission(finalOtherPerson.getWeaponPermissionNumber());
                }
                r.setDateTime(LocalDateTime.now());
                r.setImageUUID(imageUUID);
                r.setPeselOrID(String.valueOf(finalOtherPerson.getId()));
                r.setDayIndex(getDayIndex() + 1);
                String name = r.getSecondName() + ' ' + r.getFirstName();
                LOG.info("Zapisano do książki " + name);
                registrationRepo.save(r);
                return ResponseEntity.ok("Zapisano do książki " + name);
            }
        }
        if (other.getOther() != null) {
            if (rememberMe) {
                OtherPersonEntity otherPerson1 = otherPersonService.addPerson(club, other.getOther());
                r.setFirstName(firstLetterToUpperCase(other.getOther().getFirstName()));
                r.setSecondName(otherPerson1.getSecondName().toUpperCase(Locale.ROOT));
                r.setDataProcessingAgreement(false);
                r.setStatementOnReadingTheShootingPlaceRegulations(true);
                String weaponPermissionNumber = otherPerson1.getWeaponPermissionNumber();
                weaponPermissionNumber = weaponPermissionNumber != null ? weaponPermissionNumber.equals("") ? null : weaponPermissionNumber : null;
                if (weaponPermissionNumber != null) {
                    r.setWeaponPermission(weaponPermissionNumber.toUpperCase(Locale.ROOT));
                } else {
                    r.setAddress(otherPerson1.getAddress().fullAddress());
                }
                r.setDateTime(LocalDateTime.now());
                r.setImageUUID(imageUUID);
                r.setPeselOrID(String.valueOf(otherPerson1.getId()));
                r.setDayIndex(getDayIndex() + 1);
            } else {
                r.setFirstName(firstLetterToUpperCase(other.getOther().getFirstName()));
                r.setSecondName(other.getOther().getSecondName().toUpperCase());
                r.setDataProcessingAgreement(false);
                r.setStatementOnReadingTheShootingPlaceRegulations(true);

                String weaponPermissionNumber = other.getOther().getWeaponPermissionNumber();
                weaponPermissionNumber = weaponPermissionNumber != null ? weaponPermissionNumber.equals("") ? null : weaponPermissionNumber : null;

                if (weaponPermissionNumber != null) {
                    r.setWeaponPermission(weaponPermissionNumber.toUpperCase(Locale.ROOT));
                } else {
                    r.setAddress(other.getOther().getAddress().fullAddress());
                }
                r.setDateTime(LocalDateTime.now());
                r.setImageUUID(imageUUID);
                r.setPeselOrID("");
                r.setDayIndex(getDayIndex() + 1);
            }
            String name = r.getSecondName() + ' ' + r.getFirstName();
            LOG.info("Zapisano do książki " + name);
            registrationRepo.save(r);
            return ResponseEntity.ok("Zapisano do książki " + name);
        } else {
            return ResponseEntity.badRequest().body("Brak osoby w bazie");
        }
    }

    private int getDayIndex() {
        RegistrationRecordEntity registrationRecordEntity = registrationRepo.findAll()
                .stream()
                .filter(f -> f.getDateTime().equals(LocalDateTime.now()))
                .max(Comparator.comparing(RegistrationRecordEntity::getDayIndex))
                .orElse(null);
        if (registrationRecordEntity != null) {
            return registrationRecordEntity.getDayIndex();
        }
        return 0;
    }
    private String firstLetterToUpperCase(String string){
       return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    public ResponseEntity<?> getRecordsBetweenDate(LocalDate firstDate, LocalDate secondDate) {
        return ResponseEntity.ok(registrationRepo
                .findAllBeetweenDate(LocalDateTime.of(firstDate,LocalTime.of(0,0)),LocalDateTime.of(secondDate,LocalTime.of(23,59,59)))
                .stream().sorted(Comparator.comparing(RegistrationRecordEntity::getDateTime).reversed()));

    }

    public void setEndTimeToAllRegistrationRecordEntity() {
        List<RegistrationRecordEntity> collect = registrationRepo.findAllByEndDateTimeNull();
        collect.forEach(e -> e.setEndDateTime(LocalDateTime.of(e.getDateTime().toLocalDate(), LocalTime.of(23, 59, 59))));
        LOG.info("Ustawiono datę wyjścia wszystkim na godzinę 23:59:59");
    }
}
