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
    public ResponseEntity<?> getRecordsFromDate(@RequestParam String date){
        LocalDate date1 = LocalDate.parse(date);
        if (date.equals("null")){
            date1 = LocalDate.now();
        }
        return recordsServ.getRecordsFromDate(date1);
    }

    @PostMapping("/")
    public ResponseEntity<?> saveToEvidenceBook(@RequestParam("file") MultipartFile file) throws IOException {
        filesService.store(file);
        System.out.println("zapisałem plik");
        return ResponseEntity.ok("ok");
    }

}
