package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.services.ArmoryService;
import com.shootingplace.shootingplace.services.ChangeHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/tournament")
@CrossOrigin
public class TournamentController {

    private final TournamentService tournamentService;
    private final ChangeHistoryService changeHistoryService;
    private final ArmoryService armoryService;

    public TournamentController(TournamentService tournamentService, ChangeHistoryService changeHistoryService, ArmoryService armoryService) {
        this.tournamentService = tournamentService;
        this.changeHistoryService = changeHistoryService;
        this.armoryService = armoryService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> getListOfTournaments() {
        return tournamentService.getOpenTournament();
    }

    @GetMapping("/gunList")
    public ResponseEntity<?> getListOfGunsOnTournament(@RequestParam String tournamentUUID) {
        return ResponseEntity.ok(tournamentService.getListOfGunsOnTournament(tournamentUUID));
    }

    @GetMapping("/closedList")
    public ResponseEntity<?> getListOfClosedTournaments() {
        return ResponseEntity.ok().body(tournamentService.getClosedTournaments());
    }

    @GetMapping("/competitions")
    public ResponseEntity<?> getCompetitionsListInTournament(@RequestParam String tournamentUUID) {
        return ResponseEntity.ok().body(tournamentService.getCompetitionsListInTournament(tournamentUUID));
    }

    @GetMapping("/stat")
    public ResponseEntity<?> getStatistics(@RequestParam String tournamentUUID) {
        return ResponseEntity.ok().body(tournamentService.getStatistics(tournamentUUID));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAnyOpenTournament() {
        return ResponseEntity.ok().body(tournamentService.checkAnyOpenTournament());
    }

    @GetMapping("/getGunInTournament")
    public ResponseEntity<?> getGunInTournament(@RequestParam String tournamentUUID) {
        return ResponseEntity.ok(armoryService.getGunInTournament(tournamentUUID));
    }

    @PostMapping("/")
    public ResponseEntity<String> addNewTournament(@RequestBody Tournament tournament) {
        return tournamentService.createNewTournament(tournament);
    }

    @PostMapping("/addGunToTournament")
    public ResponseEntity<?> addGunToTournament(@RequestParam String barcode, @RequestParam String tournamentUUID) {
        return armoryService.addUsedHistoryToGunInTournament(barcode, tournamentUUID);
    }

    @PostMapping("/removeArbiter/{tournamentUUID}")
    public ResponseEntity<?> removeArbiterFromTournament(@PathVariable String tournamentUUID, @RequestParam int number, @RequestParam int id) {

        if (number > 0) {
            return tournamentService.removeArbiterFromTournament(tournamentUUID, number);

        }
        if (id > 0) {
            return tournamentService.removeOtherArbiterFromTournament(tournamentUUID, id);
        } else {
            return ResponseEntity.status(418).body("\"I'm a teapot\"");
        }
    }

    @PostMapping("/removeRTSArbiter/{tournamentUUID}")
    public ResponseEntity<?> removeRTSArbiterFromTournament(@PathVariable String tournamentUUID, @RequestParam int number, @RequestParam int id) {

        if (number > 0) {
            return tournamentService.removeRTSArbiterFromTournament(tournamentUUID, number);
        }
        if (id > 0) {
            return tournamentService.removeRTSOtherArbiterFromTournament(tournamentUUID, id);
        } else {
            return ResponseEntity.status(418).body("I'm a teapot");
        }
    }

    @Transactional
    @PatchMapping("/{tournamentUUID}")
    public ResponseEntity<?> closeTournament(@PathVariable String tournamentUUID) {
        return tournamentService.closeTournament(tournamentUUID);
    }

    @Transactional
    @PatchMapping("/open/{tournamentUUID}")
    public ResponseEntity<?> openTournament(@PathVariable String tournamentUUID, @RequestParam String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {
            return tournamentService.openTournament(tournamentUUID, pinCode);
        } else {
            return ResponseEntity.status(418).body("I'm a Teapot");
        }
    }

    @Transactional
    @PutMapping("/{tournamentUUID}")
    public ResponseEntity<?> updateTournament(@PathVariable String tournamentUUID, @RequestBody Tournament tournament) {
        if (tournamentService.updateTournament(tournamentUUID, tournament)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Transactional
    @PutMapping("/addMainArbiter/{tournamentUUID}")
    public ResponseEntity<?> addMainArbiter(@PathVariable String tournamentUUID, @RequestParam int number, @RequestParam int id) {

        if (number > 0) {
            return tournamentService.addMainArbiter(tournamentUUID, number);
        }
        if (id > 0) {
            return tournamentService.addOtherMainArbiter(tournamentUUID, id);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @PutMapping("/addRTSArbiter/{tournamentUUID}")
    public ResponseEntity<?> addRTSArbiter(@PathVariable String tournamentUUID, @RequestParam int number, @RequestParam int id) {

        if (number > 0) {
            return tournamentService.addRTSArbiter(tournamentUUID, number);
        }
        if (id > 0) {
            return tournamentService.addOtherRTSArbiter(tournamentUUID, id);
        } else {
            return ResponseEntity.badRequest().build();
        }

    }

    @Transactional
    @PutMapping("/addOthersArbiters/{tournamentUUID}")
    public ResponseEntity<?> addOthersArbiters(@PathVariable String tournamentUUID, @RequestParam int number, @RequestParam int id) {

        if (number > 0) {
            return tournamentService.addOthersArbiters(tournamentUUID, number);
        }
        if (id > 0) {
            return tournamentService.addPersonOthersArbiters(tournamentUUID, id);
        } else {
            return ResponseEntity.badRequest().build();
        }

    }

    @Transactional
    @PutMapping("/addOthersRTSArbiters/{tournamentUUID}")
    public ResponseEntity<?> addOthersRTSArbiters(@PathVariable String tournamentUUID, @RequestParam int number, @RequestParam int id) {

        if (number > 0) {
            return tournamentService.addOthersRTSArbiters(tournamentUUID, number);
        }
        if (id > 0) {
            return tournamentService.addPersonOthersRTSArbiters(tournamentUUID, id);
        } else {
            return ResponseEntity.badRequest().build();
        }

    }

    @Transactional
    @PutMapping("/addCompetition/{tournamentUUID}")
    public ResponseEntity<?> addCompetitionListToTournament(@PathVariable String tournamentUUID, @RequestParam List<String> competitionsUUID) {

        List<String> list = new ArrayList<>();

        competitionsUUID.forEach(e -> list.add(tournamentService.addNewCompetitionListToTournament(tournamentUUID, e)));

        if (!list.isEmpty()) {
            return ResponseEntity.ok(list);
        } else {
            return ResponseEntity.badRequest().body("pusta lista");
        }

    }

    @Transactional
    @DeleteMapping("/delete/{tournamentUUID}")
    public ResponseEntity<?> deleteTournament(@PathVariable String tournamentUUID, @RequestParam String pinCode) {
        return tournamentService.deleteTournament(tournamentUUID, pinCode);
    }
}
