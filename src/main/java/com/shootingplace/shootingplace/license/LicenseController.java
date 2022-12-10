package com.shootingplace.shootingplace.license;

import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.history.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/license")
@CrossOrigin
public class LicenseController {

    private final ChangeHistoryService changeHistoryService;
    private final LicenseService licenseService;
    private final HistoryService historyService;

    public LicenseController(ChangeHistoryService changeHistoryService, LicenseService licenseService, HistoryService historyService) {
        this.changeHistoryService = changeHistoryService;
        this.licenseService = licenseService;
        this.historyService = historyService;
    }

    @GetMapping("/membersWithValidLicense")
    public ResponseEntity<List<MemberDTO>> getMembersNamesAndLicense() {
        return ResponseEntity.ok(licenseService.getMembersNamesAndLicense());
    }

    @GetMapping("/membersWithNotValidLicense")
    public ResponseEntity<List<MemberDTO>> getMembersNamesAndLicenseNotValid() {
        return ResponseEntity.ok(licenseService.getMembersNamesAndLicenseNotValid());
    }

    @GetMapping("/allLicencePayment")
    public ResponseEntity<?> getAllLicensePayment() {
        return ResponseEntity.ok(licenseService.getAllLicencePayment());
    }

    @PutMapping("/{memberUUID}")
    public ResponseEntity<?> updateLicense(@PathVariable String memberUUID, @RequestBody License license) {
        // TODO wprowadzić blokadę kodem
        return licenseService.updateLicense(memberUUID, license);
    }

    @PutMapping("/forceUpdate")
    public ResponseEntity<?> updateLicense(@RequestParam String memberUUID, @RequestParam String number, @RequestParam String date, @RequestParam String pinCode,@Nullable @RequestParam boolean isPaid) {
        if (changeHistoryService.comparePinCode(pinCode)) {
            LocalDate parseDate = null;
            if (date != null && !date.isEmpty() && !date.equals("null")) {
                parseDate = LocalDate.parse(date);
            }
            return licenseService.updateLicense(memberUUID, number, parseDate,isPaid, pinCode);
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
    }

    @PatchMapping("/{memberUUID}")
    public ResponseEntity<?> renewLicenseValidDate(@PathVariable String memberUUID, @RequestBody License license) {
        return licenseService.renewLicenseValid(memberUUID, license);
    }

    @PatchMapping("/prolongAll")
    public ResponseEntity<?> prolongAllLicenseWherePaidInPZSSIsTrue(@RequestParam List<String> licenseList, @RequestParam String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {
            return licenseService.prolongAllLicense(licenseList, pinCode);
        } else {
            return ResponseEntity.status(403).body("Brak uprawnień");
        }
    }

    @PutMapping("/history/{memberUUID}")
    public ResponseEntity<?> addLicensePaymentHistory(@PathVariable String memberUUID) {
        return historyService.addLicenseHistoryPayment(memberUUID);
    }

    @PatchMapping("/paymentToggle")
    public ResponseEntity<?> toggleLicencePaymentInPZSS(@RequestParam String paymentUUID, @RequestParam String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {
            return historyService.toggleLicencePaymentInPZSS(paymentUUID);
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
    }

    @Transactional
    @PatchMapping("/paymentToggleArray")
    public ResponseEntity<?> toggleLicencePaymentInPZSSArray(@RequestParam List<String> paymentUUIDs, @RequestParam String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {
            ResponseEntity<?> result = null;
            for (String paymentUUID : paymentUUIDs) {
                result = historyService.toggleLicencePaymentInPZSS(paymentUUID);
            }
            return result;
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
    }

    @PutMapping("/editPayment")
    public ResponseEntity<?> editLicensePaymentHistory(@RequestParam String memberUUID, @RequestParam String paymentUUID, @RequestParam String paymentDate, @RequestParam Integer year, @RequestParam String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {
            LocalDate parseDate = null;
            if (paymentDate != null && !paymentDate.isEmpty() && !paymentDate.equals("null")) {
                parseDate = LocalDate.parse(paymentDate);
            }
            return licenseService.updateLicensePayment(memberUUID, paymentUUID, parseDate, year, pinCode);
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
    }

    @DeleteMapping("/removePayment")
    public ResponseEntity<?> removeLicensePaymentRecord(@RequestParam String paymentUUID, @RequestParam String pinCode){
        if (changeHistoryService.comparePinCode(pinCode)) {

            return licenseService.removeLicensePaymentRecord(paymentUUID, pinCode);
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
    }

}
