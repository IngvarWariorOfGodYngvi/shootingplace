package com.shootingplace.shootingplace.controllers;


import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.models.Member;
import com.shootingplace.shootingplace.domain.models.MemberDTO;
import com.shootingplace.shootingplace.domain.models.WeaponPermission;
import com.shootingplace.shootingplace.services.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/member")
@CrossOrigin
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/{number}")
    public ResponseEntity<MemberEntity> getMember(@PathVariable int number) {
        return ResponseEntity.ok(memberService.getMember(number));
    }

    @GetMapping("/activelist")
    public ResponseEntity<List<MemberEntity>> getActiveMembersList(@RequestParam Boolean active, @RequestParam Boolean adult, @RequestParam Boolean erase) {
        return ResponseEntity.ok(memberService.getMembersList(active, adult, erase));
    }

    @GetMapping("/erased")
    public ResponseEntity<List<MemberEntity>> getErasedMembers() {
        return ResponseEntity.ok(memberService.getErasedMembers());
    }

    @GetMapping("/license")
    public ResponseEntity<List<String>> getMemberWithLicense(@RequestParam Boolean license) {
        return ResponseEntity.ok(memberService.getMembersWithLicense(license));
    }

    @GetMapping("/getMembersNames")
    public List<String> getMembersNames(@RequestParam Boolean active, @RequestParam Boolean adult, @RequestParam Boolean erase) {
        return memberService.getMembersNameAndLegitimationNumber(active, adult, erase);
    }

    @GetMapping("/getAllNames")
    public List<String> getAllNames() {
        return memberService.getAllNames();
    }

    @GetMapping("/getAllMemberDTO")
    public ResponseEntity<List<MemberDTO>> getAllMemberDTO() {
        return ResponseEntity.ok(memberService.getAllMemberDTO());

    }
    @GetMapping("/getAllMemberDTOWithArgs")
    public ResponseEntity<List<MemberDTO>> getAllMemberDTO(@RequestParam boolean adult,@RequestParam boolean active,@RequestParam boolean erase) {
        return ResponseEntity.ok(memberService.getAllMemberDTO(adult,active,erase));

    }

    @GetMapping("/membersQuantity")
    public List<Long> getMembersQuantity() {
        return memberService.getMembersQuantity();
    }

    @GetMapping("/getAllActiveMembersNames")
    public List<String> getAllActiveMembersNames() {
        return memberService.getAllActiveMembersNames();
    }

    @GetMapping("/getArbiters")
    public List<String> getArbiters() {
        return memberService.getArbiters();
    }

    @GetMapping("/membersWithPermissions")
    public List<Member> getMembersWithPermissions() {
        return memberService.getMembersWithPermissions();
    }


    @GetMapping("/membersEmails")
    public String getMembersEmails(@RequestParam Boolean condition) {
        return memberService.getAdultMembersEmails(condition);
    }

    @PostMapping("/")
    public ResponseEntity<?> addMember(@RequestBody @Valid Member member) {
        ResponseEntity<?> result;
        try {
            result = memberService.addNewMember(member);
        } catch (IllegalArgumentException e) {
            result = ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return result;
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateMember(@PathVariable String uuid, @RequestBody @Valid Member member) {
        return memberService.updateMember(uuid, member);
    }

    @PutMapping("/date/{uuid}")
    public ResponseEntity<?> updateJoinDate(@PathVariable String uuid, @RequestParam String date) {
        return memberService.updateJoinDate(uuid, date);
    }

    @PutMapping("/weapon/{memberUUID}")
    public ResponseEntity<?> changeWeaponPermission(@PathVariable String memberUUID, @RequestBody WeaponPermission weaponPermission) {
        if (memberService.changeWeaponPermission(memberUUID, weaponPermission)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/adult/{uuid}")
    public ResponseEntity<?> changeAdult(@PathVariable String uuid) {
        return memberService.changeAdult(uuid);
    }

    @PatchMapping("/pzss/{uuid}")
    public ResponseEntity<?> changePzss(@PathVariable String uuid) {
        if (memberService.changePzss(uuid)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{uuid}")
    public ResponseEntity<?> activateOrDeactivateMember(@PathVariable String uuid) {
        return memberService.activateOrDeactivateMember(uuid);
    }

    @PatchMapping("/erase/{uuid}")
    public ResponseEntity<?> eraseMember(@PathVariable String uuid, @RequestParam String reason) {
        return memberService.eraseMember(uuid, reason);
    }
}
