package com.shootingplace.shootingplace.tournament;

import java.util.List;
import java.util.Optional;

public interface CompetitionMembersListRepository{
    List<CompetitionMembersListEntity> findAll();

    CompetitionMembersListEntity save(CompetitionMembersListEntity entity);

    Optional<CompetitionMembersListEntity> findById(String uuid);

    void delete(CompetitionMembersListEntity entity);
}
