package com.shootingplace.shootingplace.history;

import java.util.Optional;

public interface HistoryRepository{
    HistoryEntity save(HistoryEntity entity);

    Optional<HistoryEntity> findById(String uuid);
}
