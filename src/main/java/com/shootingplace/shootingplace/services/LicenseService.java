package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.LicenseEntity;
import com.shootingplace.shootingplace.domain.entities.LicensePaymentHistoryEntity;
import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.models.License;
import com.shootingplace.shootingplace.domain.models.MemberDTO;
import com.shootingplace.shootingplace.repositories.LicensePaymentHistoryRepository;
import com.shootingplace.shootingplace.repositories.LicenseRepository;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class LicenseService {

    @Autowired
    private Clock clock;

    public LocalDate now(){
        return LocalDate.now(clock);
    }

    private final MemberRepository memberRepository;
    private final LicenseRepository licenseRepository;
    private final HistoryService historyService;
    private final ChangeHistoryService changeHistoryService;
    private final LicensePaymentHistoryRepository licensePaymentHistoryRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public LicenseService(MemberRepository memberRepository,
                          LicenseRepository licenseRepository, HistoryService historyService, ChangeHistoryService changeHistoryService, LicensePaymentHistoryRepository licensePaymentHistoryRepository) {
        this.memberRepository = memberRepository;
        this.licenseRepository = licenseRepository;
        this.historyService = historyService;
        this.changeHistoryService = changeHistoryService;
        this.licensePaymentHistoryRepository = licensePaymentHistoryRepository;
    }

    public List<MemberDTO> getMembersNamesAndLicense() {
        List<MemberDTO> list = new ArrayList<>();
        memberRepository.findAll()
                .stream().filter(f -> !f.getErased())
                .forEach(e -> {
                    if (e.getLicense().getNumber() != null && e.getLicense().isValid()) {
                        list.add(Mapping.map2DTO(e));
                    }
                });
        list.sort(Comparator.comparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName));
        return list;
    }

    public List<MemberDTO> getMembersNamesAndLicenseNotValid() {
        List<MemberDTO> list = new ArrayList<>();
        memberRepository.findAll()
                .stream().filter(f -> !f.getErased())
                .forEach(e -> {
                    if (e.getLicense().getNumber() != null && !e.getLicense().isValid()) {
                        list.add(Mapping.map2DTO(e));
                    }
                });
        list.sort(Comparator.comparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName));
        return list;
    }

    public ResponseEntity<?> updateLicense(String memberUUID, License license) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        LicenseEntity licenseEntity = memberEntity.getLicense();
        if (memberEntity.getShootingPatent().getPatentNumber() == null && memberEntity.getAdult()) {
            LOG.info("Brak Patentu");
            return ResponseEntity.badRequest().body("\"Brak Patentu\"");
        }
        if (license.getNumber() != null) {
            boolean match = memberRepository.findAll()
                    .stream()
                    .filter(f -> !f.getErased())
                    .filter(f -> f.getLicense().getNumber() != null)
                    .anyMatch(f -> f.getLicense().getNumber().equals(license.getNumber()));
            if (match && !licenseEntity.getNumber().equals(license.getNumber())) {
                LOG.error("Ktoś już ma taki numer licencji");
                return ResponseEntity.badRequest().body("\"Ktoś już ma taki numer licencji\"");
            } else {
                licenseEntity.setNumber(license.getNumber());
                LOG.info("Dodano numer licencji");
            }
        }
        if (license.getPistolPermission() != null) {
            if (license.getPistolPermission()) {
                historyService.addLicenseHistoryRecord(memberUUID, 0);
                licenseEntity.setPistolPermission(license.getPistolPermission());
                LOG.info("Dodano dyscyplinę : pistolet");
            }
        }
        if (license.getRiflePermission() != null) {
            if (license.getRiflePermission()) {
                historyService.addLicenseHistoryRecord(memberUUID, 1);
                licenseEntity.setRiflePermission(license.getRiflePermission());
                LOG.info("Dodano dyscyplinę : karabin");
            }
        }
        if (license.getShotgunPermission() != null) {
            if (license.getShotgunPermission()) {
                historyService.addLicenseHistoryRecord(memberUUID, 2);
                licenseEntity.setShotgunPermission(license.getShotgunPermission());
                LOG.info("Dodano dyscyplinę : strzelba");
            }
        }
        if (license.getValidThru() != null) {
            licenseEntity.setValidThru(LocalDate.of(license.getValidThru().getYear(), 12, 31));
            if (license.getValidThru().getYear() >= now().getYear()) {
                licenseEntity.setValid(true);
            }
            LOG.info("zaktualizowano datę licencji");
        } else {
            licenseEntity.setValidThru(LocalDate.of(now().getYear(), 12, 31));
            licenseEntity.setValid(true);
            LOG.info("Brak ręcznego ustawienia daty, ustawiono na koniec bieżącego roku " + licenseEntity.getValidThru());
        }
        licenseEntity.setPaid(false);
        licenseRepository.saveAndFlush(licenseEntity);
        LOG.info("Zaktualizowano licencję");
        return ResponseEntity.ok("\"Zaktualizowano licencję\"");
    }

    public ResponseEntity<?> updateLicense(String memberUUID, String number, LocalDate date, String pinCode) {
        if(!memberRepository.existsById(memberUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono Klubowicza\"");
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        LicenseEntity license = memberEntity.getLicense();

        if (number != null && !number.isEmpty() && !number.equals("null")) {

            boolean match = memberRepository.findAll()
                    .stream()
                    .filter(f -> !f.getErased())
                    .filter(f -> f.getLicense().getNumber() != null)
                    .anyMatch(f -> f.getLicense().getNumber().equals(number));

            if (match && !license.getNumber().equals(number)) {
                LOG.error("Ktoś już ma taki numer licencji");
                return ResponseEntity.badRequest().body("\"Ktoś już ma taki numer licencji\"");
            } else {
                license.setNumber(number);
                LOG.info("Dodano numer licencji");
            }

        }
        if (date != null) {
            license.setValidThru(date);
            license.setValid(license.getValidThru().getYear() >= now().getYear());
        }
        licenseRepository.saveAndFlush(license);
        changeHistoryService.addRecordToChangeHistory(pinCode, license.getClass().getSimpleName() + " updateLicense", memberEntity.getUuid());
        return ResponseEntity.ok("\"Poprawiono Licencję\"");
    }

    public ResponseEntity<?> renewLicenseValid(String memberUUID, License license) {

        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        LicenseEntity licenseEntity = memberEntity.getLicense();


        if (licenseEntity.getNumber() != null && licenseEntity.isPaid()) {
            if (now().isAfter(LocalDate.of(licenseEntity.getValidThru().getYear(), 11, 1)) || licenseEntity.getValidThru().isBefore(now())) {
                licenseEntity.setValidThru(LocalDate.of((licenseEntity.getValidThru().getYear() + 1), 12, 31));
                licenseEntity.setValid(licenseEntity.getValidThru().getYear() >= now().getYear());
                if (license.getPistolPermission() != null) {
                    if (!memberEntity.getShootingPatent().getPistolPermission() && memberEntity.getAdult()) {
                        LOG.error("Brak Patentu");
                    }
                    if (license.getPistolPermission() != null && memberEntity.getShootingPatent().getPistolPermission()) {
                        if (!license.getPistolPermission()) {
                            historyService.addLicenseHistoryRecord(memberUUID, 0);
                        }
                        licenseEntity.setPistolPermission(license.getPistolPermission());
                        LOG.info("Dodano dyscyplinę : pistolet");
                    }
                }
                if (license.getRiflePermission() != null) {
                    if (!memberEntity.getShootingPatent().getRiflePermission() && memberEntity.getAdult()) {
                        LOG.error("Brak Patentu");
                    }
                    if (license.getRiflePermission() != null && memberEntity.getShootingPatent().getRiflePermission()) {
                        if (!license.getRiflePermission()) {
                            historyService.addLicenseHistoryRecord(memberUUID, 1);
                        }
                        licenseEntity.setRiflePermission(license.getRiflePermission());
                        LOG.info("Dodano dyscyplinę : karabin");
                    }
                }
                if (license.getShotgunPermission() != null) {
                    if (!memberEntity.getShootingPatent().getShotgunPermission() && memberEntity.getAdult()) {
                        LOG.error("Brak Patentu");
                    }
                    if (license.getShotgunPermission() != null && memberEntity.getShootingPatent().getShotgunPermission()) {
                        if (!license.getShotgunPermission()) {
                            historyService.addLicenseHistoryRecord(memberUUID, 2);
                        }
                        licenseEntity.setShotgunPermission(license.getShotgunPermission());
                        LOG.info("Dodano dyscyplinę : strzelba");
                    }
                }
                licenseEntity.setCanProlong(false);
                licenseEntity.setPaid(false);
                memberEntity.getHistory().setPistolCounter(0);
                memberEntity.getHistory().setRifleCounter(0);
                memberEntity.getHistory().setShotgunCounter(0);
                licenseRepository.saveAndFlush(licenseEntity);
                LOG.info("Przedłużono licencję");
                return ResponseEntity.ok().body("\"Przedłużono licencję\"");

            } else {
                LOG.error("Nie można przedłużyć licencji - należy poczekać do 1 listopada");
                return ResponseEntity.status(403).body("\"Nie można przedłużyć licencji - należy poczekać do 1 listopada\"");
            }
        } else {
            LOG.error("Nie można przedłużyć licencji");
            return ResponseEntity.badRequest().body("\"Nie można przedłużyć licencji\"");
        }
    }

    public ResponseEntity<?> updateLicensePayment(String memberUUID, String paymentUUID, LocalDate date, Integer year, String pinCode) {

        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        LicensePaymentHistoryEntity licensePaymentHistoryEntity = memberEntity.getHistory().getLicensePaymentHistory().stream().filter(f -> f.getUuid().equals(paymentUUID)).findFirst().orElseThrow(EntityNotFoundException::new);

        if (date != null) {
            licensePaymentHistoryEntity.setDate(date);
        }
        if (year != null) {
            licensePaymentHistoryEntity.setValidForYear(year);
        }

        licensePaymentHistoryRepository.saveAndFlush(licensePaymentHistoryEntity);
        changeHistoryService.addRecordToChangeHistory(pinCode, licensePaymentHistoryEntity.getClass().getSimpleName() + " updateLicense", memberEntity.getUuid());

        return ResponseEntity.ok("\"Poprawiono płatność za licencję\"");
    }

    public List<Integer> getMembersQuantity() {

        int count2 = (int) memberRepository.findAll().stream()
                .filter(MemberEntity::getErased)
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(f -> !f.getLicense().isValid())
                .count();

        List<Integer> list = new ArrayList<>();

        int count = (int) memberRepository.findAll().stream()
                .filter(f->!f.getErased())
                .filter(f ->f.getLicense()!=null)
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(f -> !f.getLicense().isValid())
                .count();
        int count1 = (int) memberRepository.findAll().stream()
                .filter(f->!f.getErased())
                .filter(f ->f.getLicense()!=null)
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(f -> f.getLicense().isValid())
                .count();

        list.add(count - count2);
        list.add(count1);

        return list;
    }


    public License getLicense() {
        return License.builder()
                .number(null)
                .validThru(null)
                .pistolPermission(false)
                .riflePermission(false)
                .shotgunPermission(false)
                .isValid(false)
                .canProlong(false)
                .isPaid(false)
                .build();
    }

}