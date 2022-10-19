package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.tournament.CompetitionMembersListRepository;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionMembersListSQLRepository extends CompetitionMembersListRepository,JpaRepository<CompetitionMembersListEntity, String> {
}
