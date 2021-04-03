package com.shootingplace.shootingplace.domain.entities;


import com.shootingplace.shootingplace.domain.enums.GunType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class GunTypeEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @Enumerated (value = EnumType.STRING)
    private GunType gunType;

    public String getUuid() {
        return uuid;
    }

    public void setGunType(GunType gunType) {
        this.gunType = gunType;
    }

    public GunType getGunType() {
        return gunType;
    }
}
