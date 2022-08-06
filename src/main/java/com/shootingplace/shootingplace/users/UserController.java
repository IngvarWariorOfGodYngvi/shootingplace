package com.shootingplace.shootingplace.users;

import org.springframework.http.ResponseEntity;
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

    @PostMapping("/createSuperUser")
    public ResponseEntity<?> createSuperUser(@RequestParam String name, @RequestParam String pinCode) {
        return userService.createSuperUser(name, pinCode);
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestParam String name, @RequestParam String pinCode, @RequestParam String superPinCode) {
        return userService.createUser(name, pinCode, superPinCode);
    }

}
