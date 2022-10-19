package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.tournament.ScoreEntity;
import com.shootingplace.shootingplace.tournament.ScoreRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreSQLRepository extends ScoreRepository, JpaRepository<ScoreEntity, String> {
}
