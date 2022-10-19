package com.shootingplace.shootingplace.history;

public interface JudgingHistoryRepository{
    JudgingHistoryEntity save(JudgingHistoryEntity entity);

    void delete(JudgingHistoryEntity entity);
}
