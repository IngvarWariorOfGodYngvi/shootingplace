package com.shootingplace.shootingplace.BookOfRegistrationOfStayAtTheShootingPlace;

import com.shootingplace.shootingplace.file.FilesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public ResponseEntity<?> getRecordsFromDate(@RequestParam String firstDate, @RequestParam String secondDate){
        LocalDate firstDateParse = LocalDate.parse(firstDate);
        LocalDate secondDateParse = LocalDate.parse(secondDate);
        return recordsServ.getRecordsFromDate(firstDateParse, secondDateParse);
    }

    @PostMapping("/")
    public ResponseEntity<?> saveToEvidenceBook(@RequestParam("file") MultipartFile file) throws IOException {
        filesService.store(file);
        return ResponseEntity.ok("ok");
    }

}
