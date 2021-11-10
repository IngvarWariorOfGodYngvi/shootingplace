package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.*;
import com.shootingplace.shootingplace.domain.enums.UsedType;
import com.shootingplace.shootingplace.domain.models.Caliber;
import com.shootingplace.shootingplace.domain.models.Gun;
import com.shootingplace.shootingplace.repositories.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArmoryService {

    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final CaliberService caliberService;
    private final CaliberRepository caliberRepository;
    private final CaliberUsedRepository caliberUsedRepository;
    private final CalibersAddedRepository calibersAddedRepository;
    private final GunRepository gunRepository;
    private final ChangeHistoryService changeHistoryService;
    private final GunStoreRepository gunStoreRepository;
    private final FilesRepository filesRepository;
    private final UsedHistoryRepository usedHistoryRepository;

    private final Logger LOG = LogManager.getLogger();


    public ArmoryService(AmmoEvidenceRepository ammoEvidenceRepository, CaliberService caliberService, CaliberRepository caliberRepository, CaliberUsedRepository caliberUsedRepository, CalibersAddedRepository calibersAddedRepository, GunRepository gunRepository, ChangeHistoryService changeHistoryService, GunStoreRepository gunStoreRepository, FilesRepository filesRepository, UsedHistoryRepository usedHistoryRepository) {
        this.ammoEvidenceRepository = ammoEvidenceRepository;
        this.caliberService = caliberService;
        this.caliberRepository = caliberRepository;
        this.caliberUsedRepository = caliberUsedRepository;
        this.calibersAddedRepository = calibersAddedRepository;
        this.gunRepository = gunRepository;
        this.changeHistoryService = changeHistoryService;
        this.gunStoreRepository = gunStoreRepository;
        this.filesRepository = filesRepository;
        this.usedHistoryRepository = usedHistoryRepository;
    }


    public List<Caliber> getSumFromAllAmmoList(LocalDate firstDate, LocalDate secondDate) {
        List<Caliber> list = new ArrayList<>();
        List<CaliberEntity> calibersList = caliberService.getCalibersList();
        calibersList.forEach(e ->
                list.add(Mapping.map(e))
        );

        List<Caliber> list1 = new ArrayList<>();
        list.forEach(e -> ammoEvidenceRepository.findAll().stream()
                .filter(f -> f.getDate().isAfter(firstDate.minusDays(1)))
                .filter(f -> f.getDate().isBefore(secondDate.plusDays(1)))
                .forEach(g -> g.getAmmoInEvidenceEntityList()
                        .stream().filter(f -> f.getCaliberName().equals(e.getName()))
                        .forEach(h -> {
                            Caliber caliber = list.stream()
                                    .filter(f -> f.getName().equals(e.getName())).findFirst().orElseThrow(EntityNotFoundException::new);
                            if (caliber.getQuantity() == null) {
                                caliber.setQuantity(0);
                            }
                            if (list1.stream().anyMatch(f -> f.getName().equals(caliber.getName()))) {
                                Caliber caliber1 = list1.stream().filter(f -> f.getName().equals(caliber.getName())).findFirst().orElseThrow(EntityNotFoundException::new);
                                caliber.setQuantity(caliber1.getQuantity() + h.getQuantity());
                            } else {

                                caliber.setQuantity(h.getQuantity());
                                list1.add(caliber);
                            }

                        })
                ));

        return list1;


    }

    public void updateAmmo(String caliberUUID, Integer count, LocalDate date, String description) {
        CaliberEntity caliberEntity = caliberRepository.findById(caliberUUID).orElseThrow(EntityNotFoundException::new);
        if (caliberEntity.getQuantity() == null) {
            caliberEntity.setQuantity(0);
        }

        CalibersAddedEntity calibersAddedEntity = CalibersAddedEntity.builder()
                .ammoAdded(count)
                .belongTo(caliberUUID)
                .caliberName(caliberEntity.getName())
                .date(date)
                .description(description)
                .stateForAddedDay(caliberEntity.getQuantity())
                .finalStateForAddedDay(caliberEntity.getQuantity() + count)
                .build();
        calibersAddedRepository.save(calibersAddedEntity);

        List<CalibersAddedEntity> ammoAdded = caliberEntity.getAmmoAdded();
        ammoAdded.add(calibersAddedEntity);
        if (caliberEntity.getQuantity() == null) {
            caliberEntity.setQuantity(0);
        }
        caliberEntity.setQuantity(caliberEntity.getQuantity() + calibersAddedEntity.getAmmoAdded());

        caliberRepository.save(caliberEntity);

    }

    public boolean substratAmmo(String caliberUUID, Integer quantity) {

        CaliberEntity caliberEntity = caliberRepository.findById(caliberUUID).orElseThrow(EntityNotFoundException::new);
        if (caliberEntity.getQuantity() - quantity < 0) {
            return false;
        }
        CaliberUsedEntity caliberUsedEntity = CaliberUsedEntity.builder()
                .date(LocalDate.now())
                .belongTo(caliberUUID)
                .ammoUsed(quantity)
                .build();
        caliberUsedRepository.saveAndFlush(caliberUsedEntity);

        List<CaliberUsedEntity> ammoUsed = caliberEntity.getAmmoUsed();

        ammoUsed.add(caliberUsedEntity);
        caliberEntity.setQuantity(caliberEntity.getQuantity() - caliberUsedEntity.getAmmoUsed());
        caliberRepository.saveAndFlush(caliberEntity);

        return true;
    }

    public List<CalibersAddedEntity> getHistoryOfCaliber(String caliberUUID) {
        CaliberEntity caliberEntity = caliberRepository.findById(caliberUUID).orElseThrow(EntityNotFoundException::new);
        List<CalibersAddedEntity> ammoAdded = caliberEntity.getAmmoAdded();
        ammoAdded.sort(Comparator.comparing(CalibersAddedEntity::getDate));
        return ammoAdded;

    }

    public boolean addGunEntity(Gun gun) {

        if ((gun.getModelName().isEmpty() || gun.getModelName().equals("null")) || (gun.getCaliber().isEmpty() || gun.getCaliber().equals("null")) || (gun.getGunType().isEmpty() || gun.getGunType().equals("null")) || (gun.getSerialNumber().isEmpty() || gun.getSerialNumber().equals("null"))) {
            LOG.info("Nie podano wszystkich informacji");
            return false;
        }
        if (gun.getProductionYear().isEmpty() || gun.getProductionYear().equals("null")) {
            gun.setProductionYear(null);
        }
        if (gun.getAdditionalEquipment().isEmpty() || gun.getAdditionalEquipment().equals("null")) {
            gun.setAdditionalEquipment(null);
        }
        if (gun.getComment().isEmpty() || gun.getComment().equals("null")) {
            gun.setComment(null);
        }

        List<GunEntity> all = gunRepository.findAll();

        if (gun.getGunCertificateSerialNumber() != null) {
            if (all.stream().filter(f -> f.getGunCertificateSerialNumber() != null).anyMatch(f -> f.getGunCertificateSerialNumber().equals(gun.getGunCertificateSerialNumber()))) {
                LOG.info("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
                return false;
            }
        }
        if (gun.getSerialNumber() != null) {
            if (all.stream().filter(f -> f.getSerialNumber() != null).anyMatch(e -> e.getSerialNumber().equals(gun.getSerialNumber()))) {
                LOG.info("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
                return false;
            }
        }
        if (gun.getRecordInEvidenceBook() != null) {
            if (all.stream().filter(f -> f.getRecordInEvidenceBook() != null).anyMatch(e -> e.getRecordInEvidenceBook().equals(gun.getRecordInEvidenceBook()))) {
                LOG.info("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
                return false;
            }
        }
        if (gun.getBarcode() != null) {
            if (all.stream().filter(f -> f.getBarcode() != null).anyMatch(e -> e.getBarcode().equals(gun.getBarcode()))) {
                LOG.info("Nie można Nadać kodu kreskowego gdyż jest przypisany już gdzieś indziej");
                return false;
            }
        }

        String s = gun.getGunCertificateSerialNumber();
        if (gun.getGunCertificateSerialNumber() != null) {
            s = s.toUpperCase();
        }

        GunEntity gunEntity = GunEntity.builder()
                .modelName(gun.getModelName().toUpperCase())
                .caliber(gun.getCaliber())
                .gunType(gun.getGunType())
                .serialNumber(gun.getSerialNumber().toUpperCase())
                .productionYear(gun.getProductionYear())
                .numberOfMagazines(gun.getNumberOfMagazines())
                .gunCertificateSerialNumber(s)
                .additionalEquipment(gun.getAdditionalEquipment())
                .recordInEvidenceBook(gun.getRecordInEvidenceBook())
                .comment(gun.getComment())
                .basisForPurchaseOrAssignment(gun.getBasisForPurchaseOrAssignment())
                .barcode(gun.getBarcode())
                .inStock(true).build();

        gunRepository.saveAndFlush(gunEntity);
        GunStoreEntity gunStoreEntity = gunStoreRepository.findAll().stream().filter(f -> f.getTypeName().equals(gun.getGunType())).findFirst().orElseThrow(EntityNotFoundException::new);
        List<GunEntity> gunEntityList = gunStoreEntity.getGunEntityList();
        gunEntityList.add(gunEntity);

        gunStoreRepository.saveAndFlush(gunStoreEntity);

        return true;


    }

    public List<String> getGunTypeList() {
        List<GunStoreEntity> all = gunStoreRepository.findAll();
        all.sort(Comparator.comparing(GunStoreEntity::getTypeName));
        List<String> list = new ArrayList<>();
        all.forEach(e -> list.add(e.getTypeName()));
        return list;
    }

    public List<GunStoreEntity> getAllGuns() {
        List<GunStoreEntity> all = gunStoreRepository.findAll();
        all.sort(Comparator.comparing(GunStoreEntity::getTypeName));
        return all;
    }

    public boolean editGunEntity(Gun gun) {
        GunEntity gunEntity = gunRepository.findById(gun.getUuid()).orElse(null);
        if (gunEntity == null) {
            return false;
        }

        List<GunEntity> collect = gunRepository.findAll().stream().filter(f -> !f.getUuid().equals(gunEntity.getUuid())).collect(Collectors.toList());

        if (collect.stream().anyMatch(f -> f.getSerialNumber().equals(gun.getSerialNumber()) || f.getGunCertificateSerialNumber().equals(gun.getGunCertificateSerialNumber()) || f.getRecordInEvidenceBook().equals(gun.getRecordInEvidenceBook()))) {
            return false;
        }
        if (gun.getModelName() != null && !gun.getModelName().isEmpty()) {
            gunEntity.setModelName(gun.getModelName());
        }
        if (gun.getCaliber() != null && !gun.getCaliber().isEmpty()) {
            gunEntity.setCaliber(gun.getCaliber());
        }
        if (gun.getGunType() != null && !gun.getGunType().isEmpty()) {
            gunEntity.setGunType(gun.getGunType());
        }
        if (gun.getSerialNumber() != null && !gun.getSerialNumber().isEmpty()) {
            gunEntity.setSerialNumber(gun.getSerialNumber());
        }
        if (gun.getProductionYear() != null && !gun.getProductionYear().isEmpty()) {
            gunEntity.setProductionYear(gun.getProductionYear());
        }
        if (gun.getNumberOfMagazines() != null && !gun.getNumberOfMagazines().isEmpty()) {
            gunEntity.setNumberOfMagazines(gun.getNumberOfMagazines());
        }
        if (gun.getGunCertificateSerialNumber() != null && !gun.getGunCertificateSerialNumber().isEmpty()) {
            gunEntity.setGunCertificateSerialNumber(gun.getGunCertificateSerialNumber());
        }
        if (gun.getAdditionalEquipment() != null && !gun.getAdditionalEquipment().isEmpty()) {
            gunEntity.setAdditionalEquipment(gun.getAdditionalEquipment());
        }
        if (gun.getRecordInEvidenceBook() != null && !gun.getRecordInEvidenceBook().isEmpty()) {
            gunEntity.setRecordInEvidenceBook(gun.getRecordInEvidenceBook());
        }
        if (gun.getComment() != null && !gun.getComment().isEmpty()) {
            gunEntity.setComment(gun.getComment());
        }
        if (gun.getBasisForPurchaseOrAssignment() != null && !gun.getBasisForPurchaseOrAssignment().isEmpty()) {
            gunEntity.setBasisForPurchaseOrAssignment(gun.getBasisForPurchaseOrAssignment());
        }
        if (gun.getBarcode() != null && !gun.getBarcode().isEmpty()) {
            gunEntity.setBarcode(gun.getBarcode());
        }
        gunRepository.saveAndFlush(gunEntity);
        return true;
    }

    public boolean removeGun(String gunUUID, String pinCode) {

        GunEntity gunEntity = gunRepository.findById(gunUUID).orElse(null);
        if (gunEntity == null) {
            return false;
        }
        gunEntity.setInStock(false);
        gunRepository.saveAndFlush(gunEntity);
        GunStoreEntity gunStoreEntity = gunStoreRepository.findAll().stream().filter(f -> f.getTypeName().equals(gunEntity.getGunType())).findFirst().orElseThrow(EntityNotFoundException::new);

        List<GunEntity> gunEntityList = gunStoreEntity.getGunEntityList();
        gunEntityList.add(gunEntity);
        gunStoreRepository.saveAndFlush(gunStoreEntity);

        changeHistoryService.addRecordToChangeHistory(pinCode, gunEntity.getClass().getSimpleName() + " removeGun", gunEntity.getUuid());
        return true;
    }

    public boolean createNewGunStore(String nameType) {
        GunStoreEntity gunStoreEntity = gunStoreRepository.findAll().stream().filter(f -> f.getTypeName().equals(nameType)).findFirst().orElse(null);
        if (gunStoreEntity == null) {
            List<GunEntity> collect = gunRepository.findAll().stream().filter(f -> f.getGunType().equals(nameType)).collect(Collectors.toList());

            String[] s1 = nameType.split(" ");
            StringBuilder name = new StringBuilder();
            for (String value : s1) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                name.append(splinted);
            }

            GunStoreEntity build = GunStoreEntity.builder()
                    .typeName(name.toString())
                    .gunEntityList(collect)
                    .build();
            LOG.info("dodaję nowy rodzaj broni");
            gunStoreRepository.saveAndFlush(build);
            return true;
        } else {
            LOG.info("nie dodaję nowego rodzaju broni");
            return false;
        }
    }

    public boolean addImageToGun(String gunUUID, String fileUUID) {
        GunEntity gunEntity = gunRepository.findById(gunUUID).orElseThrow(EntityNotFoundException::new);
        FilesEntity filesEntity = filesRepository.findById(fileUUID).orElseThrow(EntityNotFoundException::new);

        gunEntity.setImgUUID(filesEntity.getUuid());
        gunRepository.saveAndFlush(gunEntity);
        return true;
    }

    public ResponseEntity<?> findGunByBarcode(String barcode) {

        if (gunRepository.findByBarcode(barcode).isEmpty()) {
            return ResponseEntity.badRequest().body("\"Nie znaleziono broni\"");
        }

        GunEntity gunEntity = gunRepository.findByBarcode(barcode).orElseThrow(EntityNotFoundException::new);

        return ResponseEntity.ok(gunEntity);
    }

    public ResponseEntity<?> getListGunsUsedInDate(LocalDate date) {
        return ResponseEntity.ok(usedHistoryRepository.findAll().stream().filter(f -> f.getDate().equals(date)).collect(Collectors.toList()));
    }

    public String addUseToGun(String barcode, String evidenceUUID) {
        AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.findById(evidenceUUID).orElse(null);
        GunEntity gunEntity = gunRepository.findByBarcode(barcode).orElse(null);
        String response = "";
        if (gunEntity == null) {
            response = "Nie znaleziono broni";
            return response;
        }
        if (ammoEvidenceEntity == null) {
            response = "Nie znaleziono listy";
            return response;
        }
        List<UsedHistoryEntity> usedHistoryEntityList = gunEntity.getUsedHistoryEntityList();
        boolean anyMatch = usedHistoryEntityList.stream().filter(f -> f.getEvidenceUUID() != null).anyMatch(f -> f.getEvidenceUUID().equals(evidenceUUID));
        if (anyMatch) {
            response = "Broń znajduje się już na liście";
        } else {
            UsedHistoryEntity build = UsedHistoryEntity.builder()
                    .date(ammoEvidenceEntity.getDate())
                    .gunSerialNumber(gunEntity.getSerialNumber())
                    .gunUUID(gunEntity.getUuid())
                    .evidenceUUID(evidenceUUID)
                    .gunName(gunEntity.getModelName())
                    .usedType(UsedType.TRAINING.getName())
                    .build();
            UsedHistoryEntity save = usedHistoryRepository.save(build);
            usedHistoryEntityList.add(save);
            gunEntity.setUsedHistoryEntityList(usedHistoryEntityList);
            gunRepository.saveAndFlush(gunEntity);
            response = "Dodano Broń";
        }
        return response;
    }

    public List<UsedHistoryEntity> getGunUsedHistory(String gunUUID) {

        return usedHistoryRepository.findAll().stream().filter(f -> f.getGunUUID().equals(gunUUID)).sorted(Comparator.comparing(UsedHistoryEntity::getDate).reversed()).collect(Collectors.toList());

    }

    public ResponseEntity<?> addUsedHistoryToGun(String barcode) {
        GunEntity gunEntity = gunRepository.findByBarcode(barcode).orElse(null);
        if (gunEntity == null) {
            return ResponseEntity.badRequest().body("\"Nie znaleziono broni\"");
        } else {
            List<UsedHistoryEntity> usedHistoryEntityList1 = gunEntity.getUsedHistoryEntityList();
            if (usedHistoryEntityList1.stream().anyMatch(f -> f.getDate().equals(LocalDate.now()))) {
                UsedHistoryEntity usedHistoryEntity = usedHistoryEntityList1.stream().filter(f -> f.getDate().equals(LocalDate.now())).findFirst().orElse(null);
                if (usedHistoryEntity != null && usedHistoryEntity.getUsedType().equals(UsedType.CLEANING.getName()))
                    LOG.info("Broń była już dzisiaj czyszczona");
                    return ResponseEntity.badRequest().body("\"Broń była już dzisiaj czyszczona\"");
            }
            List<UsedHistoryEntity> usedHistoryEntityList = gunEntity.getUsedHistoryEntityList();
            UsedHistoryEntity build = UsedHistoryEntity.builder()
                    .date(LocalDate.now())
                    .gunSerialNumber(gunEntity.getSerialNumber())
                    .gunUUID(gunEntity.getUuid())
                    .gunName(gunEntity.getModelName())
                    .evidenceUUID(null)
                    .usedType(UsedType.CLEANING.getName())
                    .build();
            UsedHistoryEntity save = usedHistoryRepository.save(build);
            usedHistoryEntityList.add(save);
            gunEntity.setUsedHistoryEntityList(usedHistoryEntityList);
            gunRepository.save(gunEntity);
            LOG.info("Uznano broń za wyczyszczoną");
            return ResponseEntity.ok("\"Uznano broń za wyczyszczoną\"");
        }
    }

    public ResponseEntity<?> addUsedHistoryToGunInTournament(String barcode, String tournamentUUID) {
        GunEntity gunEntity = gunRepository.findByBarcode(barcode).orElse(null);
        if (gunEntity == null) {
            return ResponseEntity.badRequest().body("\"Nie znaleziono broni\"");
        } else {
            List<UsedHistoryEntity> usedHistoryEntityList1 = gunEntity.getUsedHistoryEntityList();
            if (usedHistoryEntityList1.stream().anyMatch(f -> f.getDate().equals(LocalDate.now()))) {
                UsedHistoryEntity usedHistoryEntity = usedHistoryEntityList1.stream().filter(f -> f.getDate().equals(LocalDate.now())).findFirst().orElse(null);
                if (usedHistoryEntity != null && usedHistoryEntity.getUsedType().equals(UsedType.CLEANING.getName()))
                    LOG.info("Broń jest już Dodana");
                return ResponseEntity.badRequest().body("\"Broń jest już dodana\"");
            }
            List<UsedHistoryEntity> usedHistoryEntityList = gunEntity.getUsedHistoryEntityList();
            UsedHistoryEntity build = UsedHistoryEntity.builder()
                    .date(LocalDate.now())
                    .gunSerialNumber(gunEntity.getSerialNumber())
                    .gunUUID(gunEntity.getUuid())
                    .gunName(gunEntity.getModelName())
                    .evidenceUUID(tournamentUUID)
                    .usedType(UsedType.CLUB_COMPETITION.getName())
                    .build();
            UsedHistoryEntity save = usedHistoryRepository.save(build);
            usedHistoryEntityList.add(save);
            gunEntity.setUsedHistoryEntityList(usedHistoryEntityList);
            gunRepository.save(gunEntity);
            LOG.info("Broń została dodana");
            return ResponseEntity.ok("\"Broń została dodana\"");
        }
    }

    public List<UsedHistoryEntity> getHistoryGuns(LocalDate firstDate, LocalDate secondDate) {
        List<UsedHistoryEntity> all = usedHistoryRepository.findAll();
        return all.stream()
                .filter(f -> f.getDate().isAfter(firstDate.minusDays(1)))
                .filter(f -> f.getDate().isBefore(secondDate.plusDays(1))).sorted(Comparator.comparing(UsedHistoryEntity::getDate).reversed()).collect(Collectors.toList());
    }

    public List<UsedHistoryEntity> getGunInTournament(String tournamentUUID) {
        return usedHistoryRepository.findAll()
                .stream()
                .filter(f->f.getEvidenceUUID()!=null)
                .filter(f->f.getEvidenceUUID().equals(tournamentUUID))
                .collect(Collectors.toList());
    }
}
