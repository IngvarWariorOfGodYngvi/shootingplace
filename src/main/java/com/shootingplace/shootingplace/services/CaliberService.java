package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.CaliberEntity;
import com.shootingplace.shootingplace.repositories.CaliberRepository;
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

    public List<CaliberEntity> getCalibersList() {
        List<CaliberEntity> caliberEntityList;
        if (caliberRepository.findAll().isEmpty()) {
            caliberEntityList = createAllCalibersEntities();
        } else {
            caliberEntityList = caliberRepository.findAll();
        }

        return getCaliberSortedEntityList(caliberEntityList);

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
        caliberRepository.saveAll(list);
        return list;
    }

    public ResponseEntity<?> createNewCaliber(String caliber, String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {
            boolean match = caliberRepository.findAll().stream().anyMatch(a -> a.getName().equals(caliber.trim().toLowerCase()));
            if (!match) {
                CaliberEntity caliberEntity = CaliberEntity.builder()
                        .name(caliber.trim().toLowerCase())
                        .quantity(0)
                        .ammoAdded(null)
                        .ammoUsed(null)
                        .build();
                caliberRepository.saveAndFlush(caliberEntity);
                return ResponseEntity.ok("\"Utworzono nowy kaliber\"");
            } else {
                return ResponseEntity.badRequest().body("\"Nie udało się utworzyć nowego kalibru\"");
            }
        } else return ResponseEntity.status(403).body("\"Błędny kod. Spróbuj ponownie\"");
    }
}
