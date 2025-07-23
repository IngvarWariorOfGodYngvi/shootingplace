package com.shootingplace.shootingplace.club;

import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.users.UserSubType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/club")
@CrossOrigin
public class ClubController {

    private final ClubService clubService;
    private final ChangeHistoryService changeHistoryService;

    public ClubController(ClubService clubService, ChangeHistoryService changeHistoryService) {
        this.clubService = clubService;
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping("/")
    public ResponseEntity<List<ClubEntity>> getAllClubs() {
        return ResponseEntity.ok(clubService.getAllClubs());
    }

    @GetMapping("/isMotherClubExist")
    public ResponseEntity<?> getClubsCount() {
        return ResponseEntity.ok(clubService.getClubsCount());
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
        return clubService.updateClub(clubID, club);
    }

    @Transactional
    @PostMapping("/import")
    public ResponseEntity<?> importClub(@RequestBody Club club) {
        return clubService.importCLub(club);
    }

    @Transactional
    @PostMapping("/create")
    public ResponseEntity<?> createNewClub(@RequestBody Club club) {
        return clubService.createNewClub(club);
    }

    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteClub(@RequestParam String id, String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = List.of(UserSubType.MANAGEMENT.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return clubService.deleteClub(Integer.parseInt(id), pinCode);
        } else {
            return code;
        }
    }
}
