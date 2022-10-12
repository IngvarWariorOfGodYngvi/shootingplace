package com.shootingplace.shootingplace.ammoEvidence;

public interface AmmoInEvidenceRepository{
    AmmoInEvidenceEntity save(AmmoInEvidenceEntity entity);

    void delete(AmmoInEvidenceEntity entity);
}
