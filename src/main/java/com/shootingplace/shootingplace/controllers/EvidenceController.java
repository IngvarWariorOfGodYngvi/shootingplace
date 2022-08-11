//package com.shootingplace.shootingplace.controllers;
//
//import com.shootingplace.shootingplace.domain.entities.ElectronicEvidenceEntity;
//import com.shootingplace.shootingplace.member.MemberEntity;
//import com.shootingplace.shootingplace.services.ElectronicEvidenceService;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/evidence")
//@CrossOrigin
//public class EvidenceController {
//
//    private final ElectronicEvidenceService evidenceService;
//
//    public EvidenceController(ElectronicEvidenceService evidenceService) {
//        this.evidenceService = evidenceService;
//    }
//
//    @GetMapping("/members")
//    public List<MemberEntity> getMembersInEvidence() {
//        return evidenceService.getMembersInEvidence();
//    }
//
//    @GetMapping("/evidence")
//    public ElectronicEvidenceEntity getEvidence() {
//        return evidenceService.getEvidence();
//    }
//
//    @PatchMapping("/{uuid}")
//    public Boolean addMemberToEvidence(@PathVariable String uuid) {
//        return evidenceService.addMemberToEvidence(uuid);
//    }
//
//    @PatchMapping("/clear")
//    public Boolean clearEvidence() {
//        return evidenceService.clearEvidence();
//    }
//
//    @PutMapping("/setDate")
//    public Boolean setEvidenceDate(@RequestParam String date) {
//        return evidenceService.setEvidenceDate(date);
//    }
//}
