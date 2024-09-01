package com.shootingplace.shootingplace.contributions;

import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/contribution")
@CrossOrigin
public class ContributionController {

    private final ContributionService contributionService;
    private final ChangeHistoryService changeHistoryService;

    public ContributionController(ContributionService contributionService, ChangeHistoryService changeHistoryService) {
        this.contributionService = contributionService;
        this.changeHistoryService = changeHistoryService;
    }

    @Transactional
    @PostMapping("/history/{memberUUID}")
    public ResponseEntity<?> addHistoryContributionRecord(@PathVariable String memberUUID, @RequestParam String date, @RequestParam String pinCode) throws NoUserPermissionException {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return contributionService.addContributionRecord(memberUUID, LocalDate.parse(date), pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PatchMapping("/{memberUUID}")
    public ResponseEntity<?> addContribution(@PathVariable String memberUUID, @RequestParam String pinCode, @RequestParam Integer contributionCount) throws NoUserPermissionException {

        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return contributionService.addContribution(memberUUID, LocalDate.now(), pinCode, contributionCount);
        } else {
            return code;
        }
    }

    @Transactional
    @PutMapping("/edit")
    public ResponseEntity<?> editContribution(@RequestParam String memberUUID, @RequestParam String contributionUUID, @RequestParam String paymentDay, @RequestParam String validThru, @RequestParam String pinCode) throws NoUserPermissionException {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            LocalDate parsedPaymentDay = null;
            if (paymentDay != null && !paymentDay.isEmpty() && !paymentDay.equals("null")) {
                parsedPaymentDay = LocalDate.parse(paymentDay);
            }
            LocalDate parsedValidThru = null;
            if (validThru != null && !validThru.isEmpty() && !validThru.equals("null")) {
                parsedValidThru = LocalDate.parse(validThru);
            }

            return contributionService.updateContribution(memberUUID, contributionUUID, parsedPaymentDay, parsedValidThru, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PatchMapping("/remove/{memberUUID}")
    public ResponseEntity<?> removeContribution(@PathVariable String memberUUID, @RequestParam String contributionUUID, @RequestParam String pinCode) throws NoUserPermissionException {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return contributionService.removeContribution(memberUUID, contributionUUID, pinCode);
        } else {
            return code;
        }
    }
}
