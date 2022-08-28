package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.history.HistoryEntity;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.license.LicenseService;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermissionService;
import com.shootingplace.shootingplace.address.AddressService;
import com.shootingplace.shootingplace.domain.entities.*;
import com.shootingplace.shootingplace.domain.enums.ArbiterClass;
import com.shootingplace.shootingplace.domain.enums.ErasedType;
import com.shootingplace.shootingplace.member.*;
import com.shootingplace.shootingplace.repositories.ClubRepository;
import com.shootingplace.shootingplace.repositories.ErasedRepository;
import com.shootingplace.shootingplace.repositories.LicensePaymentHistoryRepository;
import com.shootingplace.shootingplace.license.LicenseRepository;
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
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;
    @Mock
    LicenseRepository licenseRepository;
    @Mock
    HistoryService historyService;
    @Mock
    ChangeHistoryService changeHistoryService;
    @Mock
    ErasedRepository erasedRepository;
    @Mock
    AddressService addressService;
    @Mock
    ClubRepository clubRepository;
    @Mock
    ShootingPatentService shootingPatentService;
    @Mock
    LicenseService licenseService;
    @Mock
    WeaponPermissionService weaponPermissionService;
    @Mock
    MemberPermissionsService memberPermissionsService;
    @Mock
    PersonalEvidenceService personalEvidenceService;
    @Mock
    ContributionService contributionService;
    @Mock
    LicensePaymentHistoryRepository licensePaymentHistoryRepository;


    @InjectMocks
    MemberService memberService;

    private List<MemberEntity> membersList = getMemberEntities();

    private int i = 0;

    private final String pinCodeOK = "0127";

    @Before
    public void init() {
//        when(memberRepository.findAll(Sort.by("secondaName"))).thenReturn(membersList);
        when(memberRepository.findAll()).thenReturn(membersList);
    }

    @After
    public void tearDown() {
        membersList = getMemberEntities();
    }

    @Test
    public void get_Member() {
        //given
        int number = 1;
        //when
        when(memberRepository.findByLegitimationNumber(any(Integer.class))).thenReturn(Optional.ofNullable(findByLegitimationNumber(number)));
        when(memberRepository.existsByLegitimationNumber(any(Integer.class))).thenReturn(existsByLegitimationNumber(number));
        ResponseEntity<?> responseEntity = memberService.getMember(number);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
        assertThat(responseEntity.getBody(), Matchers.equalTo(membersList.get(0)));
//        assertThat(member.getEmail(), Matchers.equalTo("sample1@mail.com"));
    }

    @Test
    public void get_Member_Erased() {
        //when
        List<Member> members = memberService.getMembersErased();
        //then
        assertEquals(members.get(0).getSecondName(), membersList.get(4).getSecondName());
        assertThat(members, Matchers.hasSize(2));
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
        assertThat(list, Matchers.hasSize(3));
        MemberEntity memberEntity = membersList.get(1);
        assertThat(list, Matchers.hasItem(memberEntity.getEmail() + ";"));
    }

    @Test
    public void get_Member_By_UUID() {
        //given
        String uuid = membersList.get(0).getUuid();
        //when
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        MemberEntity memberByUUID = memberService.getMemberByUUID(uuid);
        //then
        assertThat(memberByUUID, Matchers.equalTo(memberByUUID));
    }

    @Test
    public void get_All_Names() {
        //given
        //when
        List<String> allNames = memberService.getAllNames();
        //then
        assertThat(allNames, Matchers.hasSize(5));

    }

//    @Test
//    public void get_All_MemberDTO() {
//        //given
//        //when
//        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO();
//        List<MemberDTO> memberDTOS = new ArrayList<>();
//        membersList.stream().filter(f -> !f.getErased()).forEach(e ->
//                memberDTOS.add(Mapping.map2DTO(e))
//        );
//        memberDTOS.sort(Comparator.comparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName));
//        //then
//        assertThat(allMemberDTO, Matchers.hasSize(5));
//        assertEquals(allMemberDTO.get(0).getSecondName(), memberDTOS.get(0).getSecondName());

//    }

    @Test
    public void get_All_MemberDTO_with_args_all_active() {
        //given
        Boolean adult = null;
        Boolean active = true;
        Boolean erased = false;
        //when
        List<MemberDTO> allMemberDTO = memberService.getAllMemberDTO(adult, active, erased);
        //then
        assertThat(allMemberDTO, Matchers.hasSize(3));
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
        assertThat(allMemberDTO, Matchers.hasSize(5));
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
        assertThat(allMemberDTO, Matchers.hasSize(3));
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
        assertThat(allMemberDTO, Matchers.hasSize(2));
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


//    @Test
//    public void get_members_quantity() {
//        //given
//        //when
//        List<Long> list = memberService.getMembersQuantity();
//        //then
//        assertThat(list.get(0), Matchers.equalTo(2L));
//        assertThat(list.get(1), Matchers.equalTo(2L));
//        assertThat(list.get(2), Matchers.equalTo(3L));
//        assertThat(list.get(3), Matchers.equalTo(3L));
//        assertThat(list.get(4), Matchers.equalTo(2L));
//        assertThat(list.get(5), Matchers.equalTo(1L));
//        assertThat(list.get(6), Matchers.equalTo(1L));
//    }

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
        assertThat(list, Matchers.hasSize(3));
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
        assertThat(list.get(0).replaceAll(" ", ""), Matchers.containsString(collect.get(0).getPhoneNumber()));
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
        //then
        assertThat(list, Matchers.hasSize(collect.size()));
        assertThat(list.get(0), Matchers.equalTo(collect.get(0).getEmail() + ";"));

    }

    @Test
    public void getMembersToPolicePhoneNumbers() {
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
        List<String> list = memberService.getMembersToPolicePhoneNumbers();
        //then
        assertThat(list, Matchers.hasSize(collect.size()));
        assertThat(list.get(0).replaceAll(" ", ""), Matchers.containsString(collect.get(0).getPhoneNumber()));
    }

    @Test
    public void get_members_phone_numbers_adult() {
        //given
        boolean adult = true;
        //when
        List<String> list = memberService.getMembersPhoneNumbers(adult);
        //then
        assertThat(list, Matchers.hasSize(2));
    }

    @Test
    public void get_members_phone_numbers_not_adult() {
        //given
        boolean adult = false;
        //when
        List<String> list = memberService.getMembersPhoneNumbers(adult);
        //then
        assertThat(list, Matchers.hasSize(3));
    }


    @Test
    public void get_member_pesel_is_present_success() {
        //given
        String pesel = membersList.get(0).getPesel();
        //when
        when(memberRepository.findByPesel(pesel)).thenReturn(Optional.ofNullable(findByPesel(pesel)));
        Boolean memberPeselIsPresent = memberService.getMemberPeselIsPresent(pesel);
        //then
        assertThat(memberPeselIsPresent, Matchers.equalTo(true));
    }

    @Test
    public void get_member_pesel_is_present_fail() {
        //given
        String pesel = membersList.get(1).getPesel();
        //when
        Boolean memberPeselIsPresent = memberService.getMemberPeselIsPresent(pesel);
        //then
        assertThat(memberPeselIsPresent, Matchers.equalTo(false));
    }

    @Test
    public void get_member_IDCard_present_success() {
        //given
        String idCard = membersList.get(0).getIDCard();
        //when
        when(memberRepository.findByIDCard(idCard)).thenReturn(Optional.ofNullable(findByIDCard(idCard)));
        Boolean memberIDCardPresent = memberService.getMemberIDCardPresent(idCard);
        //then
        assertThat(memberIDCardPresent, Matchers.equalTo(true));
    }

    @Test
    public void get_member_IDCard_present_fail() {
        //given
        String idCard = membersList.get(1).getIDCard();
        //when
        Boolean memberIDCardPresent = memberService.getMemberIDCardPresent(idCard);
        //then
        assertThat(memberIDCardPresent, Matchers.equalTo(false));
    }

    @Test
    public void get_member_email_present_success() {
        //given
        String email = membersList.get(0).getEmail();
        //when
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(findByEmail(email)));
        Boolean memberEmailPresent = memberService.getMemberEmailPresent(email);
        //then
        assertThat(memberEmailPresent, Matchers.equalTo(true));
    }

    @Test
    public void get_member_email_present_fail() {
        //given
        String email = membersList.get(1).getEmail();
        //when
        Boolean memberEmailPresent = memberService.getMemberEmailPresent(email);
        //then
        assertThat(memberEmailPresent, Matchers.equalTo(false));
    }

    @Test
    public void getErasedType() {
        //given
        //when
        List<String> erasedType = memberService.getErasedType();
        //then
        assertThat(erasedType.get(0), Matchers.equalTo(ErasedType.RESIGNATION.getName()));
        assertThat(erasedType.get(1), Matchers.equalTo(ErasedType.CHANGE_BELONGING.getName()));
        assertThat(erasedType.get(2), Matchers.equalTo(ErasedType.CLUB_DECISION.getName()));
        assertThat(erasedType.get(3), Matchers.equalTo(ErasedType.OTHER.getName()));
    }

    @Test
    public void add_new_member_bad_request_pesel_exist() {
        //given
        Member member = Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .pesel("63011744727")
                .email("sample7@mail.com")
                .IDCard("AAA 999997")
                .joinDate(LocalDate.now())
                .adult(true)
                .build();
        //when
        when(memberRepository.findByPesel(member.getPesel())).thenReturn(Optional.ofNullable(findByPesel(member.getPesel())));
        ResponseEntity<?> responseEntity = memberService.addNewMember(member, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("\"Uwaga! Ktoś już ma taki numer PESEL\""));
    }

    @Test
    public void add_new_member_bad_request_email_exist() {
        //given
        Member member = Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .pesel("50052116592")
                .email("sample1@mail.com")
                .IDCard("AAA 999996")
                .joinDate(LocalDate.now())
                .adult(true)
                .build();
        //when
        ResponseEntity<?> responseEntity = memberService.addNewMember(member, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("\"Uwaga! Ktoś już ma taki e-mail\" " + member.getEmail()));
    }

    @Test
    public void add_new_member_bad_request_idCard_exist() {
        //given
        Member member = Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .pesel("50052116592")
                .email("sample7@mail.com")
                .IDCard("AAA 999991")
                .joinDate(LocalDate.now())
                .adult(true)
                .build();
        //when
        ResponseEntity<?> responseEntity = memberService.addNewMember(member, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("\"Uwaga! Ktoś już ma taki numer dowodu osobistego\""));
    }

    @Test
    public void add_new_member_bad_request_legitimation_number_exist_in_erased_member() {
        //given
        Member member = Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .pesel("50052116592")
                .email("sample7@mail.com")
                .IDCard("AAA 999999")
                .joinDate(LocalDate.now())
                .adult(true)
                .legitimationNumber(6)
                .build();
        //when
        when(memberRepository.findByLegitimationNumber(member.getLegitimationNumber())).thenReturn(Optional.ofNullable(findByLegitimationNumber(member.getLegitimationNumber())));
        ResponseEntity<?> responseEntity = memberService.addNewMember(member, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("\"Uwaga! Ktoś wśród skreślonych już ma taki numer legitymacji\""));
    }

    @Test
    public void add_new_member_bad_request_legitimation_number_exist() {
        //given
        Member member = Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .pesel("50052116592")
                .email("sample7@mail.com")
                .IDCard("AAA 999999")
                .joinDate(LocalDate.now())
                .adult(true)
                .legitimationNumber(1)
                .build();
        //when
        when(memberRepository.findByLegitimationNumber(member.getLegitimationNumber())).thenReturn(Optional.ofNullable(findByLegitimationNumber(member.getLegitimationNumber())));
        ResponseEntity<?> responseEntity = memberService.addNewMember(member, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("\"Uwaga! Ktoś już ma taki numer legitymacji\""));
    }

    @Test
    public void update_member_member_not_found() {
        //given
        Member member = Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .pesel("50052116592")
                .email("sample7@mail.com")
                .IDCard("AAA 999991")
                .joinDate(LocalDate.now())
                .adult(true)
                .legitimationNumber(1)
                .build();
        //when
        ResponseEntity<?> responseEntity = memberService.updateMember(String.valueOf(UUID.randomUUID()), member);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));

    }

    @Test
    public void update_member_bad_request_pesel_exist() {
        //given
        String pesel = "63011744727";
        String uuid = membersList.get(1).getUuid();
        Member member = Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .pesel("63011744727")
                .email("sample7@mail.com")
                .IDCard("AAA 999991")
                .joinDate(LocalDate.now())
                .adult(true)
                .legitimationNumber(1)
                .build();
        //when
        when(memberRepository.findByPesel(pesel)).thenReturn(Optional.ofNullable(findByPesel(pesel)));
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        ResponseEntity<?> responseEntity = memberService.updateMember(uuid, member);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("Już ktoś ma taki sam numer PESEL"));
    }

    @Test
    public void update_member_bad_request_email_exist() {
        //given
        String email = "sample1@mail.com";
        String uuid = membersList.get(1).getUuid();
        Member member = Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .pesel("63011744727")
                .email("sample1@mail.com")
                .IDCard("AAA 999991")
                .joinDate(LocalDate.now())
                .adult(true)
                .legitimationNumber(1)
                .build();
        //when
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of((findByEmail(email))));
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        ResponseEntity<?> responseEntity = memberService.updateMember(uuid, member);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("Uwaga! Już ktoś ma taki sam e-mail"));
    }

    @Test
    public void update_member_bad_request_idCard_exist() {
        //given
        String idCard = "AAA 999992";
        String uuid = membersList.get(0).getUuid();
        Member member = Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .pesel("63011744727")
                .email("sample7@mail.com")
                .IDCard("AAA 999992")
                .joinDate(LocalDate.now())
                .adult(true)
                .legitimationNumber(1)
                .build();
        //when
        when(memberRepository.findByIDCard(idCard)).thenReturn(Optional.of((findByIDCard(idCard))));
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        ResponseEntity<?> responseEntity = memberService.updateMember(uuid, member);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("Ktoś już ma taki numer dowodu"));
    }

    @Test
    public void update_member_bad_request_legitimation_number_exist() {
        //given
        Integer legitimationNumber = 1;
        String uuid = membersList.get(0).getUuid();
        Member member = Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .pesel("63011744727")
                .email("sample7@mail.com")
                .IDCard("AAA 999992")
                .joinDate(LocalDate.now())
                .adult(true)
                .legitimationNumber(1)
                .build();
        //when
        when(memberRepository.findByLegitimationNumber(legitimationNumber)).thenReturn(Optional.of((findByLegitimationNumber(legitimationNumber))));
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        ResponseEntity<?> responseEntity = memberService.updateMember(uuid, member);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("\"Uwaga! Już ktoś ma taki numer legitymacji\""));
    }

    @Test
    public void update_member_success() {
        //given
        String uuid = membersList.get(0).getUuid();
        Member member = Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .pesel("63011744727")
                .email("sample7@mail.com")
                .IDCard("AAA 999997")
                .joinDate(LocalDate.now())
                .adult(true)
                .legitimationNumber(7)
                .build();
        //when
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        ResponseEntity<?> responseEntity = memberService.updateMember(uuid, member);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
    }

    @Test
    public void update_member_success2() {
        //given
        String uuid = membersList.get(1).getUuid();
        Member member = Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .pesel("63011744727")
                .email("sample7@mail.com")
                .IDCard("AAA 999997")
                .phoneNumber("111111111")
                .joinDate(LocalDate.now())
                .adult(true)
                .legitimationNumber(7)
                .build();
        //when
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        ResponseEntity<?> responseEntity = memberService.updateMember(uuid, member);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
    }

    @Test
    public void change_adult_not_found() {
        //given
        String uuid = String.valueOf(UUID.randomUUID());
        //when
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        ResponseEntity<?> responseEntity = memberService.changeAdult(uuid, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void change_adult_bad_request_member_already_adult() {
        //given
        String uuid = membersList.get(0).getUuid();
        //when
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        ResponseEntity<?> responseEntity = memberService.changeAdult(uuid, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("\"Klubowicz należy już do grupy powszechnej\""));
    }

    @Test
    public void change_adult_bad_request_too_short_probation() {
        //given
        String uuid = membersList.get(1).getUuid();
        //when
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        ResponseEntity<?> responseEntity = memberService.changeAdult(uuid, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("\"Klubowicz ma za krótki staż jako młodzież\""));
    }

    @Test
    public void change_adult_success() {
        //given
        String uuid = membersList.get(6).getUuid();
        //when
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        ResponseEntity<?> responseEntity = memberService.changeAdult(uuid, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
    }

//    @Test
//    public void change_pzss_return_OK() {
//        //given
//        String uuid = membersList.get(6).getUuid();
//        //when
//        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
//        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
//        ResponseEntity<?> responseEntity = memberService.changePzss(uuid);
//        //then
//        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
//    }

    @Test
    public void change_pzss_return_bad_request() {
        //given
        String uuid = String.valueOf(UUID.randomUUID());
        //when
        when(memberRepository.existsById(uuid)).thenReturn(existsById(uuid));
        ResponseEntity<?> responseEntity = memberService.changePzss(uuid);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("Nie znaleziono Klubowicza"));
    }

    @Test
    public void activate_or_deactivate_member_not_found() {
        //given
        String uuid = String.valueOf(UUID.randomUUID());
        //when
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        ResponseEntity<?> responseEntity = memberService.activateOrDeactivateMember(uuid, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void activate_or_deactivate_success() {
        //given
        String uuid = membersList.get(6).getUuid();
        //when
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        ResponseEntity<?> responseEntity = memberService.activateOrDeactivateMember(uuid, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
    }

    @Test
    public void erase_member_bad_request() {
        //given
        String uuid = String.valueOf(UUID.randomUUID());
        String erasedType = ErasedType.CLUB_DECISION.getName();
        LocalDate date = LocalDate.now();
        String description = "description";
        //when
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        ResponseEntity<?> responseEntity = memberService.eraseMember(uuid, erasedType, date, description, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void erase_member_success() {
        //given
        String uuid = membersList.get(6).getUuid();
        String erasedType = ErasedType.CLUB_DECISION.getName();
        LocalDate date = LocalDate.now();
        String description = "description";
        //when
        when(memberRepository.existsById(uuid)).thenReturn((existsById(uuid)));
        when(memberRepository.findById(uuid)).thenReturn(Optional.ofNullable(findByID(uuid)));
        ResponseEntity<?> responseEntity = memberService.eraseMember(uuid, erasedType, date, description, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
    }

    @Test
    public void add_new_member_adult_success() {
        //given
        Member member = getMember();
        MemberEntity memberEntity = MemberEntity.builder()
                .firstName(member.getFirstName())
                .secondName(member.getSecondName())
                .pesel(member.getPesel())
                .IDCard(member.getIDCard())
                .phoneNumber(member.getPhoneNumber())
                .email(member.getEmail())
                .joinDate(member.getJoinDate())
                .adult(member.getAdult())
                .build();
        //when
        when(memberRepository.findByPesel(member.getPesel())).thenReturn(Optional.ofNullable(findByPesel(member.getPesel())));
        when(memberRepository.save(any(MemberEntity.class))).thenReturn(save((memberEntity)));
        when(clubRepository.findById(1)).thenReturn(Optional.ofNullable(createClub()));
        ResponseEntity<?> responseEntity = memberService.addNewMember(member, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.CREATED));
        assertThat(responseEntity.getBody(), Matchers.equalTo("\"" + memberEntity.getUuid() + "\""));
    }

    @Test
    public void add_new_member_non_adult_success() {
        //given
        Member member = getMember();
        member.setAdult(false);
        member.setLegitimationNumber(9);
        member.setJoinDate(null);
        member.setEmail(null);
        MemberEntity memberEntity = MemberEntity.builder()
                .firstName(member.getFirstName())
                .secondName(member.getSecondName())
                .pesel(member.getPesel())
                .IDCard(member.getIDCard())
                .phoneNumber(member.getPhoneNumber())
                .adult(member.getAdult())
                .build();
        //when
        when(memberRepository.findByPesel(member.getPesel())).thenReturn(Optional.ofNullable(findByPesel(member.getPesel())));
        when(memberRepository.save(any(MemberEntity.class))).thenReturn(save((memberEntity)));
        when(clubRepository.findById(1)).thenReturn(Optional.ofNullable(createClub()));
        ResponseEntity<?> responseEntity = memberService.addNewMember(member, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.CREATED));
        assertThat(responseEntity.getBody(), Matchers.equalTo("\"" + memberEntity.getUuid() + "\""));
    }

    @Test
    public void add_new_member_to_empty_list_success() {
        //given
        Member member = getMember();
        member.setAdult(false);
        member.setJoinDate(null);
        member.setEmail(null);
        MemberEntity memberEntity = MemberEntity.builder()
                .firstName(member.getFirstName())
                .secondName(member.getSecondName())
                .pesel(member.getPesel())
                .IDCard(member.getIDCard())
                .phoneNumber(member.getPhoneNumber())
                .adult(member.getAdult())
                .build();
        //when
        when(memberRepository.findAll()).thenReturn(new ArrayList<>());
        when(memberRepository.findByPesel(member.getPesel())).thenReturn(Optional.ofNullable(findByPesel(member.getPesel())));
        when(memberRepository.save(any(MemberEntity.class))).thenReturn(save((memberEntity)));
        when(clubRepository.findById(1)).thenReturn(Optional.ofNullable(createClub()));
        ResponseEntity<?> responseEntity = memberService.addNewMember(member, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.CREATED));
        assertThat(responseEntity.getBody(), Matchers.equalTo("\"" + memberEntity.getUuid() + "\""));
    }

    private List<Member> erasedList() {
        return membersList.stream().filter(MemberEntity::getErased).map(Mapping::map).collect(Collectors.toList());
    }

    private MemberEntity findByID(String uuid) {
        return membersList.stream().filter(f -> f.getUuid().equals(uuid)).findFirst().orElseThrow(EntityNotFoundException::new);
    }

    private MemberEntity findByPesel(String pesel) {
        return membersList.stream().filter(f -> f.getPesel().equals(pesel)).findFirst().orElse(null);
    }

    private MemberEntity findByIDCard(String idCard) {
        return membersList.stream().filter(f -> f.getIDCard().equals(idCard)).findFirst().orElseThrow(EntityNotFoundException::new);
    }

    private MemberEntity findByEmail(String email) {
        return membersList.stream().filter(f -> f.getEmail().equals(email)).findFirst().orElseThrow(EntityNotFoundException::new);
    }

    private List<MemberEntity> findAllByErasedIsTrue() {
        return membersList.stream().filter(MemberEntity::getErased).collect(Collectors.toList());
    }

    private boolean existsById(String uuid) {
        return membersList.stream().anyMatch(f -> f.getUuid().equals(uuid));
    }

    private boolean existsByLegitimationNumber(Integer number) {
        return membersList.stream().anyMatch(f -> f.getLegitimationNumber().equals(number));
    }

    private MemberEntity findByLegitimationNumber(Integer legitimationNumber) {
        return membersList.stream().filter(f -> f.getLegitimationNumber().equals(legitimationNumber)).findFirst().orElseThrow(EntityNotFoundException::new);
    }

    private MemberEntity findByPhoneNumber(String phoneNumber) {
        return membersList.stream().filter(f -> f.getPhoneNumber().equals(phoneNumber)).findFirst().orElseThrow(EntityNotFoundException::new);
    }

    private MemberEntity save(MemberEntity memberEntity) {
        MemberEntity entity = MemberEntity.builder()
                .firstName(memberEntity.getFirstName())
                .secondName(memberEntity.getSecondName())
                .email(memberEntity.getEmail())
                .pesel(memberEntity.getPesel()).phoneNumber(memberEntity.getPhoneNumber())
                .IDCard(memberEntity.getIDCard())
                .legitimationNumber(memberEntity.getLegitimationNumber())
                .adult(memberEntity.getAdult()).active(memberEntity.getActive()).erased(memberEntity.getErased())
                .history(memberEntity.getHistory())
                .license(memberEntity.getLicense())
                .joinDate(memberEntity.getJoinDate())
                .club(memberEntity.getClub())
                .shootingPatent(memberEntity.getShootingPatent())
                .build();
        return entity;
    }

    private Member getMember() {
        return Member.builder()
                .firstName("John7")
                .secondName("Doe7")
                .email("sample7@mail.com")
                .pesel("95070787164")
                .phoneNumber("+48777777777")
                .IDCard("AAA 999997")
                .adult(true)
                .joinDate(LocalDate.now())
                .build();
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
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now())
                .club(createClub())
                .memberPermissions(createMemberPermission())
                .shootingPatent(createShootingPatent())
                .pzss(true)
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
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now())
                .club(createClub())
                .shootingPatent(createShootingPatent())
                .pzss(true)
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
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now())
                .club(createClub())
                .shootingPatent(createShootingPatent())
                .pzss(true)
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
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now())
                .club(createClub())
                .shootingPatent(createShootingPatent())
                .pzss(true)
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
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now())
                .club(createClub())
                .shootingPatent(createShootingPatent())
                .pzss(true)
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
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now())
                .club(createClub())
                .shootingPatent(createShootingPatent())
                .pzss(true)
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
                .history(createHistory())
                .license(createLicense())
                .joinDate(LocalDate.now().minusYears(3))
                .club(createClub())
                .shootingPatent(createShootingPatent())
                .pzss(true)
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
        if (i == 3) {
            return LicenseEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .number(String.valueOf(Math.round(Math.random())))
                    .validThru(LocalDate.of(LocalDate.now().getYear() - 2, 12, 31))
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