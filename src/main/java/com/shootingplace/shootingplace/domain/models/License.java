package com.shootingplace.shootingplace.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class License {


    private String number;

    private LocalDate validThru;

    private Boolean pistolPermission;
    private Boolean riflePermission;
    private Boolean shotgunPermission;

    private Boolean isValid;

    private Boolean canProlong = false;


    private String club;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDate getValidThru() {
        return validThru;
    }

    public void setValidThru(LocalDate validThru) {
        this.validThru = validThru;
    }

    public Boolean getPistolPermission() {
        return pistolPermission;
    }

    public void setPistolPermission(Boolean pistolPermission) {
        this.pistolPermission = pistolPermission;
    }

    public Boolean getRiflePermission() {
        return riflePermission;
    }

    public void setRiflePermission(Boolean riflePermission) {
        this.riflePermission = riflePermission;
    }

    public Boolean getShotgunPermission() {
        return shotgunPermission;
    }

    public void setShotgunPermission(Boolean shotgunPermission) {
        this.shotgunPermission = shotgunPermission;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    public Boolean getCanProlong() {
        return canProlong;
    }

    public void setCanProlong(Boolean canProlong) {
        this.canProlong = canProlong;
    }

    public String getClub() {
        return club;
    }

    public void setClub(String club) {
        this.club = club;
    }
}
