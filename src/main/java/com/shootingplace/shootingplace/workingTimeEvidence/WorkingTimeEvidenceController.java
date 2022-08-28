package com.shootingplace.shootingplace.workingTimeEvidence;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/work")
@CrossOrigin
public class WorkingTimeEvidenceController {

    private final WorkingTimeEvidenceService workService;

    public WorkingTimeEvidenceController(WorkingTimeEvidenceService workService) {
        this.workService = workService;
    }

    @PostMapping("/")
    public ResponseEntity<?> startStopWork(@RequestParam String number) {

        String newWTE = workService.createNewWTE(number);
        return ResponseEntity.ok(newWTE);
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllActiveUsers() {

        return ResponseEntity.ok(workService.getAllUsersInWork());
    }

    @GetMapping("/month")
    public ResponseEntity<?> getAllWorkingTimeEvidenceInMonth(@Nullable @RequestParam String month,@Nullable @RequestParam String workType) {
        return ResponseEntity.ok(workService.getAllWorkingTimeEvidenceInMonth(month,workType));
    }
    @GetMapping("/workType")
    public ResponseEntity<?> getAllWorkingType(){
        return workService.getAllWorkingType();
    }

    @PatchMapping("/accept")
    public ResponseEntity<?> acceptWorkingTime(@RequestParam List<String> uuidList, @RequestParam String pinCode) {
        return workService.acceptWorkingTime(uuidList, pinCode);
    }
    @Transactional
    @PutMapping("/")
    public ResponseEntity<?> inputChangesToWorkTime(@RequestParam String[] list,@RequestParam String pinCode) {
        List<WorkingTimeEvidenceDTO> list1 = new ArrayList<>();
        for (String s : list) {
            String[] split = s.split(";", 5);
            Arrays.stream(split).forEach(System.out::println);

            split[1] = split[1].replace(" ", "T").concat(":00");
            split[2] = split[2].replace(" ", "T").concat(":00");
            System.out.println(split[1]);
            System.out.println(split[2]);
            list1.add(WorkingTimeEvidenceDTO.builder()
                    .uuid(split[0])
                    .start(LocalDateTime.parse(split[1]))
                    .stop(LocalDateTime.parse(split[2]))
                    .build());

        }

        return workService.inputChangesToWorkTime(list1,pinCode);
    }
}
