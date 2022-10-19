package com.shootingplace.shootingplace.club;

import java.util.List;
import java.util.Optional;

public interface ClubRepository{
    List<ClubEntity> findAll();

    boolean existsById(int id);

    Optional<ClubEntity> findById(int id);

    ClubEntity save(ClubEntity entity);

    ClubEntity getOne(int i);
}
