package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/armory")
@CrossOrigin
public class ArmoryController {

    private final ArmoryService armoryService;
    private final CaliberService caliberService;
    private final AmmoUsedService ammoUsedService;
    private final ChangeHistoryService changeHistoryService;
    private final ShootingPacketService shootingPacketService;


    public ArmoryController(ArmoryService armoryService, CaliberService caliberService, AmmoUsedService ammoUsedService, ChangeHistoryService changeHistoryService, ShootingPacketService shootingPacketService) {
        this.armoryService = armoryService;
        this.caliberService = caliberService;
        this.ammoUsedService = ammoUsedService;
        this.changeHistoryService = changeHistoryService;
        this.shootingPacketService = shootingPacketService;
    }

    @GetMapping("/recount")
    public void recount() {
        ammoUsedService.recountAmmo();
    }


    @GetMapping("/calibers")
    public ResponseEntity<?> getCalibersList() {
        return ResponseEntity.ok(caliberService.getCalibersList());
    }

    @GetMapping("/caliberQuantity")
    public ResponseEntity<?> getcalibersQuantity(@RequestParam String uuid, @RequestParam String date) {
        LocalDate parseDate = LocalDate.parse(date);
        return ResponseEntity.ok(caliberService.getCalibersQuantity(uuid, parseDate));
    }

    @GetMapping("/calibersList")
    public ResponseEntity<List<String>> getCalibersNamesList() {
        return ResponseEntity.ok(caliberService.getCalibersNamesList());
    }

    @GetMapping("/getGun")
    public ResponseEntity<?> getGun(@RequestParam String gunUUID) {
        return armoryService.getGun(gunUUID);
    }

    @Transactional
    @GetMapping("/quantitySum")
    public ResponseEntity<?> getSumFromAllAmmoList(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(armoryService.getSumFromAllAmmoList(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/gunType")
    public ResponseEntity<List<GunStoreEntity>> getGunTypeList() {
        return ResponseEntity.ok(armoryService.getGunTypeList());
    }

    @GetMapping("/getGuns")
    public ResponseEntity<?> getAllGuns() {
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

    @GetMapping("/getAllShootingPacket")
    public ResponseEntity<?> getAllShootingPacket() {
        return ResponseEntity.ok(shootingPacketService.getAllShootingPacket());
    }

    @Transactional
    @PostMapping("/addShootingPacket")
    public ResponseEntity<?> addShootingPacket(@RequestParam String name, @RequestParam float price, @RequestBody Map<String, Integer> map, @RequestParam String pinCode) throws NoUserPermissionException {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return shootingPacketService.addShootingPacket(name, price, map, pinCode);
        } else {
            return code;
        }
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

    @Transactional
    @PutMapping("/remove")
    public ResponseEntity<?> removeGun(@RequestParam String gunUUID, @RequestParam String pinCode) throws NoUserPermissionException {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return armoryService.removeGun(gunUUID, pinCode);
        } else {
            return code;
        }

    }

    @Transactional
    @PostMapping("/calibers")
    public ResponseEntity<?> createNewCaliber(@RequestParam String caliber, @RequestParam String pinCode) throws NoUserPermissionException {
        if (caliber.isEmpty()) {
            return ResponseEntity.badRequest().body("Wprowadź dane");
        }
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return caliberService.createNewCaliber(caliber, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PatchMapping("/changeCaliberQuantity")
    public ResponseEntity<?> changeCaliberQuantity(@RequestParam String caliberUUID, @RequestParam Integer quantity, @RequestParam String pinCode) throws NoUserPermissionException {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return armoryService.changeCaliberQuantity(caliberUUID, quantity, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PatchMapping("/changeCaliberUnitPrice")
    public ResponseEntity<?> changeCaliberUnitPrice(@RequestParam String caliberUUID, @RequestParam Float price, @RequestParam String pinCode) throws NoUserPermissionException {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return armoryService.changeCaliberUnitPrice(caliberUUID, price, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PatchMapping("/changeCaliberUnitPriceForNotMember")
    public ResponseEntity<?> changeCaliberUnitPriceForNotMember(@RequestParam String caliberUUID, @RequestParam Float price, @RequestParam String pinCode) throws NoUserPermissionException {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return armoryService.changeCaliberUnitPriceForNotMember(caliberUUID, price, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PostMapping("/newGunTypeName")
    public ResponseEntity<?> createNewGunStore(@RequestParam String nameType) {
        if (nameType.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return armoryService.createNewGunStore(nameType);
    }

    @Transactional
    @PutMapping("/addGunToList")
    public ResponseEntity<?> addGunToList(@RequestParam String evidenceUUID, @RequestParam String barcode, @Nullable @RequestParam String legitimationNumber, @Nullable @RequestParam String IDNumber) {
        if (evidenceUUID.equals("undefined")) {
            evidenceUUID = null;
        }
        return armoryService.addGunToList(evidenceUUID, barcode, legitimationNumber, IDNumber);
    }

    @Transactional
    @PutMapping("/addGunToRepair")
    public ResponseEntity<?> addGunToRepair(@RequestParam String gunUUID) {
        return armoryService.addGunToRepair(gunUUID);
    }

    @GetMapping("/getGunInAmmoEvidenceList")
    public ResponseEntity<?> getGunInAmmoEvidenceList() {
        return ResponseEntity.ok().body(armoryService.getGunInAmmoEvidenceList());
    }

    @Transactional
    @PatchMapping("/returnToStore")
    public ResponseEntity<?> returnToStore(@RequestParam List<String> gunsUUID) {
        return armoryService.returnToStore(gunsUUID);
    }

    @Transactional
    @PatchMapping("/addImageToGun")
    public ResponseEntity<?> addImageToGun(@RequestParam String gunUUID, @RequestParam String fileUUID) {

        if (armoryService.addImageToGun(gunUUID, fileUUID)) {
            return ResponseEntity.ok("Przypisano zdjęcie");
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


}
