package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.ChangeHistoryEntity;
import com.shootingplace.shootingplace.domain.entities.UserEntity;
import com.shootingplace.shootingplace.repositories.ChangeHistoryRepository;
import com.shootingplace.shootingplace.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class ChangeHistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;
    private final UserRepository userRepository;


    public ChangeHistoryService(ChangeHistoryRepository changeHistoryRepository, UserRepository userRepository) {
        this.changeHistoryRepository = changeHistoryRepository;
        this.userRepository = userRepository;
    }


    ChangeHistoryEntity addRecord(UserEntity user, String classNamePlusMethod, String memberUUID) {
        return changeHistoryRepository.save(ChangeHistoryEntity.builder()
                .userEntity(user)
                .classNamePlusMethod(classNamePlusMethod)
                .belongsTo(memberUUID)
                .dayNow(LocalDate.now())
                .timeNow(String.valueOf(LocalTime.now()))
                .build());
    }

    public boolean comparePinCode(String pinCode) {

        return userRepository.findAll().stream().anyMatch(f -> f.getPinCode().equals(pinCode));
    }

    void addRecordToChangeHistory(String pinCode, String classNamePlusMethod, String uuid) {

        UserEntity userEntity = userRepository.findAll().stream().filter(f -> f.getPinCode().equals(pinCode)).findFirst().orElse(null);

        if (userEntity != null) {

            userEntity.getList().add(addRecord(userEntity, classNamePlusMethod, uuid));
            userRepository.save(userEntity);
        }
    }
}

