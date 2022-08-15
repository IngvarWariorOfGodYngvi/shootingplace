package com.shootingplace.shootingplace.License;

public interface LicenseRepository {
//    Optional<LicenseEntity> findByNumber(String number);
//
//    List<LicenseEntity> findAllByNumberIsNotNull();

    LicenseEntity save(LicenseEntity entity);
}
