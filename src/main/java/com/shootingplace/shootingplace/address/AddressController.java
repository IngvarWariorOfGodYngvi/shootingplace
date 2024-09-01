package com.shootingplace.shootingplace.address;

import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.NoPermissionException;

@RestController
@RequestMapping("/address")
@CrossOrigin
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PutMapping("/{memberUUID}")
    public ResponseEntity<?> updateMemberAddress(@PathVariable String memberUUID, @RequestBody Address address, @RequestParam String pinCode) throws NoPermissionException, NoUserPermissionException {
       return addressService.updateAddress(memberUUID, address, pinCode);
    }
}