package com.shootingplace.shootingplace.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LicensePaymentHistoryDTO {

    private String firstName;

    private String secondName;

    private Integer legitimationNumber;

    private Boolean adult;

    private Boolean active;

    private String memberUUID;

    private LocalDate date;

    private String licenseUUID;

    private Integer validForYear;

    private boolean isPayInPZSSPortal;

    private boolean isNew;

    public String getLicenseUUID() {
        return licenseUUID;
    }

    public void setLicenseUUID(String licenseUUID) {
        this.licenseUUID = licenseUUID;
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

    public Integer getLegitimationNumber() {
        return legitimationNumber;
    }

    public void setLegitimationNumber(Integer legitimationNumber) {
        this.legitimationNumber = legitimationNumber;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMemberUUID() {
        return memberUUID;
    }

    public void setMemberUUID(String memberUUID) {
        this.memberUUID = memberUUID;
    }

    public Integer getValidForYear() {
        return validForYear;
    }

    public void setValidForYear(Integer validForYear) {
        this.validForYear = validForYear;
    }

    public boolean isPayInPZSSPortal() {
        return isPayInPZSSPortal;
    }

    public void setPayInPZSSPortal(boolean payInPZSSPortal) {
        isPayInPZSSPortal = payInPZSSPortal;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
