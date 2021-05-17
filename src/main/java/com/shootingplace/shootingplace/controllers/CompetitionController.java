package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.entities.CompetitionEntity;
import com.shootingplace.shootingplace.domain.models.Competition;
import com.shootingplace.shootingplace.services.CompetitionService;
import com.shootingplace.shootingplace.services.ScoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("")
    public ResponseEntity<?> createCompetition(@RequestBody Competition competition) {
        if (competition.getName().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (competitionService.createNewCompetition(competition)) {
            return ResponseEntity.status(201).build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/ordering")
    public ResponseEntity<?> updateOrderingNumber(@RequestParam String uuid, @RequestParam String orderNumber) {

        if (competitionService.updateOrderingNumber(uuid, orderNumber)) {
            return ResponseEntity.status(200).build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("")
    public ResponseEntity<?> setScore(@RequestParam String scoreUUID, @RequestParam float score, @RequestParam float innerTen, @RequestParam float outerTen, @RequestParam int procedures, @RequestParam float alfa, @RequestParam float charlie, @RequestParam float delta) {

        if (scoreService.setScore(scoreUUID, score, innerTen, outerTen, alfa, charlie, delta, procedures)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/ammo")
    public ResponseEntity<?> toggleAmmunitionInScore(@RequestParam String scoreUUID) {

        if (scoreService.toggleAmmunitionInScore(scoreUUID)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/gun")
    public ResponseEntity<?> toggleGunInScore(@RequestParam String scoreUUID) {

        if (scoreService.toggleGunInScore(scoreUUID)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
