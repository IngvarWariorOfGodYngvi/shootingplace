package com.shootingplace.shootingplace.repositories;

import com.shootingplace.shootingplace.domain.entities.GunTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GunTypeRepository extends JpaRepository<GunTypeEntity, String> {
}
