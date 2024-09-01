package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.contributions.ContributionService;
import com.shootingplace.shootingplace.history.*;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.contributions.ContributionRepository;
import com.shootingplace.shootingplace.member.MemberRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class ContributionServiceTest {
    @Mock
    MemberRepository memberRepository;
    @Mock
    ContributionRepository contributionRepository;
    @Mock
    HistoryService historyService;
    @Mock
    ChangeHistoryService changeHistoryService;
    @Mock
    Clock clock;

    private final static LocalDate LOCAL_DATE = LocalDate.of(2021, 11, 2);

    @InjectMocks
    ContributionService contributionService;

    private int i = 1;

    private List<MemberEntity> membersList = getMemberEntities();

    private final String pinCode = "0125";

    @Before
    public void init() {

        Clock fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
//        doReturn(fixedClock.instant()).when(clock).instant();
//        doReturn(fixedClock.getZone()).when(clock).getZone();
//        when(memberRepository.findAll()).thenReturn(membersList);
    }

    @After
    public void tearDown() {
        membersList = getMemberEntities();
    }

//    @Test
//    public void add_contribution_bad_request_no_member() {
//        //given
//        String uuid = String.valueOf(UUID.randomUUID());
//        LocalDate date = LocalDate.now();
//        //when
//        when(memberRepository.existsById(any(String.class))).thenReturn(existsById(uuid));
//        ResponseEntity<?> responseEntity = contributionService.addContribution(uuid, date, pinCode);
//        //then
//        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
//        assertThat(responseEntity.getBody(), Matchers.equalTo("Nie znaleziono Klubowicza"));
//
//    }

//    @Test
//    public void add_contribution_OK() {
//        //given
//        MemberEntity memberEntity = membersList.get(0);
//        String uuid = membersList.get(0).getUuid();
//        LocalDate date = LocalDate.of(2021,11,2);
//
//
//        //when
//        when(memberRepository.existsById(any(String.class))).thenReturn(existsById(uuid));
//        when(memberRepository.findById(any(String.class))).thenReturn(java.util.Optional.ofNullable(findByID(uuid)));
//        ResponseEntity<?> responseEntity = contributionService.addContribution(uuid, date, pinCode);
//        //then
//        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
//        assertThat(responseEntity.getBody(), Matchers.equalTo("Przedłużono składkę " + memberEntity.getSecondName() + " " + memberEntity.getFirstName()));
//
//    }

    @Test
    public void addContributionRecord() {
        //given
        //when
        //then
    }

    @Test
    public void addFirstContribution() {
        //given
        //when
        //then
    }

    @Test
    public void removeContribution() {
        //given
        //when
        //then
    }

    @Test
    public void updateContribution() {
        //given
        //when
        //then
    }

    private boolean existsById(String uuid) {
        return membersList.stream().anyMatch(f -> f.getUuid().equals(uuid));
    }

    private MemberEntity findByID(String uuid) {
        return membersList.stream().filter(f -> f.getUuid().equals(uuid)).findFirst().orElseThrow(EntityNotFoundException::new);
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
                .legitimationNumber(1)
                .adult(true).active(true).erased(false)
                .joinDate(LocalDate.now())
                .history(createHistory())
                .build();
        MemberEntity member2 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John2")
                .secondName("Doe2")
                .email("sample2@mail.com")
                .pesel("77100614134").phoneNumber("+48222222222")
                .IDCard("AAA 999992")
                .legitimationNumber(2)
                .adult(false).active(true).erased(false)
                .joinDate(LocalDate.now())
                .history(createHistory())
                .build();
        MemberEntity member3 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John3")
                .secondName("Doe3")
                .email("sample3@mail.com")
                .pesel("84102078413").phoneNumber("+48333333333")
                .IDCard("AAA 999993")
                .legitimationNumber(3)
                .adult(true).active(false).erased(false)
                .joinDate(LocalDate.now())
                .history(createHistory())
                .build();
        MemberEntity member4 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John4")
                .secondName("Doe4")
                .email("sample4@mail.com")
                .pesel("87111483627").phoneNumber("+48444444444")
                .IDCard("AAA 999994")
                .legitimationNumber(4)
                .adult(false).active(false).erased(false)
                .joinDate(LocalDate.now())
                .history(createHistory())
                .build();
        MemberEntity member5 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John5")
                .secondName("Doe5")
                .email("sample5@mail.com")
                .pesel("79082032935").phoneNumber("+48555555555")
                .IDCard("AAA 999995")
                .legitimationNumber(5)
                .adult(true).active(false).erased(true)
                .joinDate(LocalDate.now())
                .history(createHistory())
                .build();
        MemberEntity member6 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John6")
                .secondName("Doe6")
                .email("sample6@mail.com")
                .pesel("90031875364").phoneNumber("+48666666666")
                .IDCard("AAA 999996")
                .legitimationNumber(6)
                .adult(false).active(false).erased(true)
                .joinDate(LocalDate.now())
                .history(createHistory())
                .build();
        MemberEntity member8 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John8")
                .secondName("Doe8")
                .email("sample8@mail.com")
                .pesel("72070186261").phoneNumber("+48888888888")
                .IDCard("AAA 999998")
                .legitimationNumber(8)
                .adult(false).active(true).erased(false)
                .joinDate(LocalDate.now().minusYears(3))
                .history(createHistory())
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

    private HistoryEntity createHistory() {
        List<CompetitionHistoryEntity> competitionHistoryEntityList = new ArrayList<>();
        List<ContributionEntity> contributionEntityList = new ArrayList<>();
        List<LicensePaymentHistoryEntity> licensePaymentHistoryEntityList = new ArrayList<>();

        String historyUUID = String.valueOf(UUID.randomUUID());
        Random r = new Random();
        int a = r.nextInt(10) + 1;
        ContributionEntity contributionEntity = ContributionEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .historyUUID(historyUUID)
                .paymentDay(LocalDate.now())
                .validThru(LocalDate.of(LocalDate.now().getYear(), 12, 31))
                .build();

            contributionEntityList.add(contributionEntity);
        i++;
        return HistoryEntity.builder()
                .uuid(historyUUID)
                .competitionHistory(competitionHistoryEntityList)
                .contributionList(contributionEntityList)
                .licensePaymentHistory(licensePaymentHistoryEntityList)
                .build();
    }
}