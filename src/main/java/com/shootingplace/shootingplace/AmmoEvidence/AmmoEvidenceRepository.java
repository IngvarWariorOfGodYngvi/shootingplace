package com.shootingplace.shootingplace.AmmoEvidence;

import java.util.List;
import java.util.Optional;

public interface AmmoEvidenceRepository{
    List<AmmoEvidenceEntity> findAll();

    boolean existsById(String uuid);

    AmmoEvidenceEntity getOne(String uuid);

    Optional<AmmoEvidenceEntity> findById(String uuid);

    AmmoEvidenceEntity save(AmmoEvidenceEntity entity);

    void delete(AmmoEvidenceEntity entity);
}
