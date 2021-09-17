package com.shootingplace.shootingplace.domain.entities;

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
public class UsedHistoryEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String gunUUID;
    private String gunSerialNumber;
    private LocalDate date;

    private String usedType;
    private String evidenceUUID;


    public String getUuid() {
        return uuid;
    }

    public String getGunUUID() {
        return gunUUID;
    }

    public void setGunUUID(String gunUUID) {
        this.gunUUID = gunUUID;
    }

    public String getGunSerialNumber() {
        return gunSerialNumber;
    }

    public void setGunSerialNumber(String gunSerialNumber) {
        this.gunSerialNumber = gunSerialNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getUsedType() {
        return usedType;
    }

    public void setUsedType(String usedType) {
        this.usedType = usedType;
    }

    public String getEvidenceUUID() {
        return evidenceUUID;
    }

    public void setEvidenceUUID(String evidenceUUID) {
        this.evidenceUUID = evidenceUUID;
    }
}
