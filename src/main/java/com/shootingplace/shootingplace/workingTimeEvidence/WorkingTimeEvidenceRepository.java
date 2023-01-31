package com.shootingplace.shootingplace.workingTimeEvidence;

import java.util.List;
import java.util.Optional;

public interface WorkingTimeEvidenceRepository{

    WorkingTimeEvidenceEntity save(WorkingTimeEvidenceEntity entity);

    List<WorkingTimeEvidenceEntity> findAll();

    Optional<WorkingTimeEvidenceEntity> findById(String e);
    boolean existsByIsCloseFalse();
}
