package com.shootingplace.shootingplace.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeaponPermission {


    private String number;

    private Boolean isExist = false;

    private String admissionToPossessAWeapon;
    private Boolean admissionToPossessAWeaponIsExist = false;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Boolean getExist() {
        return isExist;
    }

    public void setExist(Boolean exist) {
        isExist = exist;
    }

    public String getAdmissionToPossessAWeapon() {
        return admissionToPossessAWeapon;
    }

    public void setAdmissionToPossessAWeapon(String admissionToPossessAWeapon) {
        this.admissionToPossessAWeapon = admissionToPossessAWeapon;
    }

    public Boolean getAdmissionToPossessAWeaponIsExist() {
        return admissionToPossessAWeaponIsExist;
    }

    public void setAdmissionToPossessAWeaponIsExist(Boolean admissionToPossessAWeaponIsExist) {
        this.admissionToPossessAWeaponIsExist = admissionToPossessAWeaponIsExist;
    }
}
