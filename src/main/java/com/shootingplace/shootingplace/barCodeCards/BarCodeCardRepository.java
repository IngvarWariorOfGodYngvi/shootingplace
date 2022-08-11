package com.shootingplace.shootingplace.barCodeCards;

public interface BarCodeCardRepository {
    BarCodeCardEntity save(BarCodeCardEntity entity);

    boolean existsByBarCode(String barCode);

    BarCodeCardEntity findByBarCode(String number);

//    boolean existsByBelongsTo(String belongsTo);
}
