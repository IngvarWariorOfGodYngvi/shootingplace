package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.entities.ClubEntity;
import com.shootingplace.shootingplace.domain.models.Club;
import com.shootingplace.shootingplace.services.ClubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/club")
@CrossOrigin
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @GetMapping("/")
    public ResponseEntity<List<ClubEntity>> getAllClubs() {
        return ResponseEntity.ok(clubService.getAllClubs());
    }

    @GetMapping("/tournament")
    public ResponseEntity<List<String>> getAllClubsToTournament() {
        return ResponseEntity.ok(clubService.getAllClubsToTournament());
    }

    @PutMapping("/{clubID}")
    public ResponseEntity<?> updateClub(@PathVariable int clubID, @RequestBody Club club) {
        if (clubService.updateClub(clubID, club)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
