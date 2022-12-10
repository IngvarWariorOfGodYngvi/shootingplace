package com.shootingplace.shootingplace.file;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FilesRepository {
    List<FilesEntity> findAll();

    FilesEntity save(FilesEntity filesEntity);

    boolean existsById(String uuid);

    void deleteById(String uuid);

    Optional<FilesEntity> findById(String uuid);

    Page<FilesEntity> findAll(Pageable page);

    Optional<FilesEntity> findByName(String fileName);

    Page<FilesEntity> findAllByDateIsNotNullAndTimeIsNotNull(Pageable page);

    List<FilesEntity> findAllByDateIsNullAndTimeIsNull();
}

