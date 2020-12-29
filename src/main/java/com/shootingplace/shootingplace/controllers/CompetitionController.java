package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.entities.CompetitionEntity;
import com.shootingplace.shootingplace.domain.models.Competition;
import com.shootingplace.shootingplace.services.CompetitionService;
import com.shootingplace.shootingplace.services.ScoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @PostMapping("/")
    public ResponseEntity<?> createCompetition(@RequestBody Competition competition) {
        if (competitionService.createNewCompetition(competition)) {
            return ResponseEntity.status(201).build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("")
    public ResponseEntity<?> setScore(@RequestParam UUID scoreUUID, @RequestParam float score) {
        if (scoreService.setScore(scoreUUID, score)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
