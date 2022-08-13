package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.domain.entities.LicenseEntity;
import com.shootingplace.shootingplace.repositories.LicenseRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseSQLRepository extends LicenseRepository, JpaRepository<LicenseEntity,String> {
}
