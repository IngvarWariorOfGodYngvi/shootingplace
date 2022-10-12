package com.shootingplace.shootingplace.armory;

import java.util.List;

public interface GunStoreRepository{
    List<GunStoreEntity> findAll();

    GunStoreEntity save(GunStoreEntity entity);
}
