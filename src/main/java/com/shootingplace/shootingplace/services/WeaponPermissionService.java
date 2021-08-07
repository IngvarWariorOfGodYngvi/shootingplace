package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.entities.WeaponPermissionEntity;
import com.shootingplace.shootingplace.domain.models.WeaponPermission;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import com.shootingplace.shootingplace.repositories.WeaponPermissionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WeaponPermissionService {

    private final MemberRepository memberRepository;
    private final WeaponPermissionRepository weaponPermissionRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public WeaponPermissionService(MemberRepository memberRepository, WeaponPermissionRepository weaponPermissionRepository) {
        this.memberRepository = memberRepository;
        this.weaponPermissionRepository = weaponPermissionRepository;
    }

    public boolean updateWeaponPermission(String memberUUID, WeaponPermission weaponPermission) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        WeaponPermissionEntity weaponPermissionEntity = memberEntity.getWeaponPermission();
        if (weaponPermission.getNumber() != null) {

            List<MemberEntity> collect = memberRepository.findAll()
                    .stream()
                    .filter(f -> !f.getErased())
                    .filter(f -> f.getWeaponPermission().getNumber() != null)
                    .filter(f -> f.getWeaponPermission().getNumber().equals(weaponPermission.getNumber()))
                    .collect(Collectors.toList());
            if (collect.size() > 0) {
                LOG.error("ktoś już ma taki numer pozwolenia");
                return false;
            } else {
                weaponPermissionEntity.setNumber(weaponPermission.getNumber());
                weaponPermissionEntity.setExist(true);
                LOG.info("Wprowadzono numer pozwolenia");
            }
        }
        if (weaponPermission.getAdmissionToPossessAWeapon() != null) {

            List<MemberEntity> collect = memberRepository.findAll()
                    .stream()
                    .filter(f -> !f.getErased())
                    .filter(f -> f.getWeaponPermission().getAdmissionToPossessAWeapon() != null)
                    .filter(f -> f.getWeaponPermission().getAdmissionToPossessAWeapon().equals(weaponPermission.getAdmissionToPossessAWeapon()))
                    .collect(Collectors.toList());

            if (collect.size() > 0) {
                LOG.error("ktoś już ma taki numer dopuszczenia");
                return false;
            } else {
                weaponPermissionEntity.setAdmissionToPossessAWeapon(weaponPermission.getAdmissionToPossessAWeapon());
                weaponPermissionEntity.setAdmissionToPossessAWeaponIsExist(true);
                LOG.info("Wprowadzono numer dopuszczenia");
            }
        }
        weaponPermissionRepository.saveAndFlush(weaponPermissionEntity);
        memberEntity.setWeaponPermission(weaponPermissionEntity);
        memberRepository.saveAndFlush(memberEntity);
        LOG.info("Zaktualizowano pozwolenie na broń");
        return true;
    }

    public boolean removeWeaponPermission(String memberUUID, boolean admission, boolean permission) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        WeaponPermissionEntity weaponPermission = memberEntity.getWeaponPermission();

        if (permission) {
            weaponPermission.setNumber(null);
            weaponPermission.setExist(false);
        }
        if (admission) {
            weaponPermission.setAdmissionToPossessAWeapon(null);
            weaponPermission.setAdmissionToPossessAWeaponIsExist(false);
        }
        weaponPermissionRepository.saveAndFlush(weaponPermission);
        return true;
    }

    public WeaponPermission getWeaponPermission() {
        return WeaponPermission.builder()
                .number(null)
                .isExist(false)
                .build();
    }

}
