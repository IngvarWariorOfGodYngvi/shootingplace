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
import java.util.UUID;

@Service
public class WeaponPermissionService {

    private final MemberRepository memberRepository;
    private final WeaponPermissionRepository weaponPermissionRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public WeaponPermissionService(MemberRepository memberRepository, WeaponPermissionRepository weaponPermissionRepository) {
        this.memberRepository = memberRepository;
        this.weaponPermissionRepository = weaponPermissionRepository;
    }

    void addWeaponPermission(UUID memberUUID, WeaponPermission weaponPermission) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        if (memberEntity.getWeaponPermission() != null) {
            LOG.error("nie można już dodać patentu");
        }
        WeaponPermissionEntity weaponPermissionEntity = Mapping.map(weaponPermission);
        weaponPermissionRepository.saveAndFlush(weaponPermissionEntity);
        memberEntity.setWeaponPermission(weaponPermissionEntity);
        memberRepository.saveAndFlush(memberEntity);
        LOG.info("Pozwolenie na broń zostało dodane");
    }

    boolean updateWeaponPermission(UUID memberUUID, WeaponPermission weaponPermission) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        WeaponPermissionEntity weaponPermissionEntity = weaponPermissionRepository.findById(memberEntity
                .getWeaponPermission()
                .getUuid())
                .orElseThrow(EntityNotFoundException::new);
        if (memberEntity.getActive().equals(false)) {
            LOG.error("Klubowicz nie aktywny");
            return false;
        }

        if (weaponPermission.getNumber() != null) {
            if (weaponPermissionRepository.findByNumber(weaponPermission.getNumber()).isPresent()
                    && !memberEntity.getWeaponPermission().getNumber().equals(weaponPermission.getNumber())) {
                LOG.error("ktoś już ma taki numer pozwolenia");
            } else {
                weaponPermissionEntity.setNumber(weaponPermission.getNumber());
                weaponPermissionEntity.setIsExist(!weaponPermissionEntity.getIsExist());
                LOG.info("Wprowadzono numer pozwolenia");
            }
        }
        if (weaponPermission.getNumber() == null && !weaponPermission.getIsExist()) {
            weaponPermissionEntity.setIsExist(false);
            LOG.info("Usunięto pozwolenie");
        }
        weaponPermissionRepository.saveAndFlush(weaponPermissionEntity);
        memberEntity.setWeaponPermission(weaponPermissionEntity);
        memberRepository.saveAndFlush(memberEntity);
        LOG.info("Zaktualizowano pozwolenie na broń");
        return true;
    }


}