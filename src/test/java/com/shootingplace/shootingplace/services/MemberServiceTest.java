package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.*;
import com.shootingplace.shootingplace.domain.enums.ArbiterClass;
import com.shootingplace.shootingplace.domain.models.Member;
import com.shootingplace.shootingplace.domain.models.MemberDTO;
import com.shootingplace.shootingplace.repositories.LicenseRepository;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
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
    public void get_Member() {
        //when
        MemberEntity member = memberService.getMember(1);
        //then
        assertThat(member.getEmail(), Matchers.equalTo("sample1@mail.com"));
    }

    @Test
    public void get_Member_Erased() {
        //when
        List<MemberEntity> members = memberService.getErasedMembers();
        //then
        assertEquals(members, erasedList());
    }

    @Test
    public void get_Members_Emails_Adult() {
        //given
        //when
        List<String> list = memberService.getMembersEmails(true);
        //then
        assertThat(list, Matchers.hasSize(2));
        MemberEntity memberEntity = membersList.get(0);
        assertThat(list, Matchers.hasItem(memberEntity.getEmail() + ";"));
    }

    @Test
    public void get_Members_Emails_Non_Adult() {
        //given
        //when
        List<String> list = memberService.getMembersEmails(false);
        //then
        assertThat(list, Matchers.hasSize(2));
        MemberEntity memberEntity = membersList.get(1);
        assertThat(list, Matchers.hasItem(memberEntity.getEmail() + ";"));
    }

    @Test
    public void get_Member_By_UUID() {
        //given
        MemberEntity memberEntity = membersList.get(0);
        //when
        uuid = membersList.get(index).getUuid();
        MemberEntity memberByUUID = memberService.getMemberByUUID(uuid);
        //then
        assertThat(memberByUUID, Matchers.equalTo(memberEntity));
    }

    @Test
    public void get_All_Names() {
        //given
        //when
        List<String> allNames = memberService.getAllNames();
        //then
        assertThat(allNames, Matchers.hasSize(4));

    }

    @Test
    public void get_All_MemberDTO() {
        //given
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO();
        List<MemberDTO> memberDTOS = new ArrayList<>();
        membersList.stream().filter(f -> !f.getErased()).forEach(e ->
                memberDTOS.add(Mapping.map2DTO(e))
        );
        memberDTOS.sort(Comparator.comparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName));
        //then
        assertThat(allMemberDTO, Matchers.hasSize(4));
        assertEquals(allMemberDTO.get(0).getSecondName(), memberDTOS.get(0).getSecondName());

    }

    @Test
    public void get_All_MemberDTO_with_args_all_active() {
        //given
        Boolean adult = null;
        Boolean active = true;
        Boolean erased = false;
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO(adult, active, erased);
        //then
        assertThat(allMemberDTO, Matchers.hasSize(2));
    }

    @Test
    public void get_All_MemberDTO_with_args_all_not_erased() {
        //given
        Boolean adult = null;
        Boolean active = null;
        Boolean erased = false;
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO(adult, active, erased);
        //then
        assertThat(allMemberDTO, Matchers.hasSize(4));
    }

    @Test
    public void get_All_MemberDTO_with_args_all_not_active() {
        //given
        Boolean adult = null;
        Boolean active = false;
        Boolean erased = false;
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO(adult, active, erased);
        //then
        assertThat(allMemberDTO, Matchers.hasSize(2));
    }

    @Test
    public void get_All_MemberDTO_with_args_all_adult() {
        //given
        Boolean adult = true;
        Boolean active = null;
        Boolean erased = false;
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO(adult, active, erased);
        //then
        assertThat(allMemberDTO, Matchers.hasSize(2));
    }

    @Test
    public void get_All_MemberDTO_with_args_all_not_adult() {
        //given
        Boolean adult = false;
        Boolean active = null;
        Boolean erased = false;
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO(adult, active, erased);
        //then
        assertThat(allMemberDTO, Matchers.hasSize(2));
    }

    @Test
    public void get_All_MemberDTO_with_args_all_adult_erased() {
        //given
        Boolean adult = true;
        Boolean active = false;
        Boolean erased = true;
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO(adult, active, erased);
        //then
        assertThat(allMemberDTO, Matchers.hasSize(1));
        assertThat(allMemberDTO.get(0).getSecondName(), Matchers.equalTo("Doe5"));

    }

    @Test
    public void get_All_MemberDTO_with_args_adult_active_not_erased() {
        //given
        Boolean adult = true;
        Boolean active = true;
        Boolean erased = false;
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO(active, adult, erased);
        //then
        assertThat(allMemberDTO, Matchers.hasSize(1));
        assertThat(allMemberDTO.get(0).getSecondName(), Matchers.equalTo("Doe1"));

    }

    @Test
    public void get_All_MemberDTO_with_args_all_adult_not_active_not_erased() {
        //given
        Boolean adult = true;
        Boolean active = false;
        Boolean erased = false;
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO(adult, active, erased);
        //then
        assertThat(allMemberDTO, Matchers.hasSize(1));
        assertThat(allMemberDTO.get(0).getSecondName(), Matchers.equalTo("Doe3"));
    }

    @Test
    public void get_All_MemberDTO_with_args_not_adult_active_not_erased() {
        //given
        Boolean adult = false;
        Boolean active = true;
        Boolean erased = false;
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO(adult, active, erased);
        //then
        assertThat(allMemberDTO, Matchers.hasSize(1));
        assertThat(allMemberDTO.get(0).getSecondName(), Matchers.equalTo("Doe2"));

    }


    @Test
    public void get_All_MemberDTO_with_args_all_not_adult_not_active_not_erased() {
        //given
        Boolean adult = false;
        Boolean active = false;
        Boolean erased = false;
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO(adult, active, erased);
        //then
        assertThat(allMemberDTO, Matchers.hasSize(1));
        assertThat(allMemberDTO.get(0).getSecondName(), Matchers.equalTo("Doe4"));
    }


    @Test
    public void get_All_MemberDTO_with_args_all_not_adult_erased() {
        //given
        Boolean adult = false;
        Boolean active = false;
        Boolean erased = true;
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO(adult, active, erased);
        //then
        assertThat(allMemberDTO, Matchers.hasSize(1));
        assertThat(allMemberDTO.get(0).getSecondName(), Matchers.equalTo("Doe6"));
    }


    @Test
    public void get_members_quantity() {
        //given
        //when
        List<Long> list = memberService.getMembersQuantity();
        //then
        assertThat(list.get(0), Matchers.equalTo(2L));
        assertThat(list.get(1), Matchers.equalTo(1L));
        assertThat(list.get(2), Matchers.equalTo(1L));
        assertThat(list.get(3), Matchers.equalTo(2L));
        assertThat(list.get(4), Matchers.equalTo(1L));
        assertThat(list.get(5), Matchers.equalTo(1L));
        assertThat(list.get(6), Matchers.equalTo(1L));
    }

    @Test
    public void get_arbiters() {
        //given
        //when
        List<String> list = memberService.getArbiters();
        //then
        assertThat(list.get(0), Matchers.containsString("Doe1"));
        assertThat(list, Matchers.hasSize(1));

    }


    @Test
    public void get_members_with_permissions() {
        //given
        //when
        List<Member> list = memberService.getMembersWithPermissions();
        //then
        assertThat(list.get(0).getMemberPermissions(), Matchers.notNullValue());
        assertThat(list, Matchers.hasSize(1));
    }

    @Test
    public void get_members_emails_adult() {
        //given
        boolean adult = true;
        //when
        List<String> list = memberService.getMembersEmails(adult);
        //then
        assertThat(list, Matchers.hasSize(2));
    }

    @Test
    public void get_members_emails_not_adult() {
        //given
        boolean adult = false;
        //when
        List<String> list = memberService.getMembersEmails(adult);
        //then
        assertThat(list, Matchers.hasSize(2));
    }

    @Test
    public void getMembersEmailsNoActive() {
        //given
        //when
        List<String> list = memberService.getMembersEmailsNoActive();
        //then
        assertThat(list, Matchers.hasSize(2));
    }

    @Test
    public void getMembersPhoneNumbersNoActive() {
        //given
        //when
        List<String> list = memberService.getMembersPhoneNumbersNoActive();
        //then
        assertThat(list, Matchers.hasSize(2));
    }

    @Test
    public void getMembersEmailsWithNoPatent() {
        //given
        List<MemberEntity> collect = membersList.stream()
                .filter(f -> !f.getErased())
                .filter(MemberEntity::getAdult)
                .filter(MemberEntity::getActive)
                .filter(f -> f.getShootingPatent() != null)
                .filter(f -> f.getShootingPatent().getPatentNumber() == null).collect(Collectors.toList());
        //when
        List<String> list = memberService.getMembersEmailsAdultActiveWithNoPatent();
        //then
        assertThat(list, Matchers.hasSize(collect.size()));
        assertThat(list.get(0), Matchers.equalTo("sample1@mail.com;"));
    }

    @Test
    public void getMembersPhoneNumbersWithNoPatent() {
        //given
        List<MemberEntity> collect = membersList.stream()
                .filter(f -> !f.getErased())
                .filter(MemberEntity::getAdult)
                .filter(MemberEntity::getActive)
                .filter(f -> f.getShootingPatent() != null)
                .filter(f -> f.getShootingPatent().getPatentNumber() == null).collect(Collectors.toList());
        //when
        List<String> list = memberService.getMembersPhoneNumbersWithNoPatent();
        //then
        assertThat(list, Matchers.hasSize(collect.size()));
        assertThat(list.get(0), Matchers.containsString("+48 111 111 111"));
    }

    @Test
    public void getMembersToEraseEmails() {
        //given
        LocalDate notValidContribution = LocalDate.of(LocalDate.now().getYear(), 12, 31).minusYears(2);
        List<MemberEntity> collect = membersList.stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getActive())
                .filter(f -> f.getHistory().getContributionList().isEmpty() || f.getHistory().getContributionList().get(0).getValidThru().minusDays(1).isBefore(notValidContribution))
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        //when
        List<String> list = memberService.getMembersToEraseEmails();
        //then
        assertThat(list, Matchers.hasSize(collect.size()));
        assertThat(list.get(0), Matchers.equalTo(collect.get(0).getEmail() + ";"));
    }

    @Test
    public void getMembersToErasePhoneNumbers() {
        //given
        LocalDate notValidContribution = LocalDate.of(LocalDate.now().getYear(), 12, 31).minusYears(2);
        List<MemberEntity> collect = membersList.stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getActive())
                .filter(f -> f.getHistory().getContributionList().isEmpty() || f.getHistory().getContributionList().get(0).getValidThru().minusDays(1).isBefore(notValidContribution))
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        //when
        List<String> list = memberService.getMembersToErasePhoneNumbers();
        //then
        assertThat(list, Matchers.hasSize(collect.size()));
        assertThat(list.get(0).replaceAll(" ",""), Matchers.containsString(collect.get(0).getPhoneNumber()));
    }
    @Test
    public void getMembersToPoliceEmails() {
        //given
        LocalDate notValidLicense = LocalDate.now().minusYears(1);
        List<MemberEntity> collect = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(f -> !f.getLicense().isValid())
                .filter(f -> f.getLicense().getValidThru().isBefore(notValidLicense))
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        //when
        List<String> list = memberService.getMembersToPoliceEmails();
        list.forEach(System.out::println);
        //then
        assertThat(list,Matchers.hasSize(collect.size()));
//        assertThat(list.get(0), Matchers.equalTo(collect.get(0).getEmail() + ";"));

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
                .pesel("22222222222").phoneNumber("+48111111111")
                .IDCard("AAA 999991")
                .legitimationNumber(1)
                .adult(true).active(true).erased(false)
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now())
                .club(createClub())
                .memberPermissions(createMemberPermission())
                .shootingPatent(createShootingPatent())
                .build();
        MemberEntity member2 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John2")
                .secondName("Doe2")
                .email("sample2@mail.com")
                .pesel("22222222222").phoneNumber("+48222222222")
                .IDCard("AAA 999992")
                .legitimationNumber(2)
                .adult(false).active(true).erased(false)
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now())
                .club(createClub())
                .shootingPatent(createShootingPatent())
                .build();
        MemberEntity member3 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John3")
                .secondName("Doe3")
                .email("sample3@mail.com")
                .pesel("22222222222").phoneNumber("+48333333333")
                .IDCard("AAA 999993")
                .legitimationNumber(3)
                .adult(true).active(false).erased(false)
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now())
                .club(createClub())
                .shootingPatent(createShootingPatent())
                .build();
        MemberEntity member4 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John4")
                .secondName("Doe4")
                .email("sample4@mail.com")
                .pesel("22222222222").phoneNumber("+48444444444")
                .IDCard("AAA 999994")
                .legitimationNumber(4)
                .adult(false).active(false).erased(false)
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now())
                .club(createClub())
                .shootingPatent(createShootingPatent())
                .build();
        MemberEntity member5 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John5")
                .secondName("Doe5")
                .email("sample5@mail.com")
                .pesel("22222222222").phoneNumber("+48555555555")
                .IDCard("AAA 999995")
                .legitimationNumber(5)
                .adult(true).active(false).erased(true)
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now())
                .club(createClub())
                .shootingPatent(createShootingPatent())
                .build();
        MemberEntity member6 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John6")
                .secondName("Doe6")
                .email("sample6@mail.com")
                .pesel("22222222222").phoneNumber("+48666666666")
                .IDCard("AAA 999996")
                .legitimationNumber(6)
                .adult(false).active(false).erased(true)
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now())
                .club(createClub())
                .shootingPatent(createShootingPatent())
                .build();
        list.add(member1);
        list.add(member2);
        list.add(member3);
        list.add(member4);
        list.add(member5);
        list.add(member6);
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
        if (a % 2 == 0 && i % 2 == 0) {
            contributionEntityList.add(contributionEntity);
        }
        if (i == 0) {
            contributionEntityList.add(contributionEntity);
        }
        i++;
        return HistoryEntity.builder()
                .uuid(historyUUID)
                .competitionHistory(competitionHistoryEntityList)
                .contributionList(contributionEntityList)
                .licensePaymentHistory(licensePaymentHistoryEntityList)
                .build();
    }

    private LicenseEntity createLicense() {

        Random r = new Random();
        int a = r.nextInt(10) + 1;
        if(a%2==0){
            return LicenseEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .number(String.valueOf(Math.round(Math.random())))
                    .validThru(LocalDate.of(LocalDate.now().getYear()-2, 12, 31))
                    .valid(false)
                    .build();
        }

        return LicenseEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .number(String.valueOf(Math.round(Math.random())))
                .validThru(LocalDate.of(LocalDate.now().getYear(), 12, 31))
                .valid(true)
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

    private MemberPermissionsEntity createMemberPermission() {
        Random r = new Random();
        int i = r.nextInt(10000);
        return MemberPermissionsEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .arbiterNumber(String.valueOf(i))
                .arbiterClass(ArbiterClass.CLASS_3.getName())
                .arbiterPermissionValidThru(LocalDate.of(LocalDate.now().getYear() + 4, 12, 31))
                .instructorNumber(null)
                .shootingLeaderNumber(null)
                .build();
    }

    private ShootingPatentEntity createShootingPatent() {
        Random r = new Random();
        int i1 = r.nextInt(10) + 1;
        if (i == 1) {
            return ShootingPatentEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .patentNumber(null)
                    .dateOfPosting(null)
                    .pistolPermission(false)
                    .riflePermission(false)
                    .shotgunPermission(false)
                    .build();
        }
        if (i1 % 2 == 0) {
            int i = r.nextInt(100000);
            String year = String.valueOf(LocalDate.now().getYear());
            String monthValue = String.valueOf(LocalDate.now().getMonthValue());
            return ShootingPatentEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .patentNumber(i + "/PAT/" + monthValue + year)
                    .dateOfPosting(LocalDate.now())
                    .pistolPermission(true)
                    .riflePermission(true)
                    .shotgunPermission(true)
                    .build();
        } else {
            return ShootingPatentEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .patentNumber(null)
                    .dateOfPosting(null)
                    .pistolPermission(false)
                    .riflePermission(false)
                    .shotgunPermission(false)
                    .build();
        }
    }


}