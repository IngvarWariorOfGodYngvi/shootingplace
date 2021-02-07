package com.shootingplace.shootingplace.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private String uuid;
    private LocalDate joinDate;
    private Integer legitimationNumber;
    private String firstName;
    private String secondName;
    private License license;
    private Boolean pzss;


    private Boolean adult = true;
    private Boolean active = true;
    private Boolean erased = true;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public Integer getLegitimationNumber() {
        return legitimationNumber;
    }

    public void setLegitimationNumber(Integer legitimationNumber) {
        this.legitimationNumber = legitimationNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public Boolean getAdult() {
        return adult;
    }

    public void setAdult(Boolean adult) {
        this.adult = adult;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getErased() {
        return erased;
    }

    public void setErased(Boolean erased) {
        this.erased = erased;
    }

    public Boolean getPzss() {
        return pzss;
    }

    public void setPzss(Boolean pzss) {
        this.pzss = pzss;
    }
}
