package com.shootingplace.shootingplace.competition;

import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.tournament.ScoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/competition")
@CrossOrigin
public class CompetitionController {

    private final CompetitionService competitionService;
    private final ScoreService scoreService;
    private final ChangeHistoryService changeHistoryService;

    public CompetitionController(CompetitionService competitionService, ScoreService scoreService, ChangeHistoryService changeHistoryService) {
        this.competitionService = competitionService;
        this.scoreService = scoreService;
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping("/")
    public ResponseEntity<List<CompetitionEntity>> getAllCompetitions() {
        return ResponseEntity.ok(competitionService.getAllCompetitions());
    }

    @GetMapping("/getCountingMethods")
    public ResponseEntity<?> getCountingMethods() {
        return ResponseEntity.ok(competitionService.getCountingMethods());
    }

    @GetMapping("/getDisciplines")
    public ResponseEntity<?> getDisciplines() {
        return ResponseEntity.ok(competitionService.getDisciplines());
    }

    @GetMapping("/getCompetitionTypes")
    public ResponseEntity<?> getCompetitionTypes() {
        return ResponseEntity.ok(competitionService.getCompetitionTypes());
    }

    @GetMapping("/competitionMemberListUUID")
    public ResponseEntity<?> getCompetitionMemberList(@RequestParam String competitionMembersListUUID) {
        return competitionService.getCompetitionMemberList(competitionMembersListUUID);
    }

    @Transactional
    @PostMapping("")
    public ResponseEntity<?> createCompetition(@RequestBody Competition competition) {
        if (competition.getName().isEmpty()) {
            return ResponseEntity.badRequest().body("Wymyśl jakąś nazwę");
        }
        return competitionService.createNewCompetition(competition);
    }

    @Transactional
    @PutMapping("/update")
    public ResponseEntity<?> updateCompetition(@RequestParam String uuid, @RequestBody Competition competition, @RequestParam String pinCode) throws NoUserPermissionException {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return competitionService.updateCompetition(uuid, competition, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PutMapping("/score/set")
    public ResponseEntity<?> setScore(@RequestParam String scoreUUID, @RequestParam float score, @RequestParam float innerTen, @RequestParam float outerTen, @RequestParam int procedures, @RequestParam float miss, @RequestParam float alfa, @RequestParam float charlie, @RequestParam float delta, @Nullable @RequestParam List<Float> series) {
        return scoreService.setScore(scoreUUID, score, innerTen, outerTen, alfa, charlie, delta, procedures, miss, series);
    }

    @Transactional
    @PutMapping("/score/forceSetScore")
    public ResponseEntity<?> forceSetScore(@RequestParam String scoreUUID, @RequestParam float score) {
        return scoreService.forceSetScore(scoreUUID, score);
    }

    @Transactional
    @PatchMapping("/score/ammo")
    public ResponseEntity<?> toggleAmmunitionInScore(@RequestParam String scoreUUID) {

        return scoreService.toggleAmmunitionInScore(scoreUUID);
    }

    @Transactional
    @PatchMapping("/score/gun")
    public ResponseEntity<?> toggleGunInScore(@RequestParam String scoreUUID) {

        return scoreService.toggleGunInScore(scoreUUID);
    }

    @Transactional
    @PatchMapping("/score/dnf")
    public ResponseEntity<?> toggleDnfScore(@RequestParam String scoreUUID) {

        return scoreService.toggleDnfScore(scoreUUID);
    }

    @Transactional
    @PatchMapping("/score/dsq")
    public ResponseEntity<?> toggleDsqScore(@RequestParam String scoreUUID) {

        return scoreService.toggleDsqScore(scoreUUID);
    }

    @Transactional
    @PatchMapping("/score/pk")
    public ResponseEntity<?> togglePkScore(@RequestParam String scoreUUID) {

        return scoreService.togglePkScore(scoreUUID);
    }

}
