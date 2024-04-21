package com.shootingplace.shootingplace.settings;

import com.shootingplace.shootingplace.club.Club;
import com.shootingplace.shootingplace.club.ClubService;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/settings")
@CrossOrigin
public class SettingsController {

    private final ClubService clubService;
    private final Environment environment;

    public SettingsController(ClubService clubService, Environment environment) {
        this.clubService = clubService;
        this.environment = environment;
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
        LocalDate endLicense = LocalDate.parse(Objects.requireNonNull(environment.getProperty("licenseDate")));
        boolean isEnd = LocalDate.now().isAfter(endLicense);
        Map<String,String> map = new HashMap<>();
        map.put("message", !isEnd?"Licencja na program jest ważna do: " + endLicense:"licencja skończyła się: " + endLicense);
        map.put("isEnd", String.valueOf(isEnd));
        map.put("endDate", String.valueOf(endLicense));
        if (!isEnd) {
            return ResponseEntity.ok(map);
        } else {
            return ResponseEntity.badRequest().body(map);
        }
    }

}
