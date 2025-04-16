package com.shootingplace.shootingplace.club;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

    @GetMapping("/member")
    public ResponseEntity<List<ClubEntity>> getAllClubsToMember() {
        return ResponseEntity.ok(clubService.getAllClubsToMember());
    }
@Transactional
    @PutMapping("/{clubID}")
    public ResponseEntity<?> updateClub(@PathVariable int clubID, @RequestBody Club club) {
        if (clubService.updateClub(clubID, club)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @Transactional
    @PostMapping("/create")
    public ResponseEntity<?> createNewClub(@RequestBody Club clubName){
        return clubService.createNewClub(clubName);
    }
}
