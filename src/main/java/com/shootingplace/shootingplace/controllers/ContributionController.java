package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.models.Contribution;
import com.shootingplace.shootingplace.services.ContributionService;
import com.shootingplace.shootingplace.services.HistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/contribution")
@CrossOrigin
public class ContributionController {

    private final ContributionService contributionService;
    private final HistoryService historyService;

    public ContributionController(ContributionService contributionService, HistoryService historyService) {
        this.contributionService = contributionService;
        this.historyService = historyService;
    }

    @PostMapping("/{memberUUID}")
    public void addContribution(@PathVariable UUID memberUUID, @RequestBody Contribution contribution) {
        contributionService.addContribution(memberUUID, contribution);
    }

    @PutMapping("/{memberUUID}")
    public void updateContribution(@PathVariable UUID memberUUID, @RequestBody Contribution contribution) {
        contributionService.updateContribution(memberUUID, contribution);
    }

    @PatchMapping("/{memberUUID}")
    public boolean prolongContribution(@PathVariable UUID memberUUID) {
        return contributionService.prolongContribution(memberUUID);
    }

    @PutMapping("/history{memberUUID}")
    public boolean addHistoryContributionRecord(@PathVariable UUID memberUUID, @RequestParam String date) {
        return historyService.addContributionRecord(memberUUID, date);
    }
}
