package com.shootingplace.shootingplace.workingTimeEvidence;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/work")
@CrossOrigin
public class WorkingTimeEvidenceController {

    private final WorkingTimeEvidenceService workService;

    public WorkingTimeEvidenceController(WorkingTimeEvidenceService workService) {
        this.workService = workService;
    }

    @PostMapping("/")
    public ResponseEntity<?> startStopWork(@RequestParam String number){

        String newWTE = workService.createNewWTE(number);
        return ResponseEntity.ok(newWTE);
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllActiveUsers(){

       return ResponseEntity.ok(workService.getAllUsersInWork());
    }

    @GetMapping("/month")
    public ResponseEntity<?> getAllWorkingTimeEvidenceInMonth(@RequestParam String month){
        return ResponseEntity.ok(workService.getAllWorkingTimeEvidenceInMonth(month));
    }
}
