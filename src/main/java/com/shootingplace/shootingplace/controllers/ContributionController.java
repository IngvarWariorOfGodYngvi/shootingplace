package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.services.ContributionService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/contribution")
@CrossOrigin(origins = "https://localhost:8081")
public class ContributionController {

    private final ContributionService contributionService;

    public ContributionController(ContributionService contributionService) {
        this.contributionService = contributionService;
    }

    @Transactional
    @PostMapping("/history/{memberUUID}")
    public ResponseEntity<?> addHistoryContributionRecord(@PathVariable UUID memberUUID, @RequestParam String date) {
        if (contributionService.addContributionRecord(memberUUID, LocalDate.parse(date))) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @Transactional
    @PatchMapping("/{memberUUID}")
    public ResponseEntity<?> addContribution(@PathVariable UUID memberUUID) {
        if (contributionService.addContribution(memberUUID, LocalDate.now())) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @Transactional
    @PatchMapping("/remove/{memberUUID}")
    public ResponseEntity<?> removeContribution(@PathVariable UUID memberUUID, @RequestParam UUID contributionUUID) {
        if (contributionService.removeContribution(memberUUID, contributionUUID)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}
