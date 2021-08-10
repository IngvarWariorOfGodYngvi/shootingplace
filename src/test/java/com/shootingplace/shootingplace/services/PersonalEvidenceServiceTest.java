package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.models.PersonalEvidence;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

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
        Assert.assertThat(personalEvidence.getAmmoList(), Matchers.equalTo(new ArrayList<>()));
    }
}