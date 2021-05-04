package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.entities.OtherPersonEntity;
import com.shootingplace.shootingplace.domain.enums.ArbiterClass;
import com.shootingplace.shootingplace.domain.models.MemberPermissions;
import com.shootingplace.shootingplace.domain.models.OtherPerson;
import com.shootingplace.shootingplace.services.OtherPersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/other")
@CrossOrigin
public class OtherPersonController {
    private final OtherPersonService otherPersonService;

    public OtherPersonController(OtherPersonService otherPersonService) {
        this.otherPersonService = otherPersonService;
    }

    @PostMapping("")
    public ResponseEntity<?> addPerson(@RequestBody OtherPerson person, @RequestParam String club,
                                       @Nullable @RequestParam String arbiterClass,
                                       @Nullable @RequestParam String arbiterNumber,
                                       @Nullable @RequestParam String arbiterPermissionValidThru) {
        MemberPermissions memberPermissions = null;
        if (arbiterClass != null && !arbiterClass.isEmpty()) {
            if (arbiterClass.equals("1")) {
                arbiterClass = (ArbiterClass.CLASS_3.getName());
            }
            if (arbiterClass.equals("2")) {
                arbiterClass = (ArbiterClass.CLASS_2.getName());
            }
            if (arbiterClass.equals("3")) {
                arbiterClass = (ArbiterClass.CLASS_1.getName());
            }
            if (arbiterClass.equals("4")) {
                arbiterClass = (ArbiterClass.CLASS_STATE.getName());
            }
            if (arbiterClass.equals("5")) {
                arbiterClass = (ArbiterClass.CLASS_INTERNATIONAL.getName());
            }
            LocalDate parse = null;
            if (!Objects.equals(arbiterPermissionValidThru, "")) {
                if (arbiterPermissionValidThru != null) {
                    parse = LocalDate.parse(arbiterPermissionValidThru);
                }
            }
            memberPermissions = MemberPermissions.builder()
                    .arbiterNumber(arbiterNumber)
                    .arbiterClass(arbiterClass)
                    .arbiterPermissionValidThru(parse)
                    .shootingLeaderNumber(null)
                    .instructorNumber(null)
                    .build();
        }
        if (club.isEmpty()) {
            club = "BRAK";
        }

        if (otherPersonService.addPerson(club, person, memberPermissions)) {
            return ResponseEntity.status(201).build();
        } else {
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping("/")
    public ResponseEntity<List<String>> getAllOthers() {
        return ResponseEntity.ok().body(otherPersonService.getAllOthers());
    }

    @GetMapping("/arbiters")
    public ResponseEntity<List<String>> getAllOthersArbiters() {
        return ResponseEntity.ok().body(otherPersonService.getAllOthersArbiters());
    }

    @GetMapping("/all")
    public ResponseEntity<List<OtherPersonEntity>> getAll() {
        return ResponseEntity.ok().body(otherPersonService.getAll());
    }

    @PostMapping("/")
    public ResponseEntity<?> deactivatePerson(@RequestParam int id) {
        if (otherPersonService.deactivatePerson(id)) {
            return ResponseEntity.ok().build();
        } else
            return ResponseEntity.notFound().build();
    }

    @PutMapping("/")
    public ResponseEntity<?> updatePerson(@RequestParam String id,@RequestBody OtherPerson otherPerson) {
        if (otherPersonService.updatePerson(id,otherPerson)) {
            return ResponseEntity.ok().build();
        } else
            return ResponseEntity.notFound().build();
    }

}
