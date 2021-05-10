package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.entities.CaliberEntity;
import com.shootingplace.shootingplace.domain.entities.GunStoreEntity;
import com.shootingplace.shootingplace.services.ArmoryService;
import com.shootingplace.shootingplace.services.CaliberService;
import com.shootingplace.shootingplace.services.ChangeHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/armory")
@CrossOrigin
public class ArmoryController {

    private final ArmoryService armoryService;
    private final CaliberService caliberService;
    private final ChangeHistoryService changeHistoryService;


    public ArmoryController(ArmoryService armoryService, CaliberService caliberService, ChangeHistoryService changeHistoryService) {
        this.armoryService = armoryService;
        this.caliberService = caliberService;
        this.changeHistoryService = changeHistoryService;
    }


    @GetMapping("/calibers")
    public ResponseEntity<List<CaliberEntity>> getCalibersList() {
        return ResponseEntity.ok(caliberService.getCalibersList());
    }

    @GetMapping("/calibersList")
    public ResponseEntity<List<String>> getCalibersNamesList() {
        return ResponseEntity.ok(caliberService.getCalibersNamesList());
    }

    @Transactional
    @GetMapping("/quantitySum")
    public ResponseEntity<?> getSumFromAllAmmoList(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
//        armoryService.update();
        return ResponseEntity.ok(armoryService.getSumFromAllAmmoList(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/gunType")
    public ResponseEntity<List<String>> getGunTypeList() {
        return ResponseEntity.ok(armoryService.getGunTypeList());
    }

    @GetMapping("/getGuns")
    public ResponseEntity<List<GunStoreEntity>> getAllGuns() {
        return ResponseEntity.ok(armoryService.getAllGuns());
    }

    @GetMapping("/getHistory")
    public ResponseEntity<?> getHistoryOfCaliber(@RequestParam String caliberUUID) {
        return ResponseEntity.ok(armoryService.getHistoryOfCaliber(caliberUUID));
    }

    @PutMapping("/addAmmo")
    public ResponseEntity<?> updateAmmoQuantity(@RequestParam String caliberUUID, @RequestParam Integer count, @RequestParam String date, @RequestParam String description) {
        LocalDate parse = LocalDate.parse(date);
        armoryService.updateAmmo(caliberUUID, count, parse, description);
        return ResponseEntity.ok().build();
    }

    @Transactional
    @PostMapping("/addGun")
    public ResponseEntity<?> addGunEntity(@RequestParam String modelName,
                                          @RequestParam String caliber,
                                          @RequestParam String gunType,
                                          @RequestParam String serialNumber,
                                          @RequestParam String productionYear,
                                          @RequestParam String numberOfMagazines,
                                          @RequestParam String gunCertificateSerialNumber,
                                          @RequestParam String additionalEquipment,
                                          @RequestParam String recordInEvidenceBook,
                                          @RequestParam String comment,
                                          @RequestParam String basisForPurchaseOrAssignment) {
        if (armoryService.addGunEntity(modelName, caliber, gunType, serialNumber, productionYear, numberOfMagazines, gunCertificateSerialNumber, additionalEquipment, recordInEvidenceBook, comment, basisForPurchaseOrAssignment)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @PostMapping("/editGun")
    public ResponseEntity<?> editGunEntity(@RequestParam String gunUUID,
                                           @RequestParam String modelName,
                                           @RequestParam String caliber,
                                           @RequestParam String gunType,
                                           @RequestParam String serialNumber,
                                           @RequestParam String productionYear,
                                           @RequestParam String numberOfMagazines,
                                           @RequestParam String gunCertificateSerialNumber,
                                           @RequestParam String additionalEquipment,
                                           @RequestParam String recordInEvidenceBook,
                                           @RequestParam String comment,
                                           @RequestParam String basisForPurchaseOrAssignment) {
        if (armoryService.editGunEntity(gunUUID, modelName, caliber, gunType, serialNumber, productionYear, numberOfMagazines, gunCertificateSerialNumber, additionalEquipment, recordInEvidenceBook, comment, basisForPurchaseOrAssignment)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/remove")
    public ResponseEntity<?> removeGun(@RequestParam String gunUUID, @RequestParam String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {

            if (armoryService.removeGun(gunUUID, pinCode)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
    }

    @PostMapping("/calibers")
    public ResponseEntity<?> createNewCaliber(@RequestParam String caliber) {
        if (caliber.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (caliberService.createNewCaliber(caliber)) {
            return ResponseEntity.status(201).build();
        } else
            return ResponseEntity.badRequest().build();
    }

    @PostMapping("/newGunTypeName")
    public ResponseEntity<?> createNewGunStore(@RequestParam String nameType) {
        if (nameType.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (armoryService.createNewGunStore(nameType)) {
            return ResponseEntity.status(200).build();
        } else
            return ResponseEntity.badRequest().build();
    }


}
