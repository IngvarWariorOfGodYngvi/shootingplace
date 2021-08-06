package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.*;
import com.shootingplace.shootingplace.domain.models.MemberDTO;
import com.shootingplace.shootingplace.repositories.LicenseRepository;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    LicenseRepository licenseRepository;

    @InjectMocks
    MemberService memberService;

    private final List<MemberEntity> membersList = getMemberEntities();

    private int i = 0;

    private final int index = 0;
    private final MemberEntity memberEntity = membersList.get(index);
    private String uuid = membersList.get(index).getUuid();


    @Before
    public void init() {
        when(memberRepository.findAll()).thenReturn(membersList);
        when(memberRepository.findAllByErasedIsTrue()).thenReturn(erasedList());
        when(memberRepository.findById(uuid)).thenReturn(java.util.Optional.ofNullable(memberEntity));
    }

    @Test
    public void getMember() {
        //when
        MemberEntity member = memberService.getMember(1);
        //then
        assertThat(member.getEmail(), Matchers.equalTo("sample1@mail.com"));
    }

    @Test
    public void getMemberErased() {
        //when
        List<MemberEntity> members = memberService.getErasedMembers();
        //then
        assertEquals(members, erasedList());
    }

    @Test
    public void getMembersEmailsAdult() {
        //given
        //when
        List<String> list = memberService.getMembersEmails(true);
        //then
        assertThat(list, Matchers.hasSize(1));
        MemberEntity memberEntity = membersList.get(0);
        assertThat(list, Matchers.hasItem(memberEntity.getEmail() + ";"));
    }

    @Test
    public void getMembersEmailsNonAdult() {
        //given
        //when
        List<String> list = memberService.getMembersEmails(false);
        //then
        assertThat(list, Matchers.hasSize(1));
        MemberEntity memberEntity = membersList.get(1);
        assertThat(list, Matchers.hasItem(memberEntity.getEmail() + ";"));
    }

    @Test
    public void getMemberByUUID() {
        //given
        MemberEntity memberEntity = membersList.get(0);
        //when
        uuid = membersList.get(index).getUuid();
        MemberEntity memberByUUID = memberService.getMemberByUUID(uuid);
        //then
        assertThat(memberByUUID, Matchers.equalTo(memberEntity));
    }

    @Test
    public void getAllNames() {
        //given
        //when
        List<String> allNames = memberService.getAllNames();
        //then
        assertThat(allNames, Matchers.hasSize(2));

    }

    @Test
    public void getAllMemberDTO() {
        //given
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO();
        List<MemberDTO> memberDTOS = new ArrayList<>();
        membersList.stream().filter(f->!f.getErased()).forEach(e -> {
            memberDTOS.add(Mapping.map2DTO(e));
        });
        memberDTOS.sort(Comparator.comparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName));
        //then
        assertThat(allMemberDTO, Matchers.hasSize(2));
        assertEquals(allMemberDTO.get(0).getSecondName(), memberDTOS.get(0).getSecondName());

    }


    private List<MemberEntity> erasedList() {
        return membersList.stream().filter(MemberEntity::getErased).collect(Collectors.toList());
    }

    @NotNull
    private List<MemberEntity> getMemberEntities() {
        List<MemberEntity> list = new ArrayList<>();
        MemberEntity member1 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John1")
                .secondName("Doe1")
                .email("sample1@mail.com")
                .pesel("90120510813").phoneNumber("+48111111111")
                .IDCard("AAA 999991")
                .legitimationNumber(1)
                .active(true).adult(true).erased(false)
                .history(createHistory())
                .license(createLicense())
                .club(createClub())
                .build();
        MemberEntity member2 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John2")
                .secondName("Doe2")
                .email("sample2@mail.com")
                .pesel("22222222222").phoneNumber("+48222222222")
                .IDCard("AAA 999992")
                .legitimationNumber(2)
                .active(true).adult(false).erased(false)
                .history(createHistory())
                .license(createLicense())
                .club(createClub())
                .build();
        MemberEntity member3 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John3")
                .secondName("Doe3")
                .email("sample3@mail.com")
                .pesel("22222222222").phoneNumber("+48333333333")
                .IDCard("AAA 999993")
                .legitimationNumber(3)
                .adult(true).active(false).erased(true)
                .history(createHistory())
                .license(createLicense())
                .club(createClub())
                .build();
        list.add(member1);
        list.add(member2);
        list.add(member3);
        return list;
    }

    private HistoryEntity createHistory() {
        List<CompetitionHistoryEntity> competitionHistoryEntityList = new ArrayList<>();
        List<ContributionEntity> contributionEntityList = new ArrayList<>();

        String historyUUID = String.valueOf(UUID.randomUUID());
        Random r = new Random();
        int a = r.nextInt(10) + 1;
        ContributionEntity contributionEntity = ContributionEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .historyUUID(historyUUID)
                .paymentDay(LocalDate.now())
                .validThru(LocalDate.of(LocalDate.now().getYear(), 12, 31))
                .build();
//        System.out.println(a % 2);
        if (a % 2 == 0 && i % 2 == 0) {
            contributionEntityList.add(contributionEntity);
        }
        i++;
        return HistoryEntity.builder()
                .uuid(historyUUID)
                .competitionHistory(competitionHistoryEntityList)
                .contributionList(contributionEntityList)
                .build();
    }

    private LicenseEntity createLicense() {

        return LicenseEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .number(String.valueOf(Math.round(Math.random())))
                .validThru(LocalDate.of(LocalDate.now().getYear(), 12, 31))
                .build();
    }

    private ClubEntity createClub() {
        Random r = new Random();
        int i = r.nextInt(10000);
        return ClubEntity.builder()
                .name("Some Club")
                .fullName("Some Club in SOme City")
                .licenseNumber(i + "/2021")
                .build();
    }


}