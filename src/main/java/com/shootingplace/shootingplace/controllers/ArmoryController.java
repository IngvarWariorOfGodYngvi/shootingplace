package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.entities.CaliberEntity;
import com.shootingplace.shootingplace.domain.entities.GunStoreEntity;
import com.shootingplace.shootingplace.domain.models.Gun;
import com.shootingplace.shootingplace.services.ArmoryService;
import com.shootingplace.shootingplace.services.CaliberService;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
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

    @GetMapping("/getHistoryGuns")
    public ResponseEntity<?> getHistoryGuns(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(armoryService.getHistoryGuns(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/getGunsUsedInDate")
    public ResponseEntity<?> getGunsUsedInDate(@RequestParam LocalDate date) {
        return armoryService.getListGunsUsedInDate(date);
    }

    @GetMapping("/getGunByBarcode")
    public ResponseEntity<?> findGunByBarcode(@RequestParam String barcode) {
        return ResponseEntity.ok(armoryService.findGunByBarcode(barcode));
    }

    @GetMapping("/getGunUsedHistory")
    public ResponseEntity<?> getGunUsedHistory(@RequestParam String gunUUID) {
        return ResponseEntity.ok(armoryService.getGunUsedHistory(gunUUID));
    }

    @PutMapping("/addAmmo")
    public ResponseEntity<?> updateAmmoQuantity(@RequestParam String caliberUUID, @RequestParam Integer count, @RequestParam String date, @RequestParam String description) {
        LocalDate parse = LocalDate.parse(date);
        armoryService.updateAmmo(caliberUUID, count, parse, description);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/addUsedHistoryToGun")
    public ResponseEntity<?> addUsedHistoryToGun(@RequestParam String barcode) {
        return armoryService.addUsedHistoryToGun(barcode);
    }

    @Transactional
    @PostMapping("/addGun")
    public ResponseEntity<?> addGunEntity(@RequestBody Gun gun) {
        return armoryService.addGunEntity(gun);
    }

    @Transactional
    @PostMapping("/editGun")
    public ResponseEntity<?> editGunEntity(@RequestBody Gun gun) {
        return armoryService.editGunEntity(gun);
    }

    @PutMapping("/remove")
    public ResponseEntity<?> removeGun(@RequestParam String gunUUID, @RequestParam String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {

            return armoryService.removeGun(gunUUID, pinCode);
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
    }

    @PostMapping("/calibers")
    public ResponseEntity<?> createNewCaliber(@RequestParam String caliber, @RequestParam String pinCode) {
        if (caliber.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return caliberService.createNewCaliber(caliber,pinCode);
    }

    @PostMapping("/newGunTypeName")
    public ResponseEntity<?> createNewGunStore(@RequestParam String nameType) {
        if (nameType.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return armoryService.createNewGunStore(nameType);
    }

    @PatchMapping("/addImageToGun")
    public ResponseEntity<?> addImageToGun(@RequestParam String gunUUID, @RequestParam String fileUUID) {

        if (armoryService.addImageToGun(gunUUID, fileUUID)) {
            return ResponseEntity.ok("Przypisano zdjęcie");
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


}
