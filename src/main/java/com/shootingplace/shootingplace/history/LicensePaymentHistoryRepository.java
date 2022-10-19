package com.shootingplace.shootingplace.history;

import java.util.List;
import java.util.Optional;

public interface LicensePaymentHistoryRepository {
    LicensePaymentHistoryEntity save(LicensePaymentHistoryEntity entity);

    boolean existsById(String uuid);

    Optional<LicensePaymentHistoryEntity> findById(String uuid);

    void delete(LicensePaymentHistoryEntity entity);

    List<LicensePaymentHistoryEntity> findAll();
}
