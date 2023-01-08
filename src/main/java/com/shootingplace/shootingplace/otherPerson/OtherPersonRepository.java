package com.shootingplace.shootingplace.otherPerson;

import java.util.List;
import java.util.Optional;

public interface OtherPersonRepository{
    Optional<OtherPersonEntity> findById(int otherID);

    List<OtherPersonEntity> findAll();

    OtherPersonEntity save(OtherPersonEntity entity);

    OtherPersonEntity getOne(Integer id);

    boolean existsById(int id);
}
