package com.shootingplace.shootingplace.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmmoInEvidenceEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String caliberName;

    private String caliberUUID;

    private String evidenceUUID;

    private Integer quantity;
    @ManyToMany
    private List<AmmoUsedToEvidenceEntity> ammoUsedToEvidenceEntityList;

    public String getUuid() {
        return uuid;
    }

    public String getCaliberName() {
        return caliberName;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }

    public String getCaliberUUID() {
        return caliberUUID;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public String getEvidenceUUID() {
        return evidenceUUID;
    }

    public void setEvidenceUUID(String evidenceUUID) {
        this.evidenceUUID = evidenceUUID;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<AmmoUsedToEvidenceEntity> getAmmoUsedToEvidenceEntityList() {
        return ammoUsedToEvidenceEntityList;
    }

    public void setAmmoUsedToEvidenceEntityList(List<AmmoUsedToEvidenceEntity> ammoUsedToEvidenceEntityList) {
        this.ammoUsedToEvidenceEntityList = ammoUsedToEvidenceEntityList;
    }
}
