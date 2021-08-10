package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.ClubEntity;
import com.shootingplace.shootingplace.domain.entities.LicenseEntity;
import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.models.License;
import com.shootingplace.shootingplace.domain.models.MemberDTO;
import com.shootingplace.shootingplace.repositories.LicenseRepository;
import com.shootingplace.shootingplace.repositories.MemberRepository;
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
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class LicenseServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    LicenseRepository licenseRepository;

    @InjectMocks
    LicenseService licenseService;

    private List<MemberEntity> membersList = getMemberEntities();

    private int i = 0;

    private final String pinCodeOK = "0127";

    @Before
    public void init() {
        when(memberRepository.findAll()).thenReturn(membersList);
    }

    @After
    public void tearDown() {
        membersList = getMemberEntities();
    }


    @Test
    public void getMembersNamesAndLicense() {
        //given
        //when
        List<MemberDTO> membersNamesAndLicense = licenseService.getMembersNamesAndLicense();
        //then
        Assert.assertThat(membersNamesAndLicense.get(0), Matchers.isA(MemberDTO.class));
    }

    @Test
    public void getMembersNamesAndLicenseNotValid() {
        //given
        //when
        List<MemberDTO> membersNamesAndLicense = licenseService.getMembersNamesAndLicenseNotValid();
        //then
        Assert.assertThat(membersNamesAndLicense.get(0), Matchers.isA(MemberDTO.class));
    }

    @Test
    public void getLicense() {
        //given
        //when
        License license = licenseService.getLicense();
        //then
        assertThat(license.getNumber(),Matchers.equalTo(null));
    }

    private List<MemberEntity> getMemberEntities() {
        List<MemberEntity> list = new ArrayList<>();
        MemberEntity member1 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John1")
                .secondName("Doe1")
                .email("sample1@mail.com")
                .pesel("63011744727").phoneNumber("+48111111111")
                .IDCard("AAA 999991")
                .club(createClub())
                .legitimationNumber(1)
                .adult(true).active(true).erased(false)
                .license(createLicense())
                .joinDate(LocalDate.now())
                .build();
        MemberEntity member2 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John2")
                .secondName("Doe2")
                .email("sample2@mail.com")
                .pesel("77100614134").phoneNumber("+48222222222")
                .IDCard("AAA 999992")
                .club(createClub())
                .legitimationNumber(2)
                .adult(false).active(true).erased(false)
                .license(createLicense())
                .joinDate(LocalDate.now())
                .build();
        MemberEntity member3 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John3")
                .secondName("Doe3")
                .email("sample3@mail.com")
                .pesel("84102078413").phoneNumber("+48333333333")
                .IDCard("AAA 999993")
                .club(createClub())
                .legitimationNumber(3)
                .adult(true).active(false).erased(false)
                .license(createLicense())
                .joinDate(LocalDate.now())
                .build();
        MemberEntity member4 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John4")
                .secondName("Doe4")
                .email("sample4@mail.com")
                .pesel("87111483627").phoneNumber("+48444444444")
                .IDCard("AAA 999994")
                .club(createClub())
                .legitimationNumber(4)
                .adult(false).active(false).erased(false)
                .license(createLicense())
                .joinDate(LocalDate.now())
                .build();
        MemberEntity member5 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John5")
                .secondName("Doe5")
                .email("sample5@mail.com")
                .pesel("79082032935").phoneNumber("+48555555555")
                .IDCard("AAA 999995")
                .club(createClub())
                .legitimationNumber(5)
                .adult(true).active(false).erased(true)
                .license(createLicense())
                .joinDate(LocalDate.now())
                .build();
        MemberEntity member6 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John6")
                .secondName("Doe6")
                .email("sample6@mail.com")
                .pesel("90031875364").phoneNumber("+48666666666")
                .IDCard("AAA 999996")
                .club(createClub())
                .legitimationNumber(6)
                .adult(false).active(false).erased(true)
                .license(createLicense())
                .joinDate(LocalDate.now())
                .build();
        MemberEntity member8 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John8")
                .secondName("Doe8")
                .email("sample8@mail.com")
                .pesel("72070186261").phoneNumber("+48888888888")
                .IDCard("AAA 999998")
                .club(createClub())
                .legitimationNumber(8)
                .adult(false).active(true).erased(false)
                .license(createLicense())
                .joinDate(LocalDate.now().minusYears(3))
                .build();
        list.add(member1);
        list.add(member2);
        list.add(member3);
        list.add(member4);
        list.add(member5);
        list.add(member6);
        list.add(member8);
        return list;
    }

    private LicenseEntity createLicense() {

        Random r = new Random();
        int a = r.nextInt(10) + 1;
        if (i == 1) {
            return LicenseEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .number(String.valueOf(Math.round(Math.random())))
                    .validThru(LocalDate.of(LocalDate.now().getYear(), 12, 31))
                    .valid(true)
                    .build();
        }
        if (i == 2) {
            return LicenseEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .number(String.valueOf(Math.round(Math.random())))
                    .validThru(LocalDate.of(LocalDate.now().getYear()-2, 12, 31))
                    .valid(false)
                    .build();
        }
        if (a % 2 == 0) {
            return LicenseEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .number(String.valueOf(Math.round(Math.random())))
                    .validThru(LocalDate.of(LocalDate.now().getYear() - 2, 12, 31))
                    .valid(false)
                    .build();
        } else {
            return LicenseEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .number(String.valueOf(Math.round(Math.random())))
                    .validThru(LocalDate.of(LocalDate.now().getYear(), 12, 31))
                    .valid(true)
                    .build();
        }
    }
    private ClubEntity createClub() {
        Random r = new Random();
        int i = r.nextInt(10000);
        return ClubEntity.builder()
                .id(1)
                .name("Some Club")
                .fullName("Some Club in Some City")
                .licenseNumber(i + "/2021")
                .build();
    }

}