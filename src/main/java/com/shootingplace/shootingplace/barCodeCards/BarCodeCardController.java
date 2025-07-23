package com.shootingplace.shootingplace.barCodeCards;

import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/barCode")
@CrossOrigin
public class BarCodeCardController {

    private final BarCodeCardService barCodeCardService;

    public BarCodeCardController(BarCodeCardService barCodeCardService) {
        this.barCodeCardService = barCodeCardService;
    }

    @PostMapping("/")
    public ResponseEntity<?> addNewCardToPerson(@RequestParam String barCode, @RequestParam String uuid, @RequestParam String pinCode) throws NoUserPermissionException {
        return barCodeCardService.createNewCard(barCode, uuid, pinCode);
    }

}
