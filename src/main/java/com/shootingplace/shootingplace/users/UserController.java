package com.shootingplace.shootingplace.users;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/superUserList")
    public ResponseEntity<?> getListOfSuperUser() {
        return ResponseEntity.ok(userService.getListOfSuperUser());
    }

    @GetMapping("/userList")
    public ResponseEntity<?> getListOfUser() {
        return ResponseEntity.ok(userService.getListOfUser());
    }

    @GetMapping("/allUsers")
    public ResponseEntity<?> getListOfAllUsers(@Nullable @RequestParam String type) {
        return ResponseEntity.ok(userService.getListOfAllUsersNoAdmin(type));
    }

    @PostMapping("/createSuperUser")
    public ResponseEntity<?> createSuperUser(@RequestParam String firstName, @RequestParam String secondName, @RequestParam String pinCode) {
        return userService.createSuperUser(firstName, secondName, pinCode);
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestParam String firstName, @RequestParam String secondName, @RequestParam String subType, @RequestParam String pinCode, @RequestParam String superPinCode) {
        return userService.createUser(firstName, secondName, subType, pinCode, superPinCode);
    }

    @GetMapping("/checkPinCode")
    public ResponseEntity<?> checkPinCode(@RequestParam String pinCode, @RequestParam String uuid) {
        return userService.checkPinCode(pinCode, uuid);
    }

}
