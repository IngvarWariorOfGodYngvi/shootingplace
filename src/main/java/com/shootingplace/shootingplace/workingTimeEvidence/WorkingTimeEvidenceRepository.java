package com.shootingplace.shootingplace.workingTimeEvidence;

import java.util.List;

public interface WorkingTimeEvidenceRepository{

    WorkingTimeEvidenceEntity save(WorkingTimeEvidenceEntity entity);

    List<WorkingTimeEvidenceEntity> findAll();
}
