package com.shootingplace.shootingplace.BookOfRegistrationOfStayAtTheShootingPlace;

import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegistrationRecordsService {

    private final Logger LOG = LogManager.getLogger(getClass());

    private final MemberRepository memberRepo;
    private final RegistrationRecordRepository registrationRepo;
    private final OtherPersonRepository othersRepo;
    private final ClubRepository clubRepo;

    public RegistrationRecordsService(MemberRepository memberRepo, RegistrationRecordRepository registrationRepo, OtherPersonRepository othersRepo, ClubRepository clubRepo) {
        this.memberRepo = memberRepo;
        this.registrationRepo = registrationRepo;
        this.othersRepo = othersRepo;
        this.clubRepo = clubRepo;
    }

    public ResponseEntity<?> createRecordInBook(Integer legitimationNumber, int otherID) {
        System.out.println(legitimationNumber);
        System.out.println(otherID);
        String licenseNumber = clubRepo.getOne(1).getLicenseNumber();
        RegistrationRecordEntity r = new RegistrationRecordEntity();

        if (legitimationNumber > 0) {
            MemberEntity member = memberRepo.findByLegitimationNumber(legitimationNumber).orElseThrow(EntityNotFoundException::new);
            boolean b = registrationRepo.findAll().stream().anyMatch(f -> f.getDate().equals(LocalDate.now()) && f.getPeselOrID().equals(member.getPesel()));
            if (b) {
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
                r.setImageUUID(null);
                r.setPeselOrID(member.getPesel());
                int dayIndex = 0;
                RegistrationRecordEntity registrationRecordEntity = registrationRepo.findAll().stream().filter(f->f.getDate().equals(LocalDate.now())).max(Comparator.comparing(RegistrationRecordEntity::getDayIndex)).orElse(null);
                if (registrationRecordEntity != null) {
                    dayIndex = registrationRecordEntity.getDayIndex();
                }                r.setDayIndex(dayIndex + 1);
                registrationRepo.save(r);
            }
        } else {
            OtherPersonEntity o = othersRepo.findById(otherID).orElse(null);
            if (o != null) {
                r.setPeselOrID(String.valueOf(o.getId()));
                int dayIndex = getDayIndex();

                r.setDayIndex(dayIndex + 1);
                LocalDateTime now = LocalDateTime.now();
                System.out.println(now);
                r.setDate(LocalDateTime.now());
                r.setFirstName(o.getFirstName());
                r.setSecondName(o.getSecondName());
                r.setImageUUID(null);
                r.setWeaponPermission(null);
                r.setStatementOnReadingTheShootingPlaceRegulations(true);
                r.setDataProcessingAgreement(true);
                r.setAddress(licenseNumber);
                registrationRepo.save(r);
                return ResponseEntity.ok("Zapisano do książki");
            }
        }
        return ResponseEntity.badRequest().body("Coś poszło nie tak");
    }

    private int getDayIndex() {
        int dayIndex = 0;
        RegistrationRecordEntity registrationRecordEntity = registrationRepo.findAll().stream().filter(f->f.getDate().toLocalDate().equals(LocalDateTime.now().toLocalDate())).max(Comparator.comparing(RegistrationRecordEntity::getDayIndex)).orElse(null);
        if (registrationRecordEntity != null) {
            dayIndex = registrationRecordEntity.getDayIndex();
        }
        return dayIndex;
    }

    public ResponseEntity<?> getRecordsFromDate(LocalDate date) {
        List<RegistrationRecordEntity> collect = registrationRepo.findAll().stream().filter(f -> f.getDate().toLocalDate().equals(date)).sorted(Comparator.comparing(RegistrationRecordEntity::getDayIndex)).collect(Collectors.toList());
        return ResponseEntity.ok(collect);

    }
}
