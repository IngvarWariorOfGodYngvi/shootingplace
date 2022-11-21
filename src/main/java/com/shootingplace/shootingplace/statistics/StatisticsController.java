package com.shootingplace.shootingplace.statistics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/statistics")
@CrossOrigin
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }


    @GetMapping("/contributionSum")
    public ResponseEntity<?> getContributionSum(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(statisticsService.getContributionSum(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/joinDateSum")
    public ResponseEntity<?> getJoinDateSum(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(statisticsService.getJoinDateSum(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/erasedSum")
    public ResponseEntity<?> getErasedMembersSum(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(statisticsService.getErasedMembersSum(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/licenseSum")
    public ResponseEntity<?> getLicenseSum(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(statisticsService.getLicenseSum(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/joinMonthSum")
    public ResponseEntity<?> joinMonthSum(@RequestParam String year) {
        LocalDate parseYear = LocalDate.parse(year);
        return ResponseEntity.ok(statisticsService.joinMonthSum(parseYear.getYear()));
    }
    @GetMapping("/maxLegNumber")
    public ResponseEntity<?> getMaxLegNumber(){
        return ResponseEntity.ok(statisticsService.getMaxLegNumber());
    }

    @GetMapping("/actualYearMemberCounts")
    public ResponseEntity<?> getActualYearMemberCounts() {
        return ResponseEntity.ok(statisticsService.getActualYearMemberCounts());
    }
    @GetMapping("/memberAmmoTakesInTime")
    public ResponseEntity<?> getMembersAmmoTakesInTime(@RequestParam String firstDate, @RequestParam String secondDate){
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(statisticsService.getMembersAmmoTakesInTime(parseFirstDate,parseSecondDate));
    }
    @GetMapping("/personal")
    public ResponseEntity<?> getPersonalStatistic(@RequestParam String uuid){
        return statisticsService.getPersonalStatistics(uuid);
    }
}
