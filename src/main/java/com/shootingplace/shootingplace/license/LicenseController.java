package com.shootingplace.shootingplace.license;

import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.history.LicensePaymentHistoryEntity;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/getLicense")
    public LicenseEntity getLicense(@RequestParam String licenseUUID) {
        return licenseService.getLicense(licenseUUID);
    }

    @GetMapping("/getLicensePaymentHistory")
    public List<LicensePaymentHistoryEntity> getLicensePaymentHistory(@RequestParam String memberUUID) {
        return licenseService.getLicensePaymentHistory(memberUUID);
    }

    @GetMapping("/membersWithValidLicense")
    public ResponseEntity<List<?>> getMembersNamesAndLicense() {
        return ResponseEntity.ok(licenseService.getMembersNamesAndLicense());
    }

    @GetMapping("/membersWithNotValidLicense")
    public ResponseEntity<List<?>> getMembersNamesAndLicenseNotValid() {
        return ResponseEntity.ok(licenseService.getMembersNamesAndLicenseNotValid());
    }

    @GetMapping("/allLicencePayment")
    public ResponseEntity<?> getAllLicensePayment() {
        return ResponseEntity.ok(licenseService.getAllLicencePayment());
    }

    @PutMapping("/{memberUUID}")
    public ResponseEntity<?> updateLicense(@PathVariable String memberUUID, @RequestBody License license) {
        return licenseService.updateLicense(memberUUID, license);
    }

    @Transactional
    @PutMapping("/forceUpdate")
    public ResponseEntity<?> updateLicense(@RequestParam String memberUUID, @RequestParam String number, @RequestParam String date, @RequestParam String pinCode, @Nullable @RequestParam String isPaid) {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            String parseNumber = (number != null && !number.isEmpty() && !number.equals("null")) ? number : null;
            LocalDate parseDate = (date != null && !date.isEmpty() && !date.equals("null")) ? LocalDate.parse(date) : null;
            Boolean parseIsPaid = (isPaid != null && !isPaid.isEmpty() && !isPaid.equals("null")) ? Boolean.valueOf(isPaid) : null;
            return parseNumber == null && parseDate == null && parseIsPaid == null ? ResponseEntity.badRequest().body("Należy podać co najmniej jedną zmienną") : licenseService.updateLicense(memberUUID, parseNumber, parseDate, parseIsPaid, pinCode);
        } else {
            return code;
        }
    }

    @PatchMapping("/{memberUUID}")
    public ResponseEntity<?> renewLicenseValidDate(@PathVariable String memberUUID, @RequestBody License license) {
        return licenseService.renewLicenseValid(memberUUID, license);
    }

    @Transactional
    @PatchMapping("/prolongAll")
    public ResponseEntity<?> prolongAllLicenseWherePaidInPZSSIsTrue(@RequestParam List<String> licenseList, @RequestParam String pinCode) {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return licenseService.prolongAllLicense(licenseList, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PutMapping("/history/{memberUUID}")
    public ResponseEntity<?> addLicensePaymentHistory(@PathVariable String memberUUID, @RequestParam String pinCode) {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return historyService.addLicenseHistoryPayment(memberUUID, pinCode);
        } else {
            return code;
        }
    }

    @PatchMapping("/paymentChange")
    public ResponseEntity<?> paymentChange(@RequestParam String paymentUUID, @RequestParam String pinCode, @RequestParam boolean condition) {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return historyService.toggleLicencePaymentInPZSS(paymentUUID, condition, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PatchMapping("/paymentToggleArray")
    public ResponseEntity<?> toggleLicencePaymentInPZSSArray(@RequestParam List<String> paymentUUIDs, @RequestParam String pinCode, @RequestParam boolean condition) {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            ResponseEntity<?> result = null;
            for (String paymentUUID : paymentUUIDs) {
                result = historyService.toggleLicencePaymentInPZSS(paymentUUID, condition, pinCode);
            }
            return result;
        } else {
            return code;
        }
    }

    @PutMapping("/editPayment")
    public ResponseEntity<?> editLicensePaymentHistory(@RequestParam String memberUUID, @RequestParam String paymentUUID, @RequestParam String paymentDate, @RequestParam Integer year, @RequestParam String pinCode) {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            LocalDate parseDate = null;
            if (paymentDate != null && !paymentDate.isEmpty() && !paymentDate.equals("null")) {
                parseDate = LocalDate.parse(paymentDate);
            }
            return licenseService.updateLicensePayment(memberUUID, paymentUUID, parseDate, year, pinCode);
        } else {
            return code;
        }
    }

    @DeleteMapping("/removePayment")
    public ResponseEntity<?> removeLicensePaymentRecord(@RequestParam String paymentUUID, @RequestParam String pinCode) {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return licenseService.removeLicensePaymentRecord(paymentUUID, pinCode);
        } else {
            return code;
        }
    }

}
