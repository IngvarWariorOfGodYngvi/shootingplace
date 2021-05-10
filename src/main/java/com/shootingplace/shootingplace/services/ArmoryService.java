package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.*;
import com.shootingplace.shootingplace.domain.models.Caliber;
import com.shootingplace.shootingplace.repositories.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private final Logger LOG = LogManager.getLogger();


    public ArmoryService(AmmoEvidenceRepository ammoEvidenceRepository, CaliberService caliberService, CaliberRepository caliberRepository, CaliberUsedRepository caliberUsedRepository, CalibersAddedRepository calibersAddedRepository, GunRepository gunRepository, ChangeHistoryService changeHistoryService, GunStoreRepository gunStoreRepository) {
        this.ammoEvidenceRepository = ammoEvidenceRepository;
        this.caliberService = caliberService;
        this.caliberRepository = caliberRepository;
        this.caliberUsedRepository = caliberUsedRepository;
        this.calibersAddedRepository = calibersAddedRepository;
        this.gunRepository = gunRepository;
        this.changeHistoryService = changeHistoryService;
        this.gunStoreRepository = gunStoreRepository;
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

    public boolean addGunEntity(String modelName,
                                String caliber,
                                String gunType,
                                String serialNumber,
                                String productionYear,
                                String numberOfMagazines,
                                String gunCertificateSerialNumber,
                                String additionalEquipment,
                                String recordInEvidenceBook,
                                String comment,
                                String basisForPurchaseOrAssignment) {

        if ((modelName.isEmpty() || modelName.equals("null")) || (caliber.isEmpty() || caliber.equals("null")) || (gunType.isEmpty() || gunType.equals("null")) || (serialNumber.isEmpty() || serialNumber.equals("null")) || (gunCertificateSerialNumber.isEmpty() || gunCertificateSerialNumber.equals("null")) || (recordInEvidenceBook.isEmpty() || recordInEvidenceBook.equals("null"))) {
            LOG.info("Nie podano wszystkich informacji");
            return false;
        }
        if (productionYear.isEmpty() || productionYear.equals("null")) {
            productionYear = null;
        }
        if (additionalEquipment.isEmpty() || additionalEquipment.equals("null")) {
            additionalEquipment = null;
        }
        if (comment.isEmpty() || comment.equals("null")) {
            comment = null;
        }

        List<GunEntity> all = gunRepository.findAll();

        if (all.stream().anyMatch(e -> e.getGunCertificateSerialNumber().equals(gunCertificateSerialNumber) || e.getSerialNumber().equals(serialNumber) || e.getRecordInEvidenceBook().equals(recordInEvidenceBook))) {
            LOG.info("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
            return false;
        } else {

            GunEntity gunEntity = GunEntity.builder()
                    .modelName(modelName.toUpperCase())
                    .caliber(caliber)
                    .gunType(gunType)
                    .serialNumber(serialNumber.toUpperCase())
                    .productionYear(productionYear)
                    .numberOfMagazines(numberOfMagazines)
                    .gunCertificateSerialNumber(gunCertificateSerialNumber.toUpperCase())
                    .additionalEquipment(additionalEquipment)
                    .recordInEvidenceBook(recordInEvidenceBook)
                    .comment(comment)
                    .basisForPurchaseOrAssignment(basisForPurchaseOrAssignment)
                    .inStock(true).build();

            gunRepository.saveAndFlush(gunEntity);
            GunStoreEntity gunStoreEntity = gunStoreRepository.findAll().stream().filter(f -> f.getTypeName().equals(gunType)).findFirst().orElseThrow(EntityNotFoundException::new);
            List<GunEntity> gunEntityList = gunStoreEntity.getGunEntityList();
            gunEntityList.add(gunEntity);

            gunStoreRepository.saveAndFlush(gunStoreEntity);

            return true;
        }
    }

    public List<String> getGunTypeList() {
        List<GunStoreEntity> all = gunStoreRepository.findAll();
        all.sort(Comparator.comparing(GunStoreEntity::getTypeName));
        List<String> list = new ArrayList<>();
        all.forEach(e->list.add(e.getTypeName()));
        return list;
    }

    public List<GunStoreEntity> getAllGuns() {
        List<GunStoreEntity> all = gunStoreRepository.findAll();
        all.sort(Comparator.comparing(GunStoreEntity::getTypeName));
        return all;
    }

    public boolean editGunEntity(String gunUUID,
                                 String modelName,
                                 String caliber,
                                 String gunType,
                                 String serialNumber,
                                 String productionYear,
                                 String numberOfMagazines,
                                 String gunCertificateSerialNumber,
                                 String additionalEquipment,
                                 String recordInEvidenceBook,
                                 String comment,
                                 String basisForPurchaseOrAssignment) {
        GunEntity gunEntity = gunRepository.findById(gunUUID).orElse(null);
        if (gunEntity == null) {
            return false;
        }

        if (modelName != null && !modelName.isEmpty()) {
            gunEntity.setModelName(modelName);
        }
        if (caliber != null && !caliber.isEmpty()) {
            gunEntity.setCaliber(caliber);
        }
        if (gunType != null && !gunType.isEmpty()) {
            gunEntity.setGunType(gunType);
        }
        if (serialNumber != null && !serialNumber.isEmpty()) {
            gunEntity.setSerialNumber(serialNumber);
        }
        if (productionYear != null && !productionYear.isEmpty()) {
            gunEntity.setProductionYear(productionYear);
        }
        if (numberOfMagazines != null && !numberOfMagazines.isEmpty()) {
            gunEntity.setNumberOfMagazines(numberOfMagazines);
        }
        if (gunCertificateSerialNumber != null && !gunCertificateSerialNumber.isEmpty()) {
            gunEntity.setGunCertificateSerialNumber(gunCertificateSerialNumber);
        }
        if (additionalEquipment != null && !additionalEquipment.isEmpty()) {
            gunEntity.setAdditionalEquipment(additionalEquipment);
        }
        if (recordInEvidenceBook != null && !recordInEvidenceBook.isEmpty()) {
            gunEntity.setRecordInEvidenceBook(recordInEvidenceBook);
        }
        if (comment != null && !comment.isEmpty()) {
            gunEntity.setComment(comment);
        }
        if (basisForPurchaseOrAssignment != null && !basisForPurchaseOrAssignment.isEmpty()) {
            gunEntity.setBasisForPurchaseOrAssignment(basisForPurchaseOrAssignment);
        }
        if (gunEntity.getProductionYear().equals("null")) {
            gunEntity.setProductionYear(null);
        }
        if (gunEntity.getAdditionalEquipment().equals("null")) {
            gunEntity.setAdditionalEquipment(null);
        }
        if (gunEntity.getComment().equals("null")) {
            gunEntity.setComment(null);
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
}
