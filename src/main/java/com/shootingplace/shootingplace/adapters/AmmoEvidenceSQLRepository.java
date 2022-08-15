package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.AmmoEvidence.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.AmmoEvidence.AmmoEvidenceRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmmoEvidenceSQLRepository extends AmmoEvidenceRepository, JpaRepository<AmmoEvidenceEntity,String> {
}
