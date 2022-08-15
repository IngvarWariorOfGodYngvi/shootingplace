package com.shootingplace.shootingplace.tournament;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository {
    Optional<TournamentEntity> findById(String uuid);

    TournamentEntity save(TournamentEntity entity);

    List<TournamentEntity> findAll();

    boolean existsById(String uuid);

    void delete(TournamentEntity entity);
}
