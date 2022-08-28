package com.shootingplace.shootingplace.history;

import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
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


    public ChangeHistoryEntity addRecord(UserEntity user, String classNamePlusMethod, String memberUUID) {
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

    public void addRecordToChangeHistory(String pinCode, String classNamePlusMethod, String uuid) {

        UserEntity userEntity = userRepository.findAll().stream().filter(f -> f.getPinCode().equals(pinCode)).findFirst().orElse(null);

        if (userEntity != null) {

            userEntity.getList().add(addRecord(userEntity, classNamePlusMethod, uuid));
            userRepository.save(userEntity);
        }
    }
}

