package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.armory.AmmoUsedService;
import com.shootingplace.shootingplace.armory.CaliberRepository;
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
    private final CaliberRepository caliberRepository;
    private final AmmoUsedService ammoUsedService;
    private final ScoreService scoreService;
    private final CompetitionMembersListRepository competitionMembersListRepository;


    public CompetitionMembersListController(CompetitionMembersListService competitionMembersListService, AmmoUsedService ammoUsedService, CaliberRepository caliberRepository, ScoreService scoreService, CompetitionMembersListRepository competitionMembersListRepository) {
        this.competitionMembersListService = competitionMembersListService;
        this.caliberRepository = caliberRepository;
        this.ammoUsedService = ammoUsedService;
        this.scoreService = scoreService;
        this.competitionMembersListRepository = competitionMembersListRepository;
    }

    @PatchMapping("/setCompetitionUUIDToCompetitionMemberList")
    public ResponseEntity<?> setCompetitionUUIDToCompetitionMemberList() {
        return competitionMembersListService.setCompetitionUUIDToCompetitionMemberList();
    }

    @GetMapping("/memberScores")
    public ResponseEntity<?> getMemberScoresFromCompetitionMemberListUUID(@RequestParam String competitionMemberListUUID) {
        return competitionMembersListService.getMemberScoresFromComtetitionMemberListUUID(competitionMemberListUUID);
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
    public List<String> getMemberStartsInTournament(@RequestParam String memberUUID, @RequestParam int otherID, @RequestParam String tournamentUUID) {
        return competitionMembersListService.getMemberStartsInTournament(memberUUID, otherID, tournamentUUID);
    }

    @GetMapping("/getMetricNumber")
    public ResponseEntity<?> getMetricNumber(@RequestParam String legNumber, @RequestParam int otherID, @RequestParam String tournamentUUID) {
        return competitionMembersListService.getMetricNumber(legNumber, otherID, tournamentUUID);
    }

    @GetMapping("/getMemberStartsByLegitimation")
    public List<String> getMemberStartsInTournament(@RequestParam int legNumber, @RequestParam int otherID, @RequestParam String tournamentUUID) {
        return competitionMembersListService.getMemberStartsInTournament(legNumber, otherID, tournamentUUID);
    }

    @GetMapping("/getScoreIdByNumberAndCompetitionName")
    public ResponseEntity<?> getMemberStartsInTournament(@RequestParam int legNumber, @RequestParam int otherID, @RequestParam String tournamentUUID, @RequestParam String competitionName) {
        return ResponseEntity.ok(competitionMembersListService.getScoreID(legNumber, otherID, tournamentUUID, competitionName));
    }

    @Transactional
    @PutMapping("/addMember")
    public ResponseEntity<?> addScoreToCompetitionMembersList(@RequestParam List<String> competitionUUIDList, @RequestParam List<String> addAmmoList, @RequestParam int legitimationNumber, @RequestParam @Nullable int otherPerson) {
        List<List<String>> list = new ArrayList<>();
        competitionUUIDList.forEach(e -> list.add(competitionMembersListService.addScoreToCompetitionList(e.replaceAll("\\.", ","), legitimationNumber, otherPerson)));
        addAmmoList.forEach(e -> {
            CompetitionMembersListEntity one = competitionMembersListRepository.getOne(e);
            one.setPracticeShots(one.getPracticeShots() != null ? one.getPracticeShots() : 0);
            ammoUsedService.addAmmoUsedEntity(one.getCaliberUUID(), legitimationNumber, otherPerson, one.getNumberOfShots() + one.getPracticeShots());
            String uuid;
            if (legitimationNumber > 0) {
                uuid = one.getScoreList().stream().filter(f -> f.getMember() != null && f.getMember().getLegitimationNumber().equals(legitimationNumber)).findFirst().get().getUuid();
            } else {
                uuid = one.getScoreList().stream().filter(f -> f.getOtherPersonEntity() != null && f.getOtherPersonEntity().getId().equals(otherPerson)).findFirst().get().getUuid();
            }
            scoreService.toggleAmmunitionInScore(uuid);
        });
        if (!list.isEmpty()) {
            List<String> list1 = new ArrayList<>();
            list.forEach(e -> {
                if (e.get(0) == null) {
                    list1.add(e.get(1));
                } else {
                    list1.add(e.get(0));
                }
            });
            return ResponseEntity.ok(list1);
        } else
            return ResponseEntity.badRequest().body("pusta lista");
    }

    @Transactional
    @PostMapping("/removeMember")
    public ResponseEntity<?> removeMemberFromList(@RequestParam List<String> competitionNameList, @RequestParam int legitimationNumber, @RequestParam @Nullable int otherPerson) {

        List<String> list = new ArrayList<>();
        competitionNameList.forEach(e -> list.add(competitionMembersListService.removeScoreFromList(e.replaceAll("\\.", ","), legitimationNumber, otherPerson) + e));

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
