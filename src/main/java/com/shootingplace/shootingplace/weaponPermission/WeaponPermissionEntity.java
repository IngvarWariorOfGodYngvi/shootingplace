package com.shootingplace.shootingplace.weaponPermission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeaponPermissionEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String number;

    private Boolean isExist = false;

    private String admissionToPossessAWeapon;
    private Boolean admissionToPossessAWeaponIsExist = false;

    public String getUuid() {
        return uuid;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAdmissionToPossessAWeapon() {
        return admissionToPossessAWeapon;
    }

    public void setAdmissionToPossessAWeapon(String admissionToPossessAWeapon) {
        this.admissionToPossessAWeapon = admissionToPossessAWeapon;
    }

    public Boolean getExist() {
        return isExist;
    }

    public void setExist(Boolean exist) {
        isExist = exist;
    }

    public Boolean getAdmissionToPossessAWeaponIsExist() {
        return admissionToPossessAWeaponIsExist;
    }

    public void setAdmissionToPossessAWeaponIsExist(Boolean admissionToPossessAWeaponIsExist) {
        this.admissionToPossessAWeaponIsExist = admissionToPossessAWeaponIsExist;
    }
}
