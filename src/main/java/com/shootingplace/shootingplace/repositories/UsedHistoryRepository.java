package com.shootingplace.shootingplace.repositories;

import com.shootingplace.shootingplace.domain.entities.UsedHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsedHistoryRepository extends JpaRepository<UsedHistoryEntity,String> {
}
