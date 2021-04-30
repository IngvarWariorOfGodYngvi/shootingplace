package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.entities.MemberPermissionsEntity;
import com.shootingplace.shootingplace.domain.enums.ArbiterClass;
import com.shootingplace.shootingplace.domain.models.MemberPermissions;
import com.shootingplace.shootingplace.repositories.MemberPermissionsRepository;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberPermissionsService {

    private final MemberPermissionsRepository memberPermissionsRepository;
    private final MemberRepository memberRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public MemberPermissionsService(MemberPermissionsRepository memberPermissionsRepository, MemberRepository memberRepository) {
        this.memberPermissionsRepository = memberPermissionsRepository;
        this.memberRepository = memberRepository;
    }

    public Boolean updateMemberPermissions(String memberUUID, MemberPermissions memberPermissions, String ordinal) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        MemberPermissionsEntity memberPermissionsEntity = memberPermissionsRepository.findById(
                memberEntity.getMemberPermissions().getUuid()).orElseThrow(EntityNotFoundException::new);
        List<MemberEntity> collect = memberRepository.findAll()
                .stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getMemberPermissions().getInstructorNumber() != null)
                .filter(f -> f.getMemberPermissions().getShootingLeaderNumber() != null)
                .filter(f -> f.getMemberPermissions().getArbiterNumber() != null)
                .collect(Collectors.toList());
//        Instruktor
        if (memberPermissions.getInstructorNumber() != null) {
            if (!memberPermissions.getInstructorNumber().isEmpty()) {
                if (collect.stream().noneMatch(e -> e.getMemberPermissions().getInstructorNumber().equals(memberPermissions.getInstructorNumber()))) {
                    memberPermissionsEntity.setInstructorNumber(memberPermissions.getInstructorNumber());
                    LOG.info("Nadano uprawnienia Instruktora");
                } else {
                    LOG.info("Nie można nadać uprawnień");
                    return false;
                }
            }
        }
//        Prowadzący Strzelanie
        if (memberPermissions.getShootingLeaderNumber() != null) {
            if (!memberPermissions.getShootingLeaderNumber().isEmpty()) {
                if (collect.stream().noneMatch(e -> e.getMemberPermissions().getShootingLeaderNumber().equals(memberPermissions.getShootingLeaderNumber()))) {
                    memberPermissionsEntity.setShootingLeaderNumber(memberPermissions.getShootingLeaderNumber());
                    LOG.info("Nadano uprawnienia Prowadzącego Strzelanie");
                } else {
                    LOG.info("Nie można nadać uprawnień");
                    return false;
                }

            }
        }
//        Sędzia
        if (memberPermissions.getArbiterNumber() != null) {
            if (!memberPermissions.getArbiterNumber().isEmpty()) {
                if (collect.stream().noneMatch(e -> e.getMemberPermissions().getArbiterNumber().equals(memberPermissions.getArbiterNumber()))) {
                    memberPermissionsEntity.setArbiterNumber(memberPermissions.getArbiterNumber());
                    LOG.info("Zmieniono numer sędziego");
                } else {
                    LOG.info("Nie można nadać uprawnień");
                    return false;
                }
            }
            if (ordinal != null && !ordinal.isEmpty()) {
                if (ordinal.equals("1")) {
                    memberPermissionsEntity.setArbiterClass(ArbiterClass.CLASS_3.getName());
                }
                if (ordinal.equals("2")) {
                    memberPermissionsEntity.setArbiterClass(ArbiterClass.CLASS_2.getName());
                }
                if (ordinal.equals("3")) {
                    memberPermissionsEntity.setArbiterClass(ArbiterClass.CLASS_1.getName());
                }
                if (ordinal.equals("4")) {
                    memberPermissionsEntity.setArbiterClass(ArbiterClass.CLASS_STATE.getName());
                }
                if (ordinal.equals("5")) {
                    memberPermissionsEntity.setArbiterClass(ArbiterClass.CLASS_INTERNATIONAL.getName());
                }
                LOG.info("Klasa sędziego ustawiona na pole nr " + ordinal);
            }
            if (memberPermissions.getArbiterPermissionValidThru() != null) {
                LocalDate date = LocalDate.of(memberPermissions.getArbiterPermissionValidThru().getYear(), 12, 31);
                memberPermissionsEntity.setArbiterPermissionValidThru(date);
                LOG.info("Zmieniono datę ważności licencji sędziowskiej");
            }
        }
        memberPermissionsRepository.saveAndFlush(memberPermissionsEntity);

        return true;
    }


    public boolean updateMemberArbiterClass(String memberUUID) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        MemberPermissionsEntity memberPermissions = memberEntity.getMemberPermissions();
        String arbiterClass = memberPermissions.getArbiterClass();

        if(memberPermissions.getArbiterNumber()==null || memberPermissions.getArbiterNumber().isEmpty()){
            System.out.println("nie można zaktualizować");
            return false;
        }

        String finalArbiterClass = arbiterClass;
        ArbiterClass arbiterClass1 = Arrays.stream(ArbiterClass.values()).filter(f -> f.getName().equals(finalArbiterClass)).findFirst().orElseThrow(EntityNotFoundException::new);
        if (arbiterClass1.equals(ArbiterClass.CLASS_3)) {
            arbiterClass = ArbiterClass.CLASS_2.getName();
        }
        if (arbiterClass1.equals(ArbiterClass.CLASS_2)) {
            arbiterClass = ArbiterClass.CLASS_1.getName();
        }
        if (arbiterClass1.equals(ArbiterClass.CLASS_1)) {
            arbiterClass = ArbiterClass.CLASS_STATE.getName();
        }
        if (arbiterClass1.equals(ArbiterClass.CLASS_STATE)) {
            arbiterClass = ArbiterClass.CLASS_INTERNATIONAL.getName();
        }
        memberPermissions.setArbiterClass(arbiterClass);
        memberPermissionsRepository.saveAndFlush(memberPermissions);
        System.out.println("zaktualizowano");
        return true;


    }
}
