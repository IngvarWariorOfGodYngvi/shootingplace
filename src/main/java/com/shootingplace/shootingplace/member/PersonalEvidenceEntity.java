package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonalEvidenceEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @OneToMany
    @OrderBy("caliberName ASC")
    private List<AmmoUsedEntity> ammoList;

    public String getUuid() {
        return uuid;
    }

    public List<AmmoUsedEntity> getAmmoList() {
        return ammoList;
    }

    public void setAmmoList(List<AmmoUsedEntity> ammoList) {
        this.ammoList = ammoList;
    }
}
