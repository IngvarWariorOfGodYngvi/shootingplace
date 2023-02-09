package com.shootingplace.shootingplace.ammoEvidence;

import com.shootingplace.shootingplace.BookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordsService;
import com.shootingplace.shootingplace.armory.AmmoUsedService;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ammoEvidence")
@CrossOrigin
public class AmmoEvidenceController {

    private final AmmoEvidenceService ammoEvidenceService;
    private final AmmoUsedService ammoUsedService;
    private final ChangeHistoryService changeHistoryService;
    private final RegistrationRecordsService recordsService;

    public AmmoEvidenceController(AmmoEvidenceService ammoEvidenceService, AmmoUsedService ammoUsedService, ChangeHistoryService changeHistoryService, RegistrationRecordsService recordsService) {
        this.ammoEvidenceService = ammoEvidenceService;
        this.ammoUsedService = ammoUsedService;
        this.changeHistoryService = changeHistoryService;
        this.recordsService = recordsService;
    }


    // New ammo used by Member
    @Transactional
    @PostMapping("/ammo")
    public ResponseEntity<?> createAmmoUsed(@RequestParam String caliberUUID, @RequestParam Integer legitimationNumber, @RequestParam int otherID, @RequestParam Integer counter) {
        if (counter != 0) {
            recordsService.createRecordInBook(legitimationNumber,otherID);
            return ammoUsedService.addAmmoUsedEntity(caliberUUID, legitimationNumber, otherID, counter);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    // New ammo in evidence

    @GetMapping("/evidence")
    public ResponseEntity<?> getOpenEvidence() {
        return ammoEvidenceService.getOpenEvidence();
    }

    @GetMapping("/oneEvidence")
    public ResponseEntity<AmmoEvidenceEntity> getEvidence(@RequestParam String uuid) {
        return ResponseEntity.ok(ammoEvidenceService.getEvidence(uuid));
    }

    @GetMapping("/closedEvidences")
    public ResponseEntity<List<AmmoDTO>> getClosedEvidence(Pageable page) {
        return ResponseEntity.ok().body(ammoEvidenceService.getClosedEvidences(page));
    }

    @GetMapping("/checkAnyOpenEvidence")
    public ResponseEntity<?> checkAnyOpenEvidence() {
        return ResponseEntity.ok().body(ammoEvidenceService.checkAnyOpenEvidence());
    }

//    @GetMapping("/getGunInAmmoEvidenceList")
//    public ResponseEntity<?> getGunInAmmoEvidenceList(@RequestParam String evidenceUUID) {
//        return ResponseEntity.ok().body(ammoEvidenceService.getGunInAmmoEvidenceList(evidenceUUID));
//    }

    @Transactional
    @PutMapping("/addGunToList")
    public ResponseEntity<?> addGunToList(@RequestParam String evidenceUUID, @RequestParam String barcode) {
        return ammoEvidenceService.addGunToList(evidenceUUID, barcode);
    }

    @PatchMapping("/ammo")
    public ResponseEntity<?> closeEvidence(@RequestParam String evidenceUUID) {
        return ammoEvidenceService.closeEvidence(evidenceUUID);
    }

    @Transactional
    @PatchMapping("/ammoOpen")
    public ResponseEntity<?> openEvidence(@RequestParam String pinCode, @RequestParam String evidenceUUID) {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return ammoEvidenceService.openEvidence(evidenceUUID, pinCode);
        } else {
            return code;
        }
    }

}
