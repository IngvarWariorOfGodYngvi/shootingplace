package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.models.ShootingPatent;
import com.shootingplace.shootingplace.services.ShootingPatentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patent")
@CrossOrigin
public class ShootingPatentController {

    private final ShootingPatentService shootingPatentService;

    public ShootingPatentController(ShootingPatentService shootingPatentService) {
        this.shootingPatentService = shootingPatentService;
    }

    @PutMapping("/{memberUUID}")
    public ResponseEntity<?> updatePatent(@PathVariable String memberUUID, @RequestBody ShootingPatent shootingPatent) {
        return shootingPatentService.updatePatent(memberUUID, shootingPatent);
    }


}
