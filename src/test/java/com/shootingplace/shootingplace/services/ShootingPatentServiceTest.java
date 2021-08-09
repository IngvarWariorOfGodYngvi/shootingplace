package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.entities.ShootingPatentEntity;
import com.shootingplace.shootingplace.domain.models.ShootingPatent;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import com.shootingplace.shootingplace.repositories.ShootingPatentRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class ShootingPatentServiceTest {
    int i = 1;
    private List<MemberEntity> membersList = getMemberEntities();

    @Mock
    ShootingPatentRepository shootingPatentRepository;
    @Mock
    MemberRepository memberRepository;
    @Mock
    HistoryService historyService;

    @InjectMocks
    ShootingPatentService shootingPatentService;

    @Before
    public void init() {
        when(memberRepository.findAll()).thenReturn(membersList);
    }

    @After
    public void tearDown() {
        membersList = getMemberEntities();
    }


    @Test
    public void update_patent_return_true() {
        //given
        String uuid = membersList.get(0).getUuid();
        Random r = new Random();
        int i = r.nextInt(100000);
        String year = String.valueOf(LocalDate.now().getYear());
        String monthValue = String.valueOf(LocalDate.now().getMonthValue());
        ShootingPatent shootingPatent = ShootingPatent.builder()
                .patentNumber(i + "/PAT/" + monthValue + year)
                .dateOfPosting(LocalDate.now())
                .dateOfPosting(LocalDate.now())
                .pistolPermission(true)
                .riflePermission(true)
                .shotgunPermission(true)
                .build();
        //when
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        boolean b = shootingPatentService.updatePatent(uuid, shootingPatent);
        //then
        assertThat(b, Matchers.equalTo(true));
    }

    @Test
    public void update_patent_return_false() {
        //given
        String uuid = membersList.get(1).getUuid();
        ShootingPatent shootingPatent = ShootingPatent.builder()
                .patentNumber(membersList.get(1).getShootingPatent().getPatentNumber())
                .dateOfPosting(LocalDate.now())
                .pistolPermission(true)
                .riflePermission(true)
                .shotgunPermission(true)
                .build();
        //when
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        boolean b = shootingPatentService.updatePatent(uuid, shootingPatent);
        //then
        assertThat(b, Matchers.equalTo(false));
    }

    @Test
    public void getShootingPatent() {
        //given
        //when
        ShootingPatent shootingPatent = shootingPatentService.getShootingPatent();
        //then
        assertThat(shootingPatent.getPatentNumber(), Matchers.equalTo(null));
    }

    private MemberEntity findByID(String uuid) {
        return membersList.stream().filter(f -> f.getUuid().equals(uuid)).findFirst().orElseThrow(EntityNotFoundException::new);
    }

    private MemberEntity getMember() {
        return MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John" + i)
                .secondName("Doe" + i)
                .email("sample" + i + "@mail.com")
                .pesel("63011744727").phoneNumber("+48111111111")
                .IDCard("AAA 99999" + i)
                .legitimationNumber(1)
                .adult(true).active(true).erased(false)
                .shootingPatent(createShootingPatent())
                .build();
    }

    private ShootingPatentEntity createShootingPatent() {
        Random r = new Random();
        int i1 = r.nextInt(10) + 1;
        if (i == 1) {
            ShootingPatentEntity build = ShootingPatentEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .patentNumber(null)
                    .dateOfPosting(null)
                    .pistolPermission(false)
                    .riflePermission(false)
                    .shotgunPermission(false)
                    .build();
            i++;
            return build;
        }

        int i = r.nextInt(100000);
        String year = String.valueOf(LocalDate.now().getYear());
        String monthValue = String.valueOf(LocalDate.now().getMonthValue());
        return ShootingPatentEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .patentNumber(i + "/PAT/" + monthValue + "/" + year)
                .dateOfPosting(LocalDate.now())
                .pistolPermission(true)
                .riflePermission(true)
                .shotgunPermission(true)
                .build();

    }

    private List<MemberEntity> getMemberEntities() {
        List<MemberEntity> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(getMember());
        }
        return list;
    }
}