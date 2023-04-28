package com.shootingplace.shootingplace.tournament;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/competitionMembersList")
@CrossOrigin
public class CompetitionMembersListController {

    private final CompetitionMembersListService competitionMembersListService;

    public CompetitionMembersListController(CompetitionMembersListService competitionMembersListService) {
        this.competitionMembersListService = competitionMembersListService;
    }

    @GetMapping("/getID")
    public ResponseEntity<String> getIDByName(@RequestParam String name, @RequestParam String tournamentUUID) {
        return ResponseEntity.ok(competitionMembersListService.getCompetitionIDByName(name, tournamentUUID));
    }

    @GetMapping("/getByID")
    public ResponseEntity<?> getCompetitionListByID(@RequestParam String uuid) {
        return ResponseEntity.ok(competitionMembersListService.getCompetitionListByID(uuid));
    }

    @GetMapping("/getMemberStarts")
    public ResponseEntity<List<String>> getMemberStartsInTournament(@RequestParam String memberUUID, @RequestParam int otherID, @RequestParam String tournamentUUID) {
        return ResponseEntity.ok(competitionMembersListService.getMemberStartsInTournament(memberUUID, otherID, tournamentUUID));
    }

    @GetMapping("/getMetricNumber")
    public ResponseEntity<?> getMetricNumber(@RequestParam String legNumber, @RequestParam int otherID, @RequestParam String tournamentUUID) {
        return competitionMembersListService.getMetricNumber(legNumber, otherID, tournamentUUID);
    }

    @GetMapping("/getMemberStartsByLegitimation")
    public ResponseEntity<List<String>> getMemberStartsInTournament(@RequestParam int legNumber, @RequestParam int otherID, @RequestParam String tournamentUUID) {
        return ResponseEntity.ok(competitionMembersListService.getMemberStartsInTournament(legNumber, otherID, tournamentUUID));
    }

    @GetMapping("/getScoreIdByNumberAndCompetitionName")
    public ResponseEntity<?> getMemberStartsInTournament(@RequestParam int legNumber, @RequestParam int otherID, @RequestParam String tournamentUUID, @RequestParam String competitionName) {
        return ResponseEntity.ok(competitionMembersListService.getScoreID(legNumber, otherID, tournamentUUID, competitionName));
    }

    @Transactional
    @PutMapping("/addMember")
    public ResponseEntity<?> addScoreToCompetitionMembersList(@RequestParam String tournamentUUID, @RequestParam List<String> competitionNameList, @RequestParam int legitimationNumber, @RequestParam @Nullable int otherPerson) {
        List<String> list = new ArrayList<>();
        competitionNameList.forEach(e->list.add(competitionMembersListService.addScoreToCompetitionList(tournamentUUID, e.replaceAll("\\.",","), legitimationNumber, otherPerson)));

        if (!list.isEmpty()) {
            return ResponseEntity.ok(list);
        } else
            return ResponseEntity.badRequest().body("pusta lista");
    }

    @Transactional
    @PostMapping("/removeMember")
    public ResponseEntity<?> removeMemberFromList(@RequestParam String tournamentUUID, @RequestParam List<String> competitionNameList, @RequestParam int legitimationNumber, @RequestParam @Nullable int otherPerson) {

        List<String> list = new ArrayList<>();
        competitionNameList.forEach(e->list.add(competitionMembersListService.removeScoreFromList(tournamentUUID,e.replaceAll("\\.",","), legitimationNumber, otherPerson) + e ));

        if (!list.isEmpty()) {
            return ResponseEntity.ok(list);
        } else
            return ResponseEntity.badRequest().body("pusta lista");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> removeMembersListFromTournament(@RequestParam String competitionUUID, @RequestParam String tournamentUUID) {
        return competitionMembersListService.removeListFromTournament(tournamentUUID, competitionUUID);
    }


}
