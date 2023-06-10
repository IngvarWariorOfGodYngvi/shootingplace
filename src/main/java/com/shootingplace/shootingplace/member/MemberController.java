package com.shootingplace.shootingplace.member;


import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.wrappers.MemberWithAddressWrapper;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
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
    public ResponseEntity<?> getMember(@PathVariable int number) {
        return memberService.getMember(number);
    }

    @GetMapping("/ID/{number}")
    public ResponseEntity<?> getMemberUUIDByLegitimationNumber(@PathVariable int number) {
        return memberService.getMemberUUIDByLegitimationNumber(number);
    }

    @GetMapping("/PESEL/{PESELNumber}")
    public ResponseEntity<?> getMemberByPESELNumber(@PathVariable String PESELNumber) {
        return memberService.getMemberByPESELNumber(PESELNumber);
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<MemberEntity> getMemberByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(memberService.getMemberByUUID(uuid));
    }

    @GetMapping("/erased")
    public ResponseEntity<?> getErasedMembers() {
        return ResponseEntity.ok(memberService.getMembersErased());
    }

    @GetMapping("/getAllNames")
    public List<MemberInfo> getAllNames() {
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
        return ResponseEntity.ok(memberService.getAllMemberDTO(adult1, active1, erase1));
    }

    @GetMapping("/membersQuantity")
    public List<Long> getMembersQuantity() {
        return memberService.getMembersQuantity();
    }

    @GetMapping("/getArbiters")
    public List<String> getArbiters() {
        return memberService.getArbiters();
    }

    @GetMapping("/membersWithPermissions")
    public List<Member> getMembersWithPermissions() {
        return memberService.getMembersWithPermissions();
    }

    @GetMapping("/getMemberEmail")
    public ResponseEntity<?> getSingleMemberEmail(@RequestParam Integer number) {
        return memberService.getSingleMemberEmail(number);
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

    @Transactional
    @PostMapping("/")
    public ResponseEntity<?> addMember(@RequestBody @Valid MemberWithAddressWrapper memberWithAddressWrapper, @RequestParam boolean returningToClub, @RequestParam String pinCode) {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            ResponseEntity<?> result;
            Member member = memberWithAddressWrapper.getMember();
            Address address = memberWithAddressWrapper.getAddress();
            if (member.getPesel().isEmpty() || member.getPhoneNumber().isEmpty() || member.getFirstName().isEmpty() || member.getSecondName().isEmpty() || member.getIDCard().isEmpty()) {
                result = ResponseEntity.status(406).body("Uwaga! Nie podano wszystkich lub żadnej informacji");
            } else {
                try {
                    result = memberService.addNewMember(member,address,returningToClub, pinCode);
                } catch (IllegalArgumentException e) {
                    result = ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
            }
            return result;
        } else {
            return code;
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateMember(@PathVariable String uuid, @RequestBody @Valid Member member, @RequestParam String pinCode) {
        return memberService.updateMember(uuid, member, pinCode);
    }

    @GetMapping("/getMembersToReportToThePolice")
    public ResponseEntity<?> getMembersToReportToThePolice() {
        return ResponseEntity.ok(memberService.getMembersToReportToThePolice());
    }

    @GetMapping("/getMembersToErase")
    public ResponseEntity<?> getMembersToErase() {
        return ResponseEntity.ok(memberService.getMembersToErase());
    }

    @GetMapping("/findByBarCode")
    public ResponseEntity<?> findMemberByBarCode(@RequestParam String barcode) {
        return memberService.findMemberByBarCode(barcode);
    }
@Transactional
    @PatchMapping("/adult/{uuid}")
    public ResponseEntity<?> changeAdult(@PathVariable String uuid, @RequestParam String pinCode) {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return memberService.changeAdult(uuid, pinCode);
        } else {
            return code;
        }
    }

    @PatchMapping("/pzss/{uuid}")
    public ResponseEntity<?> changePzss(@PathVariable String uuid) {
        return memberService.changePzss(uuid);
    }

    @PatchMapping("/{uuid}")
    public ResponseEntity<?> activateOrDeactivateMember(@PathVariable String uuid, @RequestParam String pinCode) {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return memberService.activateOrDeactivateMember(uuid, pinCode);
        } else {
            return code;
        }
    }

    @PatchMapping("/erase/{uuid}")
    public ResponseEntity<?> eraseMember(@PathVariable String uuid, @RequestParam String additionalDescription, @RequestParam String erasedDate, @RequestParam String erasedType, @RequestParam String pinCode) {
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            if (additionalDescription.trim().isEmpty() || additionalDescription.trim().isBlank() || additionalDescription.trim().equals("null")) {
                additionalDescription = null;
            }
            if (erasedDate.trim().isEmpty() || erasedDate.trim().isBlank() || erasedDate.trim().equals("null")) {
                erasedDate = String.valueOf(LocalDate.now());
            }
            LocalDate parsedDate = LocalDate.parse(erasedDate);
            return memberService.eraseMember(uuid, erasedType, parsedDate, additionalDescription, pinCode);
        } else {
            return code;
        }
    }
    @PatchMapping("/changeClub/{uuid}")
    public ResponseEntity<?> changeClub(@PathVariable String uuid,@RequestParam String clubName){
        return memberService.changeClub(uuid,clubName);
    }
    @Transactional
    @DeleteMapping("/delete/{uuid}")
    public ResponseEntity<?> deleteMember(@PathVariable String uuid) {
        return memberService.deleteMember(uuid);
    }

}
