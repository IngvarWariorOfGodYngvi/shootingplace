package com.shootingplace.shootingplace.configurations;

import com.shootingplace.shootingplace.users.UserService;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conf")
@CrossOrigin
public class ConfigController {

    private final Environment environment;
    private final UserService userService;

    public ConfigController(Environment environment, UserService userService) {
        this.environment = environment;
        this.userService = userService;
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("zwykły ping"); // test
    }
    @GetMapping("/env")
    public ResponseEntity<?> env() {
        return ResponseEntity.ok(environment.getActiveProfiles()[0]);
    }
    @GetMapping("/fs")
    public ResponseEntity<?> fs() { return userService.checkFirstStart(); }
}
