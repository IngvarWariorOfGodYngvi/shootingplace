package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.BookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordEntity;
import com.shootingplace.shootingplace.BookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRecordSQlRepository extends RegistrationRecordRepository, JpaRepository<RegistrationRecordEntity, String> {
}
