package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.domain.models.AmmoDTO;
import com.shootingplace.shootingplace.repositories.AmmoEvidenceRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class AmmoEvidenceServiceTest {

    @Mock
    AmmoEvidenceRepository ammoEvidenceRepository;
    @Mock
    ChangeHistoryService changeHistoryService;
    @InjectMocks
    AmmoEvidenceService ammoEvidenceService;

    private List<AmmoEvidenceEntity> ammoEvidenceEntities = getAmmoEvidenceEntities();

    private int i = 1;
    private int max = 10;

    @Before
    public void init() {
        when(ammoEvidenceRepository.findAll()).thenReturn(ammoEvidenceEntities);
    }

    @After
    public void tearDown() {
        i = 1;
        max = 10;
        ammoEvidenceEntities = getAmmoEvidenceEntities();
    }

    @Test
    public void get_all_evidences_open() {
        //given
        boolean open = true;
        //when
        List<AmmoEvidenceEntity> allEvidences = ammoEvidenceService.getAllEvidences(open);
        //then
        Assert.assertThat(allEvidences, Matchers.hasSize(1));
    }

    @Test
    public void get_all_evidences_close() {
        //given
        boolean open = false;
        //when
        List<AmmoEvidenceEntity> allEvidences = ammoEvidenceService.getAllEvidences(open);
        //then
        Assert.assertThat(allEvidences, Matchers.hasSize(9));
    }

    @Test
    public void getEvidence() {
        //given
        String uuid = ammoEvidenceEntities.get(0).getUuid();
        //when
        when(ammoEvidenceRepository.getOne(any(String.class))).thenReturn(getOne(uuid));
        AmmoEvidenceEntity evidence = ammoEvidenceService.getEvidence(uuid);
        //then
        Assert.assertThat(evidence.getUuid(), Matchers.equalTo(uuid));
    }

    @Test
    public void close_evidence_return_false() {
        //given
        String uuid = String.valueOf(UUID.randomUUID());
        //when
        when(ammoEvidenceRepository.existsById(any(String.class))).thenReturn(existsById(uuid));
        boolean evidence = ammoEvidenceService.closeEvidence(uuid);
        //then
        Assert.assertThat(evidence, Matchers.equalTo(false));
    }

    @Test
    public void close_evidence_return_true() {
        //given
        String uuid = ammoEvidenceEntities.get(5).getUuid();
        //when
        when(ammoEvidenceRepository.existsById(any(String.class))).thenReturn(existsById(uuid));
        when(ammoEvidenceRepository.findById(any(String.class))).thenReturn(java.util.Optional.ofNullable(findById(uuid)));
        boolean evidence = ammoEvidenceService.closeEvidence(uuid);
        //then
        Assert.assertThat(evidence, Matchers.equalTo(true));
    }

    @Test
    public void get_closed_evidences() {
        //given
        //when
        when(ammoEvidenceRepository.findAll()).thenReturn(ammoEvidenceEntities);
        List<AmmoDTO> closedEvidences = ammoEvidenceService.getClosedEvidences();
        //then
        assertThat(closedEvidences, Matchers.hasSize(9));
        assertThat(closedEvidences.get(0), Matchers.isA(AmmoDTO.class));
    }

    @Test
    public void open_evidence_return_false() {
        //given
        String uuid = ammoEvidenceEntities.get(5).getUuid();
        //when
        boolean b = ammoEvidenceService.openEvidence(uuid, "0125");
        //then
        assertThat(b, Matchers.equalTo(false));
    }

    @Test
    public void open_evidence_return_true() {
        //given
        List<AmmoEvidenceEntity> list = ammoEvidenceEntities;
        AmmoEvidenceEntity ammoEvidenceEntity = list.get(1);
        list.remove(ammoEvidenceEntity);
        String uuid = list.get(5).getUuid();
        //when
        when(ammoEvidenceRepository.findAll()).thenReturn(list);
        when(ammoEvidenceRepository.findById(any(String.class))).thenReturn(java.util.Optional.ofNullable(findById(uuid)));
        boolean b = ammoEvidenceService.openEvidence(uuid, "0125");
        //then
        assertThat(b, Matchers.equalTo(true));
    }

    @Test
    public void check_any_open_evidence_all_evidence_close() {
        //given
        List<AmmoEvidenceEntity> list = ammoEvidenceEntities;
        AmmoEvidenceEntity ammoEvidenceEntity = list.get(1);
        list.remove(ammoEvidenceEntity);
        //when
        when(ammoEvidenceRepository.findAll()).thenReturn(list);
        String s = ammoEvidenceService.checkAnyOpenEvidence();
        //then
        assertThat(s, Matchers.equalTo("\"primary\""));
    }

    @Test
    public void check_any_open_evidence_return_message_negative() {
        //given
        List<AmmoEvidenceEntity> list = ammoEvidenceEntities;
        //when
        when(ammoEvidenceRepository.findAll()).thenReturn(list);
        String s = ammoEvidenceService.checkAnyOpenEvidence();
        //then
        assertThat(s, Matchers.equalTo("\"negative\""));
    }

    @Test
    public void check_any_open_evidence_return_message_red() {
        //given
        List<AmmoEvidenceEntity> list = ammoEvidenceEntities;
        list.get(1).setForceOpen(true);
        //when
        when(ammoEvidenceRepository.findAll()).thenReturn(list);
        String s = ammoEvidenceService.checkAnyOpenEvidence();
        //then
        assertThat(s, Matchers.equalTo("\"red\""));
    }

    private AmmoEvidenceEntity getOne(String uuid) {
        return ammoEvidenceEntities.stream().filter(f -> f.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    private boolean existsById(String uuid) {
        return ammoEvidenceEntities.stream().anyMatch(f -> f.getUuid().equals(uuid));
    }

    private AmmoEvidenceEntity findById(String uuid) {
        return ammoEvidenceEntities.stream().filter(f -> f.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    private List<AmmoEvidenceEntity> getAmmoEvidenceEntities() {
        List<AmmoEvidenceEntity> list = new ArrayList<>();

        for (int y = 0; y < 10; y++) {
            list.add(getAmmoEvidenceEntity());
        }
        return list;
    }

    private AmmoEvidenceEntity getAmmoEvidenceEntity() {
        boolean open = false;
        if (i == 1) {
            open = true;
        }
        AmmoEvidenceEntity build = AmmoEvidenceEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .ammoInEvidenceEntityList(new ArrayList<>())
                .forceOpen(false)
                .open(open)
                .date(LocalDate.now().minusDays(max - i))
                .number("-LA-" + i)
                .build();
        i++;
        return build;
    }

}