package com.shootingplace.shootingplace.users;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
//    UserEntity findByCardNumber(String number);

//    boolean existsByCardNumber(String number);

    List<UserEntity> findAll();

    UserEntity save(UserEntity entity);

    Optional<UserEntity> findById(String uuid);

    boolean existsById(String uuid);
}
