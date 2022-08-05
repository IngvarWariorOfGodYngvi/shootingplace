package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.models.Club;
import com.shootingplace.shootingplace.services.ClubService;
import com.shootingplace.shootingplace.users.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
@CrossOrigin
public class SettingsController {

    private final UserService userService;
    private final ClubService clubService;

    public SettingsController(UserService userService, ClubService clubService) {
        this.userService = userService;
        this.clubService = clubService;
    }

    @GetMapping("/superUserList")
    public ResponseEntity<?> getListOfSuperUser() {
        return ResponseEntity.ok(userService.getListOfSuperUser());
    }

    @GetMapping("/userList")
    public ResponseEntity<?> getListOfUser() {
        return ResponseEntity.ok(userService.getListOfUser());
    }

    @PostMapping("/createSuperUser")
    public ResponseEntity<?> createSuperUser(@RequestParam String name, @RequestParam String pinCode) {
        return userService.createSuperUser(name, pinCode);
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestParam String name, @RequestParam String pinCode, @RequestParam String superPinCode) {
        return userService.createUser(name, pinCode, superPinCode);
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
