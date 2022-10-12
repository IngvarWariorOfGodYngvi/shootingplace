package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.member.MemberEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class Logic {
    public final ChangeHistoryService changeHistoryService;

    public Logic(ChangeHistoryService changeHistoryService) {
        this.changeHistoryService = changeHistoryService;
    }

    public ResponseEntity<String> getStringResponseEntity(String pinCode, MemberEntity memberEntity, HttpStatus status, String methodName, String body) {
        ResponseEntity<String> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, memberEntity.getClass().getSimpleName() + " " + methodName + " ", memberEntity.getUuid());
        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }

}
