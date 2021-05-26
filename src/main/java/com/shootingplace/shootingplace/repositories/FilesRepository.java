package com.shootingplace.shootingplace.repositories;

import com.shootingplace.shootingplace.domain.entities.FilesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilesRepository extends JpaRepository<FilesEntity, String> {
}

