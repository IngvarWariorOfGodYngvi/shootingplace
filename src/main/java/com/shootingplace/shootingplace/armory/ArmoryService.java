package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.enums.UsedType;
import com.shootingplace.shootingplace.file.FilesEntity;
import com.shootingplace.shootingplace.file.FilesRepository;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.history.UsedHistoryEntity;
import com.shootingplace.shootingplace.history.UsedHistoryRepository;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
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
    private final MemberRepository memberRepository;
    private final OtherPersonRepository otherPersonRepository;

    private final Logger LOG = LogManager.getLogger();


    public ArmoryService(AmmoEvidenceRepository ammoEvidenceRepository, CaliberService caliberService, CaliberRepository caliberRepository, CaliberUsedRepository caliberUsedRepository, CalibersAddedRepository calibersAddedRepository, GunRepository gunRepository, ChangeHistoryService changeHistoryService, GunStoreRepository gunStoreRepository, FilesRepository filesRepository, UsedHistoryRepository usedHistoryRepository, MemberRepository memberRepository, OtherPersonRepository otherPersonRepository) {
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
        this.memberRepository = memberRepository;
        this.otherPersonRepository = otherPersonRepository;
    }

    public List<Caliber> getSumFromAllAmmoList(LocalDate firstDate, LocalDate secondDate) {
        List<Caliber> list = new ArrayList<>();
        List<CaliberEntity> calibersList = caliberService.getCalibersEntityList();
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
        CaliberEntity caliberEntity = caliberRepository.getOne(caliberUUID);
        if (caliberEntity.getQuantity() == null) {
            caliberEntity.setQuantity(0);
        }
        int caliberAmmoInStore = caliberService.getCaliberAmmoInStore(caliberUUID);
        CalibersAddedEntity calibersAddedEntity = CalibersAddedEntity.builder()
                .ammoAdded(count)
                .belongTo(caliberUUID)
                .caliberName(caliberEntity.getName())
                .date(date)
                .description(description)
                .stateForAddedDay(caliberAmmoInStore)
                .finalStateForAddedDay(caliberAmmoInStore + count)
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

    public void substratAmmo(String caliberUUID, Integer quantity) {
        CaliberEntity caliberEntity = caliberRepository.getOne(caliberUUID);
        CaliberUsedEntity caliberUsedEntity = CaliberUsedEntity.builder()
                .date(LocalDate.now())
                .time(LocalTime.now())
                .belongTo(caliberUUID)
                .ammoUsed(quantity)
                .unitPrice(caliberEntity.getUnitPrice())
                .build();
        caliberUsedRepository.save(caliberUsedEntity);
        List<CaliberUsedEntity> ammoUsed = caliberEntity.getAmmoUsed();
        ammoUsed.add(caliberUsedEntity);
        caliberEntity.setAmmoUsed(ammoUsed);
        caliberRepository.save(caliberEntity);
    }

    public List<CalibersAddedEntity> getHistoryOfCaliber(String caliberUUID) {
        CaliberEntity caliberEntity = caliberRepository.findById(caliberUUID).orElseThrow(EntityNotFoundException::new);
        List<CalibersAddedEntity> ammoAdded = caliberEntity.getAmmoAdded();
        ammoAdded.sort(Comparator.comparing(CalibersAddedEntity::getDate));
        return ammoAdded;

    }

    public ResponseEntity<String> addGunEntity(Gun gun) {

        if ((gun.getModelName().isEmpty() || gun.getModelName().equals("null")) || (gun.getCaliber().isEmpty() || gun.getCaliber().equals("null")) || (gun.getGunType().isEmpty() || gun.getGunType().equals("null")) || (gun.getSerialNumber().isEmpty() || gun.getSerialNumber().equals("null"))) {
            LOG.info("Nie podano wszystkich informacji");
            return ResponseEntity.badRequest().body("Nie podano wszystkich informacji");
        }
        if (gun.getProductionYear() == null || gun.getProductionYear().isEmpty() || gun.getProductionYear().equals("null")) {
            gun.setProductionYear(null);
        }
        if (gun.getAdditionalEquipment() == null || gun.getAdditionalEquipment().isEmpty() || gun.getAdditionalEquipment().equals("null")) {
            gun.setAdditionalEquipment(null);
        }
        if (gun.getComment() == null || gun.getComment().isEmpty() || gun.getComment().equals("null")) {
            gun.setComment(null);
        }
        if (gun.getBarcode() == null || gun.getBarcode().isEmpty() || gun.getBarcode().equals("null")) {
            gun.setBarcode(null);
        }
        if (gun.getGunCertificateSerialNumber() == null || gun.getGunCertificateSerialNumber().isEmpty() || gun.getGunCertificateSerialNumber().equals("null")) {
            gun.setGunCertificateSerialNumber(null);
        }
        if (gun.getRecordInEvidenceBook() == null || gun.getRecordInEvidenceBook().isEmpty() || gun.getRecordInEvidenceBook().equals("null")) {
            gun.setRecordInEvidenceBook(null);
        }

        List<GunEntity> all = gunRepository.findAll();

        if (gun.getGunCertificateSerialNumber() != null) {
            if (all.stream().filter(f -> f.getGunCertificateSerialNumber() != null).anyMatch(f -> f.getGunCertificateSerialNumber().equals(gun.getGunCertificateSerialNumber()))) {
                LOG.info("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
                return ResponseEntity.badRequest().body("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
            }
        }
        if (gun.getSerialNumber() != null) {
            if (all.stream().filter(f -> f.getSerialNumber() != null).anyMatch(e -> e.getSerialNumber().equals(gun.getSerialNumber()))) {
                LOG.info("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
                return ResponseEntity.badRequest().body("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
            }
        }
        if (gun.getRecordInEvidenceBook() != null) {
            if (all.stream().filter(f -> f.getRecordInEvidenceBook() != null).anyMatch(e -> e.getRecordInEvidenceBook().equals(gun.getRecordInEvidenceBook()))) {
                LOG.info("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
                return ResponseEntity.badRequest().body("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
            }
        }
        if (gun.getBarcode() != null) {
            if (all.stream().filter(f -> f.getBarcode() != null).anyMatch(e -> e.getBarcode().equals(gun.getBarcode()))) {
                LOG.info("Nie można Nadać kodu kreskowego gdyż jest przypisany już gdzieś indziej");
                return ResponseEntity.badRequest().body("Nie można Nadać kodu kreskowego gdyż jest przypisany już gdzieś indziej");
            }
        }

        String s = null;
        if (gun.getGunCertificateSerialNumber() != null) {
            s = gun.getGunCertificateSerialNumber().toUpperCase();
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
                .addedDate(gun.getAddedDate() != null ? gun.getAddedDate() : LocalDate.now())
                .available(true)
                .inStock(true).build();

        gunRepository.save(gunEntity);
        GunStoreEntity gunStoreEntity = gunStoreRepository.findByTypeName(gunEntity.getGunType());
//        GunStoreEntity gunStoreEntity = gunStoreRepository.findAll()
//                .stream()
//                .filter(f -> f.getTypeName().equals(gun.getGunType()))
//                .findFirst().orElseThrow(EntityNotFoundException::new);
        List<GunEntity> gunEntityList = gunStoreEntity.getGunEntityList();
        gunEntityList.add(gunEntity);

        gunStoreRepository.save(gunStoreEntity);

        return ResponseEntity.ok("Dodano broń");


    }

    public List<GunStoreEntity> getGunTypeList() {
        return gunStoreRepository.findAll().stream().map(m -> GunStoreEntity.builder().uuid(m.getUuid()).gunEntityList(null).typeName(m.getTypeName()).build())
                .sorted(Comparator.comparing(GunStoreEntity::getTypeName)).collect(Collectors.toList());
    }

    public List<?> getAllGuns() {
        List<GunStoreEntity> all = gunStoreRepository.findAll();
        all.sort(Comparator.comparing(GunStoreEntity::getTypeName));
        List<GunStoreDTO> all1 = new ArrayList<>();
        all.forEach(e -> {
            GunStoreDTO dto = new GunStoreDTO();
            dto.setTypeName(e.getTypeName());
            List<Gun> collect = e.getGunEntityList()
                    .stream()
                    .map(Mapping::map)
                    .sorted(Comparator.comparing(Gun::getModelName).reversed()
                            .thenComparing((o1, o2) -> {
                                int a = Integer.parseInt(o1.getRecordInEvidenceBook().replaceAll("/", ""));
                                int b = Integer.parseInt(o2.getRecordInEvidenceBook().replaceAll("/", ""));
                                return b - a;
                            }).reversed())
                    .collect(Collectors.toList());
            dto.setGunEntityList(collect);
            all1.add(dto);
        });
        return all1;
    }

    public ResponseEntity<?> editGunEntity(Gun gun) {
        GunEntity gunEntity = gunRepository.getOne(gun.getUuid());
        if (gunEntity == null) {
            return ResponseEntity.badRequest().body("Nie udało się zaktualizować broni");
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
        if (gun.getAddedDate() != null && gunEntity.getAddedDate() != gun.getAddedDate()) {
            gunEntity.setAddedDate(gun.getAddedDate());
        }
        gunRepository.save(gunEntity);
        return ResponseEntity.ok("Zaktualizowano broń");
    }

    public ResponseEntity<?> removeGun(String gunUUID, String pinCode) {

        GunEntity gunEntity = gunRepository.findById(gunUUID).orElse(null);
        if (gunEntity == null) {
            return ResponseEntity.badRequest().body("Nie udało się usunąć broni ze stanu");
        }
        gunEntity.setInStock(false);
        GunStoreEntity gunStoreEntity = gunStoreRepository.findAll().stream().filter(f -> f.getTypeName().equals(gunEntity.getGunType())).findFirst().orElseThrow(EntityNotFoundException::new);

        List<GunEntity> gunEntityList = gunStoreEntity.getGunEntityList();
        gunEntityList.remove(gunEntity);

        ResponseEntity<?> response = getStringResponseEntity(pinCode, gunEntity, HttpStatus.OK, "removeGun", "Usunięto broń ze stanu magazynu");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            gunStoreRepository.save(gunStoreEntity);
        }
        return response;
    }

    public ResponseEntity<?> createNewGunStore(String nameType) {
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
                    .typeName(name.toString().trim())
                    .gunEntityList(collect)
                    .build();
            LOG.info("dodaję nowy rodzaj broni");
            gunStoreRepository.save(build);
            return ResponseEntity.ok("dodaję nowy rodzaj broni");
        } else {
            LOG.info("nie dodaję nowego rodzaju broni");
            return ResponseEntity.badRequest().body("nie dodaję nowego rodzaju broni");
        }
    }

    public boolean addImageToGun(String gunUUID, String fileUUID) {
        GunEntity gunEntity = gunRepository.findById(gunUUID).orElseThrow(EntityNotFoundException::new);
        FilesEntity filesEntity = filesRepository.findById(fileUUID).orElseThrow(EntityNotFoundException::new);

        gunEntity.setImgUUID(filesEntity.getUuid());
        gunRepository.save(gunEntity);
        return true;
    }

    public ResponseEntity<?> findGunByBarcode(String barcode) {

        if (gunRepository.findByBarcode(barcode).isEmpty()) {
            return ResponseEntity.badRequest().body("Nie znaleziono broni");
        }

        GunEntity gunEntity = gunRepository.findByBarcode(barcode).orElseThrow(EntityNotFoundException::new);

        return ResponseEntity.ok(gunEntity);
    }

    public ResponseEntity<?> getListGunsUsedInDate(LocalDate date) {
        return ResponseEntity.ok(usedHistoryRepository.findAll().stream().filter(f -> LocalDate.from(f.getDateTime()).equals(date)).collect(Collectors.toList()));
    }

    public String addUseToGun(String gunUUID, String evidenceUUID, String userName, String memberUUID, String usedType) {
        AmmoEvidenceEntity ammoEvidenceEntity = evidenceUUID != null ? ammoEvidenceRepository.getOne(evidenceUUID) : null;
        GunEntity gunEntity = gunRepository.getOne(gunUUID);
        String response;
        if (gunEntity == null) {
            response = "Nie znaleziono broni";
            return response;
        }
        List<UsedHistoryEntity> usedHistoryEntityList = gunEntity.getUsedHistoryEntityList();
        boolean anyMatch = usedHistoryEntityList.stream().filter(f -> f.getEvidenceUUID() != null).anyMatch(f -> f.getEvidenceUUID().equals(evidenceUUID) && !f.isReturnToStore());
        if (anyMatch) {
            response = "Broń w użyciu";
        } else {
            UsedHistoryEntity build = UsedHistoryEntity.builder()
                    .dateTime(ammoEvidenceEntity != null ? LocalDateTime.from(ammoEvidenceEntity.getDate()) : LocalDateTime.now())
                    .gunSerialNumber(gunEntity.getSerialNumber())
                    .gunUUID(gunEntity.getUuid())
                    .evidenceUUID(evidenceUUID)
                    .gunName(gunEntity.getModelName())
                    .returnToStore(false)
                    .usedType(usedType)
                    .userName(userName)
                    .memberUUID(memberUUID)
                    .build();
            UsedHistoryEntity save = usedHistoryRepository.save(build);
            usedHistoryEntityList.add(save);
            gunEntity.setUsedHistoryEntityList(usedHistoryEntityList);
            gunEntity.setAvailable(false);
            gunEntity.setInUseStatus(usedType);
            gunRepository.save(gunEntity);
            response = usedType.equals(UsedType.TRAINING.getName()) ? "Dodano Broń" : usedType.equals(UsedType.REPAIR.getName()) ? "Wydano do Naprawy" : "odpowiedź";
        }
        return response;
    }

    public List<UsedHistoryEntity> getGunUsedHistory(String gunUUID) {

        return usedHistoryRepository.findAll().stream().filter(f -> f.getGunUUID().equals(gunUUID)).sorted(Comparator.comparing(UsedHistoryEntity::getDateTime).reversed()).collect(Collectors.toList());

    }

    public ResponseEntity<?> addUsedHistoryToGun(String barcode) {
        GunEntity gunEntity = gunRepository.findByBarcode(barcode).orElse(null);
        if (gunEntity == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono broni");
        } else {
            List<UsedHistoryEntity> usedHistoryEntityList1 = gunEntity.getUsedHistoryEntityList();
            if (usedHistoryEntityList1.stream().anyMatch(f -> LocalDate.from(f.getDateTime()).equals(LocalDate.now()))) {
                UsedHistoryEntity usedHistoryEntity = usedHistoryEntityList1.stream().filter(f -> LocalDate.from(f.getDateTime()).equals(LocalDate.now())).findFirst().orElse(null);
                if (usedHistoryEntity != null && usedHistoryEntity.getUsedType().equals(UsedType.CLEANING.getName()))
                    LOG.info("Broń była już dzisiaj czyszczona");
                return ResponseEntity.badRequest().body("Broń była już dzisiaj czyszczona");
            }
            List<UsedHistoryEntity> usedHistoryEntityList = gunEntity.getUsedHistoryEntityList();
            UsedHistoryEntity build = UsedHistoryEntity.builder()
                    .dateTime(LocalDateTime.now())
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
            return ResponseEntity.ok("Uznano broń za wyczyszczoną");
        }
    }

    public ResponseEntity<?> addUsedHistoryToGunInTournament(String barcode, String tournamentUUID) {
        GunEntity gunEntity = gunRepository.findByBarcode(barcode).orElse(null);
        if (gunEntity == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono broni");
        } else {
            List<UsedHistoryEntity> usedHistoryEntityList1 = gunEntity.getUsedHistoryEntityList();
            if (usedHistoryEntityList1.stream().anyMatch(f -> LocalDate.from(f.getDateTime()).equals(LocalDate.now()))) {
                UsedHistoryEntity usedHistoryEntity = usedHistoryEntityList1.stream().filter(f -> LocalDate.from(f.getDateTime()).equals(LocalDate.now())).findFirst().orElse(null);
                if (usedHistoryEntity != null && usedHistoryEntity.getUsedType().equals(UsedType.CLEANING.getName()))
                    LOG.info("Broń jest już Dodana");
                return ResponseEntity.badRequest().body("Broń jest już dodana");
            }
            List<UsedHistoryEntity> usedHistoryEntityList = gunEntity.getUsedHistoryEntityList();
            UsedHistoryEntity build = UsedHistoryEntity.builder()
                    .dateTime(LocalDateTime.now())
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
            return ResponseEntity.ok("Broń została dodana");
        }
    }

    public List<UsedHistoryEntity> getHistoryGuns(LocalDate firstDate, LocalDate secondDate) {
        List<UsedHistoryEntity> all = usedHistoryRepository.findAll();
        return all.stream()
                .filter(f -> f.getDateTime().isAfter(ChronoLocalDateTime.from(firstDate.minusDays(1))))
                .filter(f -> f.getDateTime().isBefore(ChronoLocalDateTime.from(secondDate.plusDays(1))))
                .sorted(Comparator.comparing(UsedHistoryEntity::getDateTime)
                        .reversed())
                .collect(Collectors.toList());
    }

    public List<UsedHistoryEntity> getGunInTournament(String tournamentUUID) {
        return usedHistoryRepository.findAll()
                .stream()
                .filter(f -> f.getEvidenceUUID() != null)
                .filter(f -> f.getEvidenceUUID().equals(tournamentUUID))
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> getStringResponseEntity(String pinCode, GunEntity gunEntity, HttpStatus status, String methodName, Object body) {
        ResponseEntity<?> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> stringResponseEntity;
        if (gunEntity != null) {
            stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, gunEntity.getClass().getSimpleName() + " " + methodName + " ", gunEntity.getUuid());
        } else {
            stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, "zmiana w zbrojowni", "kaliber");
        }
        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }

    public ResponseEntity<?> returnToStore(List<String> gunsUUID) {
        List<String> responseList = new ArrayList<>();
        gunsUUID.forEach(e -> {
            GunEntity one = gunRepository.getOne(e);
            one.setAvailable(true);
            one.setInUseStatus(null);
            gunRepository.save(one);
            UsedHistoryEntity usedHistoryEntity = usedHistoryRepository.findByGunUUIDAndReturnToStoreFalse(one.getUuid());
            usedHistoryEntity.setReturnToStore(true);
            usedHistoryRepository.save(usedHistoryEntity);
            responseList.add("Zwrócono " + one.getModelName() + " " + one.getSerialNumber());
        });
        return ResponseEntity.ok(responseList);
    }

    public ResponseEntity<?> addGunToList(String evidenceUUID, String barcode, String legitimationNumber, String IDNumber) {
        if (!gunRepository.getByBarcode(barcode).isAvailable()) {
            return ResponseEntity.ok("Broń niedostępna");
        }
        if (legitimationNumber.equals("")) {
            legitimationNumber = "0";
        }
        if (IDNumber.equals("")) {
            IDNumber = "0";
        }
        MemberEntity member = memberRepository.findByLegitimationNumber(Integer.valueOf(legitimationNumber)).orElse(null);
        OtherPersonEntity other = otherPersonRepository.findById(Integer.parseInt(IDNumber)).orElse(null);
        String userName = member != null ? member.getFullName() : other != null ? other.getSecondName() + " " + other.getFirstName() : null;
        String s = addUseToGun(gunRepository.getByBarcode(barcode).getUuid(), evidenceUUID, userName, member != null ? member.getUuid() : null, UsedType.TRAINING.getName());
        return ResponseEntity.ok(s);
    }

    public List<?> getGunInAmmoEvidenceList() {
        return usedHistoryRepository.findAllByUsedTypeAndReturnToStoreFalse(UsedType.TRAINING.getName());
    }

    public ResponseEntity<?> addGunToRepair(String gunUUID) {
        String s = addUseToGun(gunUUID, null, null, null, UsedType.REPAIR.getName());
        return ResponseEntity.ok(s);
    }

    public ResponseEntity<?> getGun(String gunUUID) {
        return ResponseEntity.ok(gunRepository.getOne(gunUUID));
    }

    public ResponseEntity<?> changeCaliberQuantity(String caliberUUID, Integer quantity, String pinCode) {
        CaliberEntity caliberEntity = caliberRepository.getOne(caliberUUID);
        caliberEntity.setQuantity(quantity);
        return getStringResponseEntity(pinCode, null, HttpStatus.OK, "changeCaliberQuantity" + caliberEntity.getName(), "zmieniono ilość amunicji");
    }

    public ResponseEntity<?> changeCaliberUnitPrice(String caliberUUID, Float price, String pinCode) {
        CaliberEntity caliberEntity = caliberRepository.getOne(caliberUUID);
        caliberEntity.setUnitPrice(price);
        return getStringResponseEntity(pinCode, null, HttpStatus.OK, "changeCaliberUnitPrice" + caliberEntity.getName(), "zmieniono cenę amunicji");

    }

    public ResponseEntity<?> changeCaliberUnitPriceForNotMember(String caliberUUID, Float price, String pinCode) {
        CaliberEntity caliberEntity = caliberRepository.getOne(caliberUUID);
        caliberEntity.setUnitPriceForNotMember(price);
        return getStringResponseEntity(pinCode, null, HttpStatus.OK, "changeCaliberUnitPriceForNotMember" + caliberEntity.getName(), "zmieniono cenę amunicji dla pozostałych");

    }

}
