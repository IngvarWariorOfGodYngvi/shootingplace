package com.shootingplace.shootingplace.contributions;

import java.util.List;
import java.util.Optional;

public interface ContributionRepository{

    ContributionEntity save(ContributionEntity entity);

    void delete(ContributionEntity entity);

    Optional<ContributionEntity> findById(String contributionUUID);

    List<ContributionEntity> findAll();

    ContributionEntity getOne(String contributionUUID);
}
