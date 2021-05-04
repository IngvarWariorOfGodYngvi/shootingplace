package com.shootingplace.shootingplace.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GunStoreEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    @NotNull
    private String typeName;


    private List<GunEntity> gunEntityList;

    public String getUuid() {
        return uuid;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public List<GunEntity> getGunEntityList() {
        return gunEntityList;
    }

    public void setGunEntityList(List<GunEntity> gunEntityList) {
        this.gunEntityList = gunEntityList;
    }
}
