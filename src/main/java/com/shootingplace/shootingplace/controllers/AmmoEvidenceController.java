package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.entities.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.domain.models.AmmoDTO;
import com.shootingplace.shootingplace.services.AmmoEvidenceService;
import com.shootingplace.shootingplace.services.AmmoUsedService;
import com.shootingplace.shootingplace.services.ChangeHistoryService;
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

    public AmmoEvidenceController(AmmoEvidenceService ammoEvidenceService, AmmoUsedService ammoUsedService, ChangeHistoryService changeHistoryService) {
        this.ammoEvidenceService = ammoEvidenceService;
        this.ammoUsedService = ammoUsedService;
        this.changeHistoryService = changeHistoryService;
    }


    // New ammo used by Member
    @Transactional
    @PostMapping("/ammo")
    public ResponseEntity<?> createAmmoUsed(@RequestParam String caliberUUID, @RequestParam Integer legitimationNumber, @RequestParam int otherID, @RequestParam Integer counter) {
        if (counter != 0) {
            if (ammoUsedService.addAmmoUsedEntity(caliberUUID, legitimationNumber, otherID, counter)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(406).build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    // New ammo in evidence

    @GetMapping("/evidence")
    public ResponseEntity<List<AmmoEvidenceEntity>> getAllEvidences(@RequestParam boolean state) {
        return ResponseEntity.ok(ammoEvidenceService.getAllEvidences(state));
    }

    @GetMapping("/oneEvidence")
    public ResponseEntity<AmmoEvidenceEntity> getEvidence(@RequestParam String uuid) {
        return ResponseEntity.ok(ammoEvidenceService.getEvidence(uuid));
    }

    @GetMapping("/closedEvidences")
    public ResponseEntity<List<AmmoDTO>> getClosedEvidence() {
        return ResponseEntity.ok().body(ammoEvidenceService.getClosedEvidences());
    }

    @GetMapping("/checkAnyOpenEvidence")
    public ResponseEntity<?> checkAnyOpenEvidence(){
        return ResponseEntity.ok().body(ammoEvidenceService.checkAnyOpenEvidence());
    }

    @PatchMapping("/ammo")
    public ResponseEntity<?> closeEvidence(@RequestParam String evidenceUUID) {
        if (ammoEvidenceService.closeEvidence(evidenceUUID)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @PatchMapping("/ammoOpen")
    public ResponseEntity<?> openEvidence(@RequestParam String pinCode, @RequestParam String evidenceUUID) {
        if (changeHistoryService.comparePinCode(pinCode)) {
            if (ammoEvidenceService.openEvidence(evidenceUUID,pinCode)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
    }

}
