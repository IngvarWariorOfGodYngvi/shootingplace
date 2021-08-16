package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.entities.OtherPersonEntity;
import com.shootingplace.shootingplace.domain.models.MemberPermissions;
import com.shootingplace.shootingplace.services.MemberPermissionsService;
import com.shootingplace.shootingplace.services.OtherPersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@CrossOrigin
public class MemberPermissionsController {

    private final MemberPermissionsService memberPermissionsService;
    private final OtherPersonService otherPersonService;


    public MemberPermissionsController(MemberPermissionsService memberPermissionsService, OtherPersonService otherPersonService) {
        this.memberPermissionsService = memberPermissionsService;
        this.otherPersonService = otherPersonService;
    }

    @GetMapping("/othersWithPermissions")
    public List<OtherPersonEntity> getOthersWithPermissions(){
        return otherPersonService.getOthersWithPermissions();
    }

    @PutMapping("/{memberUUID}")
    public ResponseEntity<?> updateMemberPermissions(@PathVariable String memberUUID,
                                                     @RequestBody MemberPermissions memberPermissions, @RequestParam String ordinal) {
        return memberPermissionsService.updateMemberPermissions(memberUUID, memberPermissions, ordinal);
    }

    @PutMapping("arbiter/{memberUUID}")
    public ResponseEntity<?> updateMemberArbiterClass(@PathVariable String memberUUID) {
        return memberPermissionsService.updateMemberArbiterClass(memberUUID);
    }
}
