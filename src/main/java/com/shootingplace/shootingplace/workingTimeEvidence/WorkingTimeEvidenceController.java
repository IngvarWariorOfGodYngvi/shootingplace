package com.shootingplace.shootingplace.workingTimeEvidence;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

        return workService.createNewWTE(number);
    }

//    @GetMapping("/stream-sse-mvc")
//    public SseEmitter streamSseMvc() {
//        SseEmitter emitter = new SseEmitter();
//        ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
//
//        sseMvcExecutor.execute(() -> {
//            try {
//                int i = 0;
//                while (true) {
//                    SseEmitter.SseEventBuilder event = SseEmitter.event()
//                            .data("SSE MVC - " + LocalTime.now()
//                                    .toString())
//                            .id(String.valueOf(i))
//                            .name("sse event - mvc");
//                    emitter.send(event);
//                    Thread.sleep(1000);
//                    i++;
//                }
//            } catch (Exception ex) {
//                emitter.completeWithError(ex);
//            }
//        });
//        return emitter;
//    }

    @GetMapping("/")
    public ResponseEntity<?> getAllActiveUsers() {

        return ResponseEntity.ok(workService.getAllUsersInWork());
    }

    @GetMapping("/month")
    public ResponseEntity<?> getAllWorkingTimeEvidenceInMonth(@Nullable @RequestParam String year, @Nullable @RequestParam String month, @Nullable @RequestParam String workType) {
        if (year == null || year.equals("null") || month == null || month.equals("null")) {
            return ResponseEntity.badRequest().body("Musisz podać Rok i Miesiąc aby wyświetlić wyniki");
        }
        int year1 = Integer.parseInt(year);
        return ResponseEntity.ok(workService.getAllWorkingTimeEvidenceInMonth(year1, month, workType));
    }

    @GetMapping("/workType")
    public ResponseEntity<?> getAllWorkingType() {
        return workService.getAllWorkingType();
    }

    @GetMapping("/getAllWorkingYear")
    public ResponseEntity<?> getAllWorkingYear() {
        return workService.getAllWorkingYear();
    }

    @GetMapping("/getAllWorkingMonthInYear")
    public ResponseEntity<?> getAllWorkingMonthInYear(@RequestParam Integer year) {
        return workService.getAllWorkingMonthInYear(year);
    }
    @GetMapping("/getAllWorkingTypeInMonthAndYear")
    public ResponseEntity<?> getAllWorkingTypeInMonthAndYear(@RequestParam String year,@RequestParam String month) {
        return workService.getAllWorkingTypeInMonthAndYear(year,month);
    }

    @Transactional
    @PatchMapping("/accept")
    public ResponseEntity<?> acceptWorkingTime(@RequestParam List<String> uuidList, @RequestParam String pinCode) {
        return workService.acceptWorkingTime(uuidList, pinCode);
    }

    @Transactional
    @PutMapping("/")
    public ResponseEntity<?> inputChangesToWorkTime(@RequestParam String[] list, @RequestParam String pinCode) {
        List<WorkingTimeEvidenceDTO> list1 = new ArrayList<>();
        for (String s : list) {
            String[] split = s.split(";", 5);

            split[1] = split[1].replace(" ", "T").concat(":00");
            split[2] = split[2].replace(" ", "T").concat(":00");
            list1.add(WorkingTimeEvidenceDTO.builder()
                    .uuid(split[0])
                    .start(LocalDateTime.parse(split[1]))
                    .stop(LocalDateTime.parse(split[2]))
                    .build());

        }

        return workService.inputChangesToWorkTime(list1, pinCode);
    }
}
