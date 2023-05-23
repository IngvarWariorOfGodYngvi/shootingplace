package com.shootingplace.shootingplace.armory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GunStoreDTO {
    private String typeName;

    private List<Gun> gunList;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public List<Gun> getGunList() {
        return gunList;
    }

    public void setGunEntityList(List<Gun> gunList) {
        this.gunList = gunList;
    }
}
