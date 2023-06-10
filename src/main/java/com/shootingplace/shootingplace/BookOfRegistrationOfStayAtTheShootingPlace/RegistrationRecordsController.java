package com.shootingplace.shootingplace.BookOfRegistrationOfStayAtTheShootingPlace;

import com.shootingplace.shootingplace.file.FilesService;
import com.shootingplace.shootingplace.wrappers.ImageOtherPersonWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/evidence")
@CrossOrigin
public class RegistrationRecordsController {

    private final RegistrationRecordsService recordsServ;
    private final FilesService filesService;

    public RegistrationRecordsController(RegistrationRecordsService recordsServ, FilesService filesService) {
        this.recordsServ = recordsServ;
        this.filesService = filesService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getRecordsFromDate(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate firstDateParse = LocalDate.parse(firstDate);
        LocalDate secondDateParse = LocalDate.parse(secondDate);
        return recordsServ.getRecordsFromDate(firstDateParse, secondDateParse);
    }

    @Transactional
    @PostMapping("/")
    public ResponseEntity<?> saveToEvidenceBook(@RequestBody String imageString, @RequestParam String pesel) {
        String imegeUUID = filesService.storeImageEvidenceBook(imageString);
        return recordsServ.createRecordInBook(pesel, imegeUUID);
    }
    @Transactional
    @PostMapping("/other")
    public ResponseEntity<?> saveToEvidenceBookNonMember(@Nullable @RequestParam String phone, @Nullable @RequestBody ImageOtherPersonWrapper other, @Nullable @RequestParam String club, @RequestParam Boolean rememberMe) {
        String imegeUUID = filesService.storeImageEvidenceBook(other.getImageString());
        return recordsServ.createRecordInBook(imegeUUID,phone, other,club,rememberMe);
    }

}
