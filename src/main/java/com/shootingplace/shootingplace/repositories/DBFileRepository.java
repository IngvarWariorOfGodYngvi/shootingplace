package com.shootingplace.shootingplace.repositories;

import com.shootingplace.shootingplace.domain.entities.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DBFileRepository extends JpaRepository<ImageEntity, String> {
}