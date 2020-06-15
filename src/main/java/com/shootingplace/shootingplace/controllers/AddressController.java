package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.models.Address;
import com.shootingplace.shootingplace.services.AddressService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/address")
public class AddressController {

    private final AddressService addressService;


    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/{memberUUID}")
    public boolean addMemberAddress(@PathVariable UUID memberUUID, @RequestBody Address address) {
       return addressService.addAddress(memberUUID,address);

    }
    @PutMapping("/{memberUUID}")
    public boolean updateMemberAddress(@PathVariable UUID memberUUID, @RequestBody Address address){
        return addressService.updateAddress(memberUUID,address);
    }
}