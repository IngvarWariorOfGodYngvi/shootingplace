package com.shootingplace.shootingplace.address;

import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/address")
@CrossOrigin
public class AddressController {

    private final AddressService addressService;
    private final ChangeHistoryService changeHistoryService;

    public AddressController(AddressService addressService, ChangeHistoryService changeHistoryService) {
        this.addressService = addressService;
        this.changeHistoryService = changeHistoryService;
    }

    @Transactional
    @PutMapping("/{memberUUID}")
    public ResponseEntity<?> updateMemberAddress(@PathVariable String memberUUID, @RequestBody Address address, @RequestParam String pinCode) throws NoUserPermissionException {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return addressService.updateAddress(memberUUID, address, pinCode);
        } else {
            return code;
        }
    }
}