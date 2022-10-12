package com.shootingplace.shootingplace.ammoEvidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoUsedEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String caliberName;

    private String caliberUUID;

    private String memberUUID;

    private Integer otherPersonEntityID;

    private String userName;

    private Integer counter;

    private LocalDate date;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getUuid() {
        return uuid;
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

    public String getCaliberUUID() {
        return caliberUUID;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public Integer getOtherPersonEntityID() {
        return otherPersonEntityID;
    }

    public void setOtherPersonEntityID(Integer otherPersonEntityID) {
        this.otherPersonEntityID = otherPersonEntityID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
