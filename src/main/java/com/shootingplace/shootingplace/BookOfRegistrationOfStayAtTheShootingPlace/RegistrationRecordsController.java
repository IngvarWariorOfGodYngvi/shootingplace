package com.shootingplace.shootingplace.BookOfRegistrationOfStayAtTheShootingPlace;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/evidence")
@CrossOrigin
public class RegistrationRecordsController {

    private final RegistrationRecordsService recordsServ;

    public RegistrationRecordsController(RegistrationRecordsService recordsServ) {
        this.recordsServ = recordsServ;
    }

    @GetMapping("/")
    public ResponseEntity<?> getRecordsFromDate(@RequestParam String date){
        LocalDate date1 = LocalDate.parse(date);
        if (date.equals("null")){
            date1 = LocalDate.now();
        }
        return recordsServ.getRecordsFromDate(date1);
    }

}
