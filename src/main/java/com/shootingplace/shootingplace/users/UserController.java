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

    @GetMapping("/getAccess")
    public ResponseEntity<?> getAccess(@RequestParam String pinCode){
        return userService.getAccess(pinCode);
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
    @GetMapping("/userActions")
    public ResponseEntity<?> getUserActions(@RequestParam String uuid) {
        return userService.getUserActions(uuid);
    }

    @PostMapping("/createSuperUser")
    public ResponseEntity<?> createSuperUser(@RequestParam String firstName, @RequestParam String secondName,  @RequestParam String subType, @RequestParam String pinCode, @RequestParam String superPinCode,@RequestParam @Nullable String memberUUID) {
        return userService.createSuperUser(firstName, secondName, subType, pinCode, superPinCode, memberUUID);
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestParam String firstName, @RequestParam String secondName, @RequestParam String subType, @RequestParam String pinCode, @RequestParam String superPinCode,@RequestParam @Nullable String memberUUID) {
        return userService.createUser(firstName, secondName, subType, pinCode, superPinCode, memberUUID);
    }

    @GetMapping("/checkPinCode")
    public ResponseEntity<?> checkPinCode(@RequestParam String pinCode) {
        return userService.checkPinCode(pinCode);
    }
    @PutMapping("/setSuperUser")
    public ResponseEntity<?> setSuperUser(@RequestParam String uuid,@RequestParam String pinCode) {
        return userService.setSuperUser(uuid,pinCode);
    }

}
