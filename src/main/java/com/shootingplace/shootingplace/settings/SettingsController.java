package com.shootingplace.shootingplace.settings;

import com.shootingplace.shootingplace.club.Club;
import com.shootingplace.shootingplace.club.ClubService;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.users.UserSubType;
import com.shootingplace.shootingplace.utils.update.RunPowerShell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/settings")
@CrossOrigin
public class SettingsController {

    private final ClubService clubService;
    private final Environment environment;
    private final ChangeHistoryService changeHistoryService;
    private final Logger LOG = LogManager.getLogger();


    public SettingsController(ClubService clubService, Environment environment, ChangeHistoryService changeHistoryService) {
        this.clubService = clubService;
        this.environment = environment;
        this.changeHistoryService = changeHistoryService;
    }

    @Transactional
    @PostMapping("/createMotherClub")
    public ResponseEntity<?> createMotherClub(@RequestBody Club club) {
        return clubService.createMotherClub(club);
    }

    @GetMapping("/termsAndLicense")
    public ResponseEntity<?> termsAndLicense() {
        LocalDate endLicense = LocalDate.parse(Objects.requireNonNull(environment.getProperty("licenseDate")));
        boolean isEnd = LocalDate.now().isAfter(endLicense);
        Map<String, String> map = new HashMap<>();
        map.put("message", !isEnd ? "Licencja na program jest ważna do: " + endLicense : "licencja skończyła się: " + endLicense);
        map.put("isEnd", String.valueOf(isEnd));
        map.put("endDate", String.valueOf(endLicense));
        if (!isEnd) {
            return ResponseEntity.ok(map);
        } else {
            return ResponseEntity.badRequest().body(map);
        }
    }

    @Transactional
    @PostMapping("/update")
    public ResponseEntity<?> updateProgram(@RequestParam String pinCode) throws IOException, NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.ADMIN.getName(), UserSubType.SUPER_USER.getName(),UserSubType.CEO.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode,acceptedPermissions);
        new RunPowerShell(code);
        return code;


    }


}
