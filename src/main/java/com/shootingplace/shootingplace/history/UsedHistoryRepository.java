package com.shootingplace.shootingplace.history;

import java.util.List;

public interface UsedHistoryRepository{
    List<UsedHistoryEntity> findAll();

    UsedHistoryEntity save(UsedHistoryEntity entity);
}
