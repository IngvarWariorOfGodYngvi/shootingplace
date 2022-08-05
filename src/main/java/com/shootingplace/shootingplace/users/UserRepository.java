package com.shootingplace.shootingplace.users;

import java.util.List;

public interface UserRepository {
    UserEntity findByCardNumber(String number);

    boolean existsByCardNumber(String number);

    List<UserEntity> findAll();

    UserEntity save(UserEntity entity);
}
