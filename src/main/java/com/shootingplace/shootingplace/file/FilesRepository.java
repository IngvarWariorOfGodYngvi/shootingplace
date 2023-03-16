package com.shootingplace.shootingplace.file;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FilesRepository {
    List<FilesEntity> findAll();

    FilesEntity save(FilesEntity filesEntity);

    boolean existsById(String uuid);

    void deleteById(String uuid);

    Optional<FilesEntity> findById(String uuid);

    Page<FilesEntity> findAll(Pageable page);

    Page<FilesEntity> findAllByDateIsNotNullAndTimeIsNotNull(Pageable page);

    List<FilesEntity> findAllByDateIsNullAndTimeIsNull();

    List<FilesEntity> findAllByBelongToMemberUUIDEquals(String uuid);

    @Query(nativeQuery = true, value = "SELECT * FROM shootingplace.files_entity where name like '%raport_pracy%' and name like (:monthName) and name like (:workType) and name like (:year)")
    List<FilesEntity> findAllByNameContains(@Param("monthName") String month,@Param("year") String year, @Param("workType") String workType);
}

