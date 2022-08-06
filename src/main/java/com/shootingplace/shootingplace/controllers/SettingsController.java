package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.models.Club;
import com.shootingplace.shootingplace.services.ClubService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
@CrossOrigin
public class SettingsController {

    private final ClubService clubService;

    public SettingsController(ClubService clubService) {
        this.clubService = clubService;
    }

    @Transactional
    @PostMapping("/createMotherClub")
    public ResponseEntity<?> createMotherClub(@RequestBody Club club) {
        if (clubService.createMotherClub(club)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
