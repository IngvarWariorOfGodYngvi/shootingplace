package com.shootingplace.shootingplace.barCodeCards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/barCode")
@CrossOrigin
public class BarCodeCardController {

    @Autowired
    BarCodeCardService barCodeCardService;

    @PostMapping("/")
    public ResponseEntity<?> addNewCardToPerson(@RequestBody BarCodeCardDTO dto){
        return barCodeCardService.createNewCard(dto);
    }
    @GetMapping("/")
    public ResponseEntity<?> findAdminMasterCode(@RequestParam String code) {
        return barCodeCardService.findAdminCode(code);
    }

}
