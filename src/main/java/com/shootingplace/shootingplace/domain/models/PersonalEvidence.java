package com.shootingplace.shootingplace.domain.models;

import com.shootingplace.shootingplace.domain.entities.AmmoUsedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonalEvidence {

    private List<AmmoUsedEntity> ammoList;
    private FilesModel file;

    public List<AmmoUsedEntity> getAmmoList() {
        return ammoList;
    }

    public void setAmmoList(List<AmmoUsedEntity> ammoList) {
        this.ammoList = ammoList;
    }

    public FilesModel getFile() {
        return file;
    }

    public void setFile(FilesModel file) {
        this.file = file;
    }
}
