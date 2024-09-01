package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.ammoEvidence.AmmoInEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoInEvidenceRepository;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class CaliberService {

    private final CaliberRepository caliberRepository;
    private final ChangeHistoryService changeHistoryService;
    private final CalibersAddedRepository calibersAddedRepository;
    private final AmmoInEvidenceRepository ammoInEvidenceRepository;
    private final AmmoEvidenceRepository ammoEvidenceRepository;

    public CaliberService(CaliberRepository caliberRepository, ChangeHistoryService changeHistoryService, CalibersAddedRepository calibersAddedRepository, AmmoInEvidenceRepository ammoInEvidenceRepository, AmmoEvidenceRepository ammoEvidenceRepository) {
        this.caliberRepository = caliberRepository;
        this.changeHistoryService = changeHistoryService;
        this.calibersAddedRepository = calibersAddedRepository;
        this.ammoInEvidenceRepository = ammoInEvidenceRepository;
        this.ammoEvidenceRepository = ammoEvidenceRepository;
    }

    public List<CaliberEntity> getCalibersEntityList() {
        List<CaliberEntity> caliberEntityList;
        if (caliberRepository.findAll().isEmpty()) {
            caliberEntityList = createAllCalibersEntities();
        } else {
            caliberEntityList = caliberRepository.findAll();
        }

        return getCaliberSortedEntityList(caliberEntityList);

    }

    public List<Caliber> getCalibersList() {
        List<Caliber> caliberList;
        if (caliberRepository.findAll().isEmpty()) {
            caliberList = createAllCalibersEntities().stream().map(this::map).collect(Collectors.toList());
        } else {
            caliberList = caliberRepository.findAll().stream().map(this::map).collect(Collectors.toList());
        }

        return getCaliberSortedList(caliberList);

    }

    public int getCalibersQuantity(String uuid, LocalDate date) {
        List<CalibersAddedEntity> collect = calibersAddedRepository.findAll()
                .stream()
                .filter(f -> f.getBelongTo().equals(uuid))
                .filter(f -> f.getDate().isBefore(date.plusDays(1)))
                .collect(Collectors.toList());
        AtomicReference<Integer> count = new AtomicReference<>(0);
        if (collect.size() > 0) {
            collect.forEach(f -> {
                count.updateAndGet(v -> v + f.getAmmoAdded());
            });
        }
        List<AmmoInEvidenceEntity> collect1 = new ArrayList<>();
        ammoEvidenceRepository.findAll()
                .stream()
                .filter(f -> f.getDate().isBefore(date.plusDays(1)))
                .forEach(e -> {
                    List<AmmoInEvidenceEntity> collect2 = e.getAmmoInEvidenceEntityList()
                            .stream()
                            .filter(f -> f.getCaliberUUID().equals(uuid))
                            .collect(Collectors.toList());
                    collect1.addAll(collect2);
                });
        AtomicReference<Integer> count1 = new AtomicReference<>(0);
        if (collect1.size() > 0) {
            collect1.forEach(g -> {
                count1.updateAndGet(v -> v + g.getQuantity());
            });
        }
        Integer opaque = count.getOpaque();
        Integer opaque1 = count1.getOpaque();
        return opaque - opaque1;
    }

    private Caliber map(CaliberEntity c) {
        return Optional.ofNullable(c).map(e -> Caliber.builder()
                .name(e.getName())
                .uuid(e.getUuid())
                .quantity(getCaliberAmmoInStore(c.getUuid()))
                .unitPrice(e.getUnitPrice())
                .unitPriceForNotMember(e.getUnitPriceForNotMember())
                .build()).orElse(null);
    }

    public int getCaliberAmmoInStore(String uuid) {
        List<CalibersAddedEntity> collect = calibersAddedRepository.findAll()
                .stream()
                .filter(f -> f.getBelongTo().equals(uuid))
                .collect(Collectors.toList());
        AtomicReference<Integer> count = new AtomicReference<>(0);
        collect.forEach(f -> {
            count.updateAndGet(v -> v + f.getAmmoAdded());
        });
        List<AmmoInEvidenceEntity> collect1 = ammoInEvidenceRepository.findAll()
                .stream()
                .filter(f -> f.getCaliberUUID().equals(uuid))
                .collect(Collectors.toList());
        AtomicReference<Integer> count1 = new AtomicReference<>(0);
        collect1.forEach(g -> {
            count1.updateAndGet(v -> v + g.getQuantity());
        });
        return count.getOpaque() - count1.getOpaque();
    }

    private List<CaliberEntity> getCaliberSortedEntityList(List<CaliberEntity> caliberEntityList) {
        String[] sort = {"5,6mm", "9x19mm", "12/76", ".357", ".38", "7,62x39mm"};
        List<CaliberEntity> collect = caliberEntityList.stream().filter(f -> !f.getName().equals(sort[0])
                        && !f.getName().equals(sort[1])
                        && !f.getName().equals(sort[2])
                        && !f.getName().equals(sort[3])
                        && !f.getName().equals(sort[4])
                        && !f.getName().equals(sort[5]))
                .collect(Collectors.toList());

        CaliberEntity caliberEntity = caliberEntityList.stream().filter(f -> f.getName().equals(sort[0])).findFirst().orElse(null);
        CaliberEntity caliberEntity1 = caliberEntityList.stream().filter(f -> f.getName().equals(sort[1])).findFirst().orElse(null);
        CaliberEntity caliberEntity2 = caliberEntityList.stream().filter(f -> f.getName().equals(sort[2])).findFirst().orElse(null);
        CaliberEntity caliberEntity3 = caliberEntityList.stream().filter(f -> f.getName().equals(sort[3])).findFirst().orElse(null);
        CaliberEntity caliberEntity4 = caliberEntityList.stream().filter(f -> f.getName().equals(sort[4])).findFirst().orElse(null);
        CaliberEntity caliberEntity5 = caliberEntityList.stream().filter(f -> f.getName().equals(sort[5])).findFirst().orElse(null);
        List<CaliberEntity> caliberEntityList2 = new ArrayList<>();
        caliberEntityList2.add(caliberEntity);
        caliberEntityList2.add(caliberEntity1);
        caliberEntityList2.add(caliberEntity2);
        caliberEntityList2.add(caliberEntity3);
        caliberEntityList2.add(caliberEntity4);
        caliberEntityList2.add(caliberEntity5);
        caliberEntityList2.addAll(collect);
        return caliberEntityList2;
    }

    private List<Caliber> getCaliberSortedList(List<Caliber> caliberList) {
        String[] sort = {"5,6mm", "9x19mm", "12/76", ".357", ".38", "7,62x39mm"};
        List<Caliber> collect = caliberList.stream().filter(f -> !f.getName().equals(sort[0])
                        && !f.getName().equals(sort[1])
                        && !f.getName().equals(sort[2])
                        && !f.getName().equals(sort[3])
                        && !f.getName().equals(sort[4])
                        && !f.getName().equals(sort[5]))
                .collect(Collectors.toList());

        Caliber caliber = caliberList.stream().filter(f -> f.getName().equals(sort[0])).findFirst().orElse(null);
        Caliber caliber1 = caliberList.stream().filter(f -> f.getName().equals(sort[1])).findFirst().orElse(null);
        Caliber caliber2 = caliberList.stream().filter(f -> f.getName().equals(sort[2])).findFirst().orElse(null);
        Caliber caliber3 = caliberList.stream().filter(f -> f.getName().equals(sort[3])).findFirst().orElse(null);
        Caliber caliber4 = caliberList.stream().filter(f -> f.getName().equals(sort[4])).findFirst().orElse(null);
        Caliber caliber5 = caliberList.stream().filter(f -> f.getName().equals(sort[5])).findFirst().orElse(null);
        List<Caliber> caliberList2 = new ArrayList<>();
        caliberList2.add(caliber);
        caliberList2.add(caliber1);
        caliberList2.add(caliber2);
        caliberList2.add(caliber3);
        caliberList2.add(caliber4);
        caliberList2.add(caliber5);
        caliberList2.addAll(collect);
        return caliberList2;
    }

    public List<String> getCalibersNamesList() {
        List<CaliberEntity> caliberEntityList;
        if (caliberRepository.findAll().isEmpty()) {
            caliberEntityList = createAllCalibersEntities();
        } else {
            caliberEntityList = caliberRepository.findAll();
        }
        List<CaliberEntity> caliberSortedEntityList = getCaliberSortedEntityList(caliberEntityList);
        List<String> caliberSortedEntityListNames = new ArrayList<>();
        caliberSortedEntityList.forEach(e -> caliberSortedEntityListNames.add(e.getName()));

        return caliberSortedEntityListNames;

    }

    private List<CaliberEntity> createAllCalibersEntities() {

        List<CaliberEntity> list = new ArrayList<>();
        CaliberEntity caliberEntity = CaliberEntity.builder()

                .name("5,6mm")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        list.add(caliberEntity);
        CaliberEntity caliberEntity1 = CaliberEntity.builder()
                .name("9x19mm")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        list.add(caliberEntity1);
        CaliberEntity caliberEntity2 = CaliberEntity.builder()
                .name("7,62x39mm")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        list.add(caliberEntity2);
        CaliberEntity caliberEntity3 = CaliberEntity.builder()
                .name(".38")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        list.add(caliberEntity3);
        CaliberEntity caliberEntity4 = CaliberEntity.builder()
                .name(".357")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        list.add(caliberEntity4);
        CaliberEntity caliberEntity5 = CaliberEntity.builder()
                .name("12/76")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        list.add(caliberEntity5);
        list.forEach(caliberRepository::save);
        return list;
    }

    public ResponseEntity<?> createNewCaliber(String caliber, String pinCode) throws NoUserPermissionException {

        boolean match = caliberRepository.findAll().stream().anyMatch(a -> a.getName().equals(caliber.trim().toLowerCase()));
        if (!match) {
            CaliberEntity caliberEntity = CaliberEntity.builder()
                    .name(caliber.trim().toLowerCase())
                    .quantity(0)
                    .ammoAdded(null)
                    .ammoUsed(null)
                    .build();
            ResponseEntity response = getStringResponseEntity(pinCode, caliberEntity, HttpStatus.OK, "createNewCaliber", "Utworzono nowy Kaliber");
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                caliberRepository.save(caliberEntity);
            }
            return response;
        } else {
            return ResponseEntity.badRequest().body("Nie udało się utworzyć nowego kalibru");
        }
    }

    public ResponseEntity<?> getStringResponseEntity(String pinCode, CaliberEntity caliberEntity, HttpStatus status, String methodName, Object body) throws NoUserPermissionException {
        ResponseEntity<?> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, caliberEntity.getClass().getSimpleName() + " " + methodName + " ", caliberEntity.getUuid());
        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }
}
