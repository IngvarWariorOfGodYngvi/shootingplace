package com.shootingplace.shootingplace.history;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class ChangeHistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;
    private final UserRepository userRepository;
    private final WorkingTimeEvidenceService workServ;


    public ChangeHistoryService(ChangeHistoryRepository changeHistoryRepository, UserRepository userRepository, WorkingTimeEvidenceService workServ) {
        this.changeHistoryRepository = changeHistoryRepository;
        this.userRepository = userRepository;
        this.workServ = workServ;
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
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        return userRepository.findAll().stream().anyMatch(f -> f.getPinCode().equals(pin));
    }

    public ResponseEntity<String> addRecordToChangeHistory(String pinCode, String classNamePlusMethod, String uuid) {
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity userEntity = userRepository.findAll().stream().filter(f -> f.getPinCode().equals(pin)).findFirst().orElse(null);
        // user is found
        if (userEntity != null) {
            // user isn't in work
            if (!workServ.isInWork(userEntity)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Najpierw zarejestruj pobyt w Klubie");
            }
            userEntity.getList().add(addRecord(userEntity, classNamePlusMethod, uuid));
            userRepository.save(userEntity);
            return null;
        }
        //user not found
        else {
            return ResponseEntity.status(403).body("Brak Uprawnień");
        }
    }
}

