package com.shootingplace.shootingplace.repositories;

import com.shootingplace.shootingplace.domain.entities.GunStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GunStoreRepository extends JpaRepository<GunStoreEntity,String> {
}
