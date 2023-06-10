package com.shootingplace.shootingplace.BookOfRegistrationOfStayAtTheShootingPlace;

import java.util.List;


public interface RegistrationRecordRepository{
    List<RegistrationRecordEntity> findAll();

    RegistrationRecordEntity save(RegistrationRecordEntity entity);

    List<RegistrationRecordEntity> findAllByEndDateTimeNull();
}
