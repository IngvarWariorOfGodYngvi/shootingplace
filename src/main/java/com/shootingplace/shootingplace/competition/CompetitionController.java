package com.shootingplace.shootingplace.competition;

import com.shootingplace.shootingplace.tournament.ScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/competition")
@CrossOrigin
public class CompetitionController {
    private final CompetitionService competitionService;
    private final ScoreService scoreService;

    public CompetitionController(CompetitionService competitionService, ScoreService scoreService) {
        this.competitionService = competitionService;
        this.scoreService = scoreService;
    }

    @GetMapping("/")
    public ResponseEntity<List<CompetitionEntity>> getAllCompetitions() {
        return ResponseEntity.ok(competitionService.getAllCompetitions());
    }
    @GetMapping("/competitionMemberListUUID")
    public ResponseEntity<?> getCompetitionMemberList(@RequestParam String competitionMembersListUUID) {
        return competitionService.getCompetitionMemberList(competitionMembersListUUID);
    }

    @PostMapping("")
    public ResponseEntity<?> createCompetition(@RequestBody Competition competition) {
        if (competition.getName().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return competitionService.createNewCompetition(competition);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateCompetition(@RequestParam String uuid, @RequestBody Competition competition) {

        return competitionService.updateCompetition(uuid, competition);
    }

    @PutMapping("/score/set")
    public ResponseEntity<?> setScore(@RequestParam String scoreUUID, @RequestParam float score, @RequestParam float innerTen, @RequestParam float outerTen, @RequestParam int procedures, @RequestParam float alfa, @RequestParam float charlie, @RequestParam float delta,@Nullable @RequestParam List<Float> series) {
        return scoreService.setScore(scoreUUID, score, innerTen, outerTen, alfa, charlie, delta, procedures,series);
    }
    @PutMapping("/score/forceSetScore")
    public ResponseEntity<?> forceSetScore(@RequestParam String scoreUUID, @RequestParam float score){
        return scoreService.forceSetScore(scoreUUID,score);
    }

    @PatchMapping("/score/ammo")
    public ResponseEntity<?> toggleAmmunitionInScore(@RequestParam String scoreUUID) {

        return scoreService.toggleAmmunitionInScore(scoreUUID);
    }

    @PatchMapping("/score/gun")
    public ResponseEntity<?> toggleGunInScore(@RequestParam String scoreUUID) {

        return scoreService.toggleGunInScore(scoreUUID);
    }

    @PatchMapping("/score/dnf")
    public ResponseEntity<?> toggleDnfScore(@RequestParam String scoreUUID) {

        return scoreService.toggleDnfScore(scoreUUID);
    }

    @PatchMapping("/score/dsq")
    public ResponseEntity<?> toggleDsqScore(@RequestParam String scoreUUID) {

        return scoreService.toggleDsqScore(scoreUUID);
    }

    @PatchMapping("/score/pk")
    public ResponseEntity<?> togglePkScore(@RequestParam String scoreUUID) {

        return scoreService.togglePkScore(scoreUUID);
    }

}
