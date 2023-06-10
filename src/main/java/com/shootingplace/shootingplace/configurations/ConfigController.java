package com.shootingplace.shootingplace.configurations;

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

    public ConfigController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("192.168.1.145:8080/strzelnica/#/"); // test
    }
    @GetMapping("/env")
    public ResponseEntity<?> env() {
        return ResponseEntity.ok(environment.getActiveProfiles()[0]);
    }
}
