package com.shootingplace.shootingplace.settings;

import com.shootingplace.shootingplace.club.Club;
import com.shootingplace.shootingplace.club.ClubService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    @GetMapping("/termsAndLicense")
    public ResponseEntity<?> termsAndLicense() {
        LocalDate endLicense = LocalDate.of(2026, 8, 30);
        if (LocalDate.now().isBefore(endLicense)) {
            return ResponseEntity.ok("Licencja na program jest ważna do: " + endLicense);
        } else {
            return ResponseEntity.status(403).body("licencja skończyła się: " + endLicense);
        }
    }

}
