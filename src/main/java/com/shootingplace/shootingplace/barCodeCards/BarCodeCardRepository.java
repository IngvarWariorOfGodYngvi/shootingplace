package com.shootingplace.shootingplace.barCodeCards;

public interface BarCodeCardRepository {
    BarCodeCardEntity save(BarCodeCardEntity entity);

    boolean existsByBarCode(String barCode);

    boolean existsByBelongsT(String belongsTo);
}
