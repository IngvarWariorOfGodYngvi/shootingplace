package com.shootingplace.shootingplace.controllers;


import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.models.Member;
import com.shootingplace.shootingplace.domain.models.MemberDTO;
import com.shootingplace.shootingplace.services.ChangeHistoryService;
import com.shootingplace.shootingplace.services.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/member")
@CrossOrigin
public class MemberController {

    private final MemberService memberService;
    private final ChangeHistoryService changeHistoryService;


    public MemberController(MemberService memberService, ChangeHistoryService changeHistoryService) {
        this.memberService = memberService;
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping("/{number}")
    public ResponseEntity<MemberEntity> getMember(@PathVariable int number) {
        return ResponseEntity.ok(memberService.getMember(number));
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<MemberEntity> getMemberByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(memberService.getMemberByUUID(uuid));
    }
//
//    @GetMapping("/activeList")
//    public ResponseEntity<List<MemberEntity>> getActiveMembersList(@RequestParam Boolean active, @RequestParam Boolean adult, @RequestParam Boolean erase) {
//        return ResponseEntity.ok(memberService.getMembersList(active, adult, erase));
//    }

    @GetMapping("/erased")
    public ResponseEntity<List<MemberEntity>> getErasedMembers() {
        return ResponseEntity.ok(memberService.getErasedMembers());
    }

//    @GetMapping("/license")
//    public ResponseEntity<List<String>> getMemberWithLicense(@RequestParam Boolean license) {
//        return ResponseEntity.ok(memberService.getMembersWithLicense(license));
//    }

//    @GetMapping("/getMembersNames")
//    public List<String> getMembersNames(@RequestParam Boolean active, @RequestParam Boolean adult, @RequestParam Boolean erase) {
//        return memberService.getMembersNameAndLegitimationNumber(active, adult, erase);
//    }

    @GetMapping("/getAllNames")
    public List<String> getAllNames() {
        return memberService.getAllNames();
    }

    @GetMapping("/getAllMemberDTO")
    public ResponseEntity<List<MemberDTO>> getAllMemberDTO() {
        return ResponseEntity.ok(memberService.getAllMemberDTO());

    }

    @GetMapping("/getAllMemberDTOWithArgs")
    public ResponseEntity<List<MemberDTO>> getAllMemberDTO(@RequestParam @Nullable String adult, @Nullable @RequestParam String active, @RequestParam String erase) {
        Boolean adult1;
        Boolean active1;
        Boolean erase1;
        if (adult == null || adult.equals("null")) {
            adult1 = null;
        } else {
            adult1 = Boolean.valueOf(adult);
        }
        if (active == null || active.equals("null")) {
            active1 = null;
        } else {
            active1 = Boolean.valueOf(active);
        }
        if (erase == null || erase.equals("null")) {
            erase1 = null;
        } else {
            erase1 = Boolean.valueOf(erase);
        }
        memberService.checkMembers();
        return ResponseEntity.ok(memberService.getAllMemberDTO(adult1, active1, erase1));

    }

    @GetMapping("/membersQuantity")
    public List<Long> getMembersQuantity() {
        return memberService.getMembersQuantity();
    }

//    @GetMapping("/getAllActiveMembersNames")
//    public List<String> getAllActiveMembersNames() {
//        return memberService.getAllActiveMembersNames();
//    }

    @GetMapping("/getArbiters")
    public List<String> getArbiters() {
        return memberService.getArbiters();
    }

    @GetMapping("/membersWithPermissions")
    public List<Member> getMembersWithPermissions() {
        return memberService.getMembersWithPermissions();
    }

    @GetMapping("/membersEmails")
    public ResponseEntity<?> getMembersEmails(@RequestParam Boolean condition) {
        return ResponseEntity.ok(memberService.getMembersEmails(condition));
    }

    @GetMapping("/membersEmailsNoActive")
    public ResponseEntity<?> getMembersEmailsNoActive() {
        return ResponseEntity.ok(memberService.getMembersEmailsNoActive());
    }

    @GetMapping("/phoneNumbersNoActive")
    public ResponseEntity<?> getMembersPhoneNumbersNoActive() {
        return ResponseEntity.ok(memberService.getMembersPhoneNumbersNoActive());
    }

    @GetMapping("/membersEmailsNoPatent")
    public ResponseEntity<?> getMembersEmailsWithNoPatent() {
        return ResponseEntity.ok(memberService.getMembersEmailsAdultActiveWithNoPatent());
    }

    @GetMapping("/phoneNumbersNoPatent")
    public ResponseEntity<?> getMembersPhoneNumbersWithNoPatent() {
        return ResponseEntity.ok(memberService.getMembersPhoneNumbersWithNoPatent());
    }

    @GetMapping("/membersToEraseEmails")
    public ResponseEntity<?> getMembersToEraseEmails() {
        return ResponseEntity.ok(memberService.getMembersToEraseEmails());
    }

    @GetMapping("/membersToErasePhoneNumbers")
    public ResponseEntity<?> getMembersToErasePhoneNumbers() {
        return ResponseEntity.ok(memberService.getMembersToErasePhoneNumbers());
    }

    @GetMapping("/membersToPoliceEmails")
    public ResponseEntity<?> getMembersToPoliceEmails() {
        return ResponseEntity.ok(memberService.getMembersToPoliceEmails());
    }

    @GetMapping("/membersToPolicePhoneNumbers")
    public ResponseEntity<?> getMembersToPolicePhoneNumbers() {
        return ResponseEntity.ok(memberService.getMembersToPolicePhoneNumbers());
    }

    @GetMapping("/phoneNumbers")
    public ResponseEntity<?> getMembersPhoneNumbers(@RequestParam Boolean condition) {
        return ResponseEntity.ok(memberService.getMembersPhoneNumbers(condition));
    }

    @GetMapping("/pesel")
    public ResponseEntity<?> getMemberPeselIsPresent(@RequestParam String pesel) {
        return ResponseEntity.ok(memberService.getMemberPeselIsPresent(pesel));
    }

    @GetMapping("/IDCard")
    public ResponseEntity<?> getMemberIDCardPresent(@RequestParam String IDCard) {
        return ResponseEntity.ok(memberService.getMemberIDCardPresent(IDCard));
    }

    @GetMapping("/email")
    public ResponseEntity<?> getMemberEmailPresent(@RequestParam String email) {
        return ResponseEntity.ok(memberService.getMemberEmailPresent(email));
    }

    @GetMapping("/erasedType")
    public ResponseEntity<?> getErasedType() {
        return ResponseEntity.ok(memberService.getErasedType());
    }

    @PostMapping("/")
    public ResponseEntity<?> addMember(@RequestBody @Valid Member member, @RequestParam String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {
            ResponseEntity<?> result;
            if (member.getPesel().isEmpty() || member.getPhoneNumber().isEmpty() || member.getFirstName().isEmpty() || member.getSecondName().isEmpty() || member.getIDCard().isEmpty()) {
                result = ResponseEntity.status(406).body("\"Uwaga! Nie podano wszystkich lub żadnej informacji\"");
            } else {
                try {
                    result = memberService.addNewMember(member, pinCode);
                } catch (IllegalArgumentException e) {
                    result = ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
            }
            return result;
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateMember(@PathVariable String uuid, @RequestBody @Valid Member member) {
        return memberService.updateMember(uuid, member);
    }

//    @PutMapping("/date/{uuid}")
//    public ResponseEntity<?> updateJoinDate(@PathVariable String uuid, @RequestParam String date) {
//        return memberService.updateJoinDate(uuid, date);
//    }

    @PatchMapping("/adult/{uuid}")
    public ResponseEntity<?> changeAdult(@PathVariable String uuid, @RequestParam String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {

            return memberService.changeAdult(uuid, pinCode);
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
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
    public ResponseEntity<?> activateOrDeactivateMember(@PathVariable String uuid, @RequestParam String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {
            return memberService.activateOrDeactivateMember(uuid, pinCode);
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
    }

    @PatchMapping("/erase/{uuid}")
    public ResponseEntity<?> eraseMember(@PathVariable String uuid, @RequestParam String additionalDescription, @RequestParam String erasedDate, @RequestParam String erasedType, @RequestParam String pinCode) {
        if (changeHistoryService.comparePinCode(pinCode)) {
            if (additionalDescription.trim().isEmpty() || additionalDescription.trim().isBlank() || additionalDescription.trim().equals("null")) {
                additionalDescription = null;
            }
            if (erasedDate.trim().isEmpty() || erasedDate.trim().isBlank() || erasedDate.trim().equals("null")) {
                erasedDate = String.valueOf(LocalDate.now());
            }
            LocalDate parsedDate = LocalDate.parse(erasedDate);
            return memberService.eraseMember(uuid, erasedType, parsedDate, additionalDescription, pinCode);
        } else {
            return ResponseEntity.status(403).body("Brak dostępu");
        }
    }

}
