package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.models.Tournament;
import com.shootingplace.shootingplace.domain.models.TournamentDTO;
import com.shootingplace.shootingplace.services.ChangeHistoryService;
import com.shootingplace.shootingplace.services.TournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tournament")
@CrossOrigin
public class TournamentController {

    private final TournamentService tournamentService;
    private final ChangeHistoryService changeHistoryService;

    public TournamentController(TournamentService tournamentService, ChangeHistoryService changeHistoryService) {
        this.tournamentService = tournamentService;
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<Tournament>> getListOfTournaments() {
        return ResponseEntity.ok(tournamentService.getListOfTournaments());
    }

    @GetMapping("/closedList")
    public ResponseEntity<List<TournamentDTO>> getListOfClosedTournaments() {
        return ResponseEntity.ok().body(tournamentService.getClosedTournaments());
    }

    @GetMapping("/competitions")
    public ResponseEntity<List<String>> getCompetitionsListInTournament(@RequestParam String tournamentUUID) {
        return ResponseEntity.ok().body(tournamentService.getCompetitionsListInTournament(tournamentUUID));
    }

    @GetMapping("/stat")
    public ResponseEntity<List<String>> getStatistics(@RequestParam String tournamentUUID) {
        return ResponseEntity.ok().body(tournamentService.getStatistics(tournamentUUID));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAnyOpenTournament() {
        return ResponseEntity.ok().body(tournamentService.checkAnyOpenTournament());
    }

    @PostMapping("/")
    public ResponseEntity<String> addNewTournament(@RequestBody Tournament tournament) {
        return ResponseEntity.status(201).body(tournamentService.createNewTournament(tournament));
    }

    @PostMapping("/removeArbiter/{tournamentUUID}")
    public ResponseEntity<?> removeArbiterFromTournament(@PathVariable String tournamentUUID, @RequestParam int number, @RequestParam int id) {

        if (number > 0) {
            if (tournamentService.removeArbiterFromTournament(tournamentUUID, number)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(418).body("I'm a teapot");
            }
        }
        if (id > 0) {
            if (tournamentService.removeOtherArbiterFromTournament(tournamentUUID, id)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(418).body("I'm a teapot");
            }
        } else {
            return ResponseEntity.status(418).body("I'm a teapot");
        }
    }

    @PostMapping("/removeRTSArbiter/{tournamentUUID}")
    public ResponseEntity<?> removeRTSArbiterFromTournament(@PathVariable String tournamentUUID, @RequestParam int number, @RequestParam int id) {

        if (number > 0) {
            if (tournamentService.removeRTSArbiterFromTournament(tournamentUUID, number)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(418).body("I'm a teapot");
            }
        }
        if (id > 0) {
            if (tournamentService.removeRTSOtherArbiterFromTournament(tournamentUUID, id)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(418).body("I'm a teapot");
            }
        } else {
            return ResponseEntity.status(418).body("I'm a teapot");
        }
    }

    @PatchMapping("/{tournamentUUID}")
    public ResponseEntity<?> closeTournament(@PathVariable String tournamentUUID) {
        if (tournamentService.closeTournament(tournamentUUID)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(418).body("I'm a teapot");
        }
    }

    @PatchMapping("/open/{tournamentUUID}")
    public ResponseEntity<?> openTournament(@PathVariable String tournamentUUID, @RequestParam String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {
            if (tournamentService.openTournament(tournamentUUID, pinCode)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(418).body("I'm a teapot");
            }
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
    }

    @PutMapping("/{tournamentUUID}")
    public ResponseEntity<?> updateTournament(@PathVariable String tournamentUUID, @RequestBody Tournament tournament) {
        if (tournamentService.updateTournament(tournamentUUID, tournament)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/addMainArbiter/{tournamentUUID}")
    public ResponseEntity<?> addMainArbiter(@PathVariable String tournamentUUID, @RequestParam int number, @RequestParam int id) {

        if (number > 0) {
            if (tournamentService.addMainArbiter(tournamentUUID, number)) {
                return ResponseEntity.ok().build();
            }
        }
        if (id > 0) {
            if (tournamentService.addOtherMainArbiter(tournamentUUID, id)) {
                return ResponseEntity.ok().build();
            }
        }

        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/addRTSArbiter/{tournamentUUID}")
    public ResponseEntity<?> addRTSArbiter(@PathVariable String tournamentUUID, @RequestParam int number, @RequestParam int id) {

        if (number > 0) {
            if (tournamentService.addRTSArbiter(tournamentUUID, number)) {
                return ResponseEntity.ok().build();
            }
        }
        if (id > 0) {
            if (tournamentService.addOtherRTSArbiter(tournamentUUID, id)) {
                return ResponseEntity.ok().build();
            }
        }

        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/addOthersArbiters/{tournamentUUID}")
    public ResponseEntity<?> addOthersArbiters(@PathVariable String tournamentUUID, @RequestParam int number, @RequestParam int id) {

        if (number > 0) {
            if (tournamentService.addOthersArbiters(tournamentUUID, number)) {
                return ResponseEntity.ok().build();
            }
        }
        if (id > 0) {
            if (tournamentService.addPersonOthersArbiters(tournamentUUID, id)) {
                return ResponseEntity.ok().build();
            }
        }

        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/addOthersRTSArbiters/{tournamentUUID}")
    public ResponseEntity<?> addOthersRTSArbiters(@PathVariable String tournamentUUID, @RequestParam int number, @RequestParam int id) {

        if (number > 0) {
            if (tournamentService.addOthersRTSArbiters(tournamentUUID, number)) {
                return ResponseEntity.ok().build();
            }
        }
        if (id > 0) {
            if (tournamentService.addPersonOthersRTSArbiters(tournamentUUID, id)) {
                return ResponseEntity.ok().build();
            }
        }

        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/addCompetition/{tournamentUUID}")
    public ResponseEntity<?> addCompetitionListToTournament(@PathVariable String tournamentUUID, @RequestParam String competitionUUID) {
        if (tournamentService.addNewCompetitionListToTournament(tournamentUUID, competitionUUID)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @DeleteMapping("/delete/{tournamentUUID}")
    public ResponseEntity<?> deleteTournament(@PathVariable String tournamentUUID) {
        if (tournamentService.deleteTournament(tournamentUUID)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
