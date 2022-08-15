package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.License.LicenseEntity;
import com.shootingplace.shootingplace.License.LicenseRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface LicenseSQLRepository extends LicenseRepository, JpaRepository<LicenseEntity,String> {
}
