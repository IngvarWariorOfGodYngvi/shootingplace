package com.shootingplace.shootingplace.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoUsedPersonal {
    private String caliberName;

    private String memberUUID;

    private String caliberUUID;

    private Integer otherPersonEntityID;

    private String memberName;

    private Integer counter;

    private LocalDate date;

    public LocalDate getDate() {
        return date;
    }

    public String getCaliberName() {
        return caliberName;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public String getMemberUUID() {
        return memberUUID;
    }

    public void setMemberUUID(String memberUUID) {
        this.memberUUID = memberUUID;
    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public String getCaliberUUID() {
        return caliberUUID;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
