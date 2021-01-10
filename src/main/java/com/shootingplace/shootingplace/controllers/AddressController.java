package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.models.Address;
import com.shootingplace.shootingplace.services.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/address")
@CrossOrigin(origins = "https://localhost:8081")
public class AddressController {

    private final AddressService addressService;


    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PutMapping("/{memberUUID}")
    public ResponseEntity<?> updateMemberAddress(@PathVariable UUID memberUUID, @RequestBody Address address) {
        if (addressService.updateAddress(memberUUID, address)) {
            return ResponseEntity.ok().build();
        } else
            return ResponseEntity.status(418).body("I'm a teapot");
    }
}