package com.shootingplace.shootingplace.ammoEvidence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AmmoEvidenceRepository{
    List<AmmoEvidenceEntity> findAll();

    List<AmmoEvidenceEntity> findAllByOpenTrueAndForceOpenFalse();

    boolean existsByOpenTrueAndForceOpenFalse();

    boolean existsById(String uuid);

    AmmoEvidenceEntity getOne(String uuid);

    Optional<AmmoEvidenceEntity> findById(String uuid);

    AmmoEvidenceEntity save(AmmoEvidenceEntity entity);

    void delete(AmmoEvidenceEntity entity);

    Page<AmmoEvidenceEntity> findAll(Pageable page);
    Page<AmmoEvidenceEntity> findAllByOpenFalse(Pageable page);

    List<AmmoEvidenceEntity> findAllByOpenTrue();
}
