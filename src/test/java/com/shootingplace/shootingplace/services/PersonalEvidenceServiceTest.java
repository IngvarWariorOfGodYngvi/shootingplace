package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.models.PersonalEvidence;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class PersonalEvidenceServiceTest {

    @InjectMocks
    PersonalEvidenceService personalEvidenceService;

    @Test
    public void getPersonalEvidence() {
        //given
        //when
        PersonalEvidence personalEvidence = personalEvidenceService.getPersonalEvidence();
        //then
        assertThat(personalEvidence.getAmmoList(), Matchers.equalTo(new ArrayList<>()));
    }
}