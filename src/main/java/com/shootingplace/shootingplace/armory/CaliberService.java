package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CaliberService {

    private final CaliberRepository caliberRepository;
    private final ChangeHistoryService changeHistoryService;

    public CaliberService(CaliberRepository caliberRepository, ChangeHistoryService changeHistoryService) {
        this.caliberRepository = caliberRepository;
        this.changeHistoryService = changeHistoryService;
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
            caliberList = createAllCalibersEntities().stream().map(Mapping::map).collect(Collectors.toList());
        } else {
            caliberList = caliberRepository.findAll().stream().map(Mapping::map).collect(Collectors.toList());
        }

        return getCaliberSortedList(caliberList);

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

    public ResponseEntity<?> createNewCaliber(String caliber, String pinCode) {

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

    public ResponseEntity<?> getStringResponseEntity(String pinCode, CaliberEntity caliberEntity, HttpStatus status, String methodName, Object body) {
        ResponseEntity<?> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, caliberEntity.getClass().getSimpleName() + " " + methodName + " ", caliberEntity.getUuid());
        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }
}
