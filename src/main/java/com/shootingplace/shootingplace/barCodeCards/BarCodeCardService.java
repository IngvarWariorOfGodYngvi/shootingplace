package com.shootingplace.shootingplace.barCodeCards;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class BarCodeCardService {

    private final BarCodeCardRepository barCodeCardRepo;

    public BarCodeCardService(BarCodeCardRepository barCodeCardRepo) {
        this.barCodeCardRepo = barCodeCardRepo;
    }

    public ResponseEntity<?> createNewCard(BarCodeCardDTO dto) {

        if (barCodeCardRepo.existsByBarCode(dto.getBarCode())) {
            return ResponseEntity.badRequest().body("Taki numer karty jest już do kogoś przypisany\nużyj innej karty");
        }

        BarCodeCardEntity build = BarCodeCardEntity.builder()
                .barCode(dto.getBarCode())
                .belongsTo(dto.getBelongsTo())
                .isActive(true)
                .build();

        barCodeCardRepo.save(build);
        return ResponseEntity.ok("Zapisano numer i przypisano do: ");
    }
}
