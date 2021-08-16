package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.models.PersonalEvidence;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class PersonalEvidenceService {

    public PersonalEvidence getPersonalEvidence() {
        return PersonalEvidence.builder()
                .ammoList(new ArrayList<>())
                .build();
    }

}
