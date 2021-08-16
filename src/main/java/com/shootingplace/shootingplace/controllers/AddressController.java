package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.models.Address;
import com.shootingplace.shootingplace.services.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/address")
@CrossOrigin
public class AddressController {

    private final AddressService addressService;


    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PutMapping("/{memberUUID}")
    public ResponseEntity<?> updateMemberAddress(@PathVariable String memberUUID, @RequestBody Address address) {
       return addressService.updateAddress(memberUUID, address);
    }
}