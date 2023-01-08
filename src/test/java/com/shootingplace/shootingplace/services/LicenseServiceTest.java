package com.shootingplace.shootingplace.services;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.history.*;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.license.LicenseService;
import com.shootingplace.shootingplace.license.License;
import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.history.LicensePaymentHistoryRepository;
import com.shootingplace.shootingplace.license.LicenseRepository;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentEntity;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class LicenseServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    LicenseRepository licenseRepository;

    @Mock
    HistoryService historyService;

    @Mock
    ChangeHistoryService changeHistoryService;

    @Mock
    LicensePaymentHistoryRepository licensePaymentHistoryRepository;

    private final static LocalDate LOCAL_DATE = LocalDate.of(2021, 11, 2);

    @InjectMocks
    LicenseService licenseService;

    private List<MemberEntity> membersList = getMemberEntities();

    private int i = 1;

    private final String pinCodeOK = "0127";

    @Before
    public void init() {
        when(memberRepository.findAll()).thenReturn(membersList);
        when(memberRepository.findAllByErasedFalse()).thenReturn(membersList.stream().filter(f->!f.getErased()).collect(Collectors.toList()));

        MockitoAnnotations.initMocks(licenseService);

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
        assertThat(license.getNumber(), Matchers.equalTo(null));
    }

    @Test
    public void update_license_no_patent_return_false() {
        //given
        String uuid = membersList.get(1).getUuid();
        License license = License.builder()
                .build();
        //when
        when(memberRepository.findById(uuid)).thenReturn(java.util.Optional.ofNullable(findMemberByID(uuid)));
        ResponseEntity<?> responseEntity = licenseService.updateLicense(uuid, license);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("Brak Patentu"));
    }

    @Test
    public void update_license_licence_exist_return_false() {
        //given
        MemberEntity memberEntity = membersList.get(0);
        String uuid = memberEntity.getUuid();
        License license = License.builder()
                .number(membersList.get(1).getLicense().getNumber())
                .build();
        //when
        when(memberRepository.findById(any(String.class))).thenReturn(java.util.Optional.ofNullable(findMemberByID(uuid)));
        ResponseEntity<?> responseEntity = licenseService.updateLicense(uuid, license);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody(), Matchers.equalTo("Ktoś już ma taki numer licencji"));

    }

    @Test
    public void update_license_licence_not_exist_return_true() {
        //given
        String uuid = membersList.get(0).getUuid();
        boolean t = true;
        License license = License.builder()
                .pistolPermission(t)
                .riflePermission(t)
                .shotgunPermission(t)
                .build();
        //when
        when(memberRepository.findById(any(String.class))).thenReturn(java.util.Optional.ofNullable(findMemberByID(uuid)));
        ResponseEntity<?> responseEntity = licenseService.updateLicense(uuid, license);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
        assertThat(responseEntity.getBody(), Matchers.equalTo("Zaktualizowano licencję"));
    }

//    @Test
//    public void update_license_licence_not_exist_return_true1() {
//        //given
//        String uuid = membersList.get(0).getUuid();
//        boolean t = true;
//        License license = License.builder()
//                .pistolPermission(t)
//                .riflePermission(t)
//                .shotgunPermission(t)
//                .number(String.valueOf(33))
//                .validThru(LocalDate.now().plusDays(1))
//                .build();
//        //when
//        when(memberRepository.findById(any(String.class))).thenReturn(java.util.Optional.ofNullable(findMemberByID(uuid)));
//        ResponseEntity<?> responseEntity = licenseService.updateLicense(uuid, license);
//        //then
//        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
//        assertThat(responseEntity.getBody(), Matchers.equalTo("Zaktualizowano licencję"));
//
//    }

//    @Test
//    public void update_license_return_true() {
//        //given
//        String uuid = membersList.get(0).getUuid();
//        String licenceNumber = String.valueOf(22);
//        LocalDate date = LocalDate.of(2022, 12, 31);
//        //when
//        when(memberRepository.existsById(any(String.class))).thenReturn(existsById(uuid));
//        when(memberRepository.findById(any(String.class))).thenReturn(java.util.Optional.ofNullable(findMemberByID(uuid)));
//        ResponseEntity<?> responseEntity = licenseService.updateLicense(uuid, licenceNumber, date, pinCodeOK);
//        //then
//        assertThat(responseEntity, Matchers.equalTo(HttpStatus.OK));
//
//    }

    @Test
    public void update_license_return_false() {
        //given
        String uuid = membersList.get(0).getUuid();
        String licenceNumber = String.valueOf(3);
        LocalDate date = LocalDate.of(2022, 12, 31);
        //when
        ResponseEntity<?> responseEntity = licenseService.updateLicense(uuid, licenceNumber, date,true, pinCodeOK);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));

    }

    @Test
    public void renew_license_valid_license_no_pais_return_false() {
        //given
        String uuid = membersList.get(0).getUuid();
        boolean t = true;
        License license = License.builder()
                .pistolPermission(t)
                .riflePermission(t)
                .shotgunPermission(t)
                .number(String.valueOf(33))
                .validThru(LocalDate.of(2022, 12, 31))
                .build();
        //when
        when(memberRepository.findById(any(String.class))).thenReturn(java.util.Optional.ofNullable(findMemberByID(uuid)));
        ResponseEntity<?> responseEntity = licenseService.renewLicenseValid(uuid, license);
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
    }

//    @Test
//    public void renew_license_valid_wrong_date_return_false() {
//        //given
//        String uuid = membersList.get(0).getUuid();
//        membersList.get(0).getLicense().setPaid(true);
//        boolean t = true;
//        License license = License.builder()
//                .pistolPermission(t)
//                .riflePermission(t)
//                .shotgunPermission(t)
//                .number(String.valueOf(33))
//                .validThru(LocalDate.of(2022, 12, 31))
//                .build();
//        //when
//        when(memberRepository.findById(any(String.class))).thenReturn(java.util.Optional.ofNullable(findMemberByID(uuid)));
//        ResponseEntity<?> responseEntity = licenseService.renewLicenseValid(uuid, license);
//        //then
//        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
//    }

    @Test
    public void renew_license_valid_good_date() {

        //given
        String uuid = membersList.get(0).getUuid();
        membersList.get(0).getLicense().setPaid(true);
        membersList.get(0).getLicense().setValidThru(LocalDate.of(2021, 12, 31));
        boolean t = true;
        License license = License.builder()
                .pistolPermission(t)
                .riflePermission(t)
                .shotgunPermission(t)
                .number(String.valueOf(33))
                .validThru(LocalDate.now().plusDays(1))
                .build();
        //when
        when(memberRepository.findById(any(String.class))).thenReturn(java.util.Optional.ofNullable(findMemberByID(uuid)));
        ResponseEntity<?> responseEntity = licenseService.renewLicenseValid(uuid, license);
        //then
        Assert.assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));

    }

    @Test
    public void update_license_payment() {
        //given
        MemberEntity memberEntity = membersList.get(0);
        String uuid = memberEntity.getUuid();
        String paymentUUID = memberEntity.getHistory().getLicensePaymentHistory().get(0).getUuid();
        LocalDate now = LocalDate.now();
        //when
        when(memberRepository.findById(any(String.class))).thenReturn(java.util.Optional.ofNullable(findMemberByID(uuid)));
        ResponseEntity<?> responseEntity = licenseService.updateLicensePayment(uuid, paymentUUID, now, 2022, "0125");
        //then
        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
        assertThat(responseEntity.getBody(), Matchers.equalTo("Poprawiono płatność za licencję"));

    }

    private MemberEntity findMemberByID(String uuid) {
        return membersList.stream().filter(f -> f.getUuid().equals(uuid)).findFirst().orElseThrow(EntityNotFoundException::new);
    }

    private List<MemberEntity> getMemberEntities() {

        Faker faker = new Faker(new Locale("pl-PL"));
        Name name = faker.name();

        List<MemberEntity> list = new ArrayList<>();
        MemberEntity member1 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName(name.firstName())
                .secondName(name.lastName())
                .email(faker.internet().emailAddress())
                .pesel("63011744727").phoneNumber("+48111111111")
                .IDCard("AAA 999991")
                .club(createClub())
                .pzss(true)
                .legitimationNumber(1)
                .adult(true).active(true).erased(false)
                .license(createLicense())
                .shootingPatent(createShootingPatent())
                .joinDate(LocalDate.now())
                .history(createHistory())
                .build();
        System.out.println(member1.getFirstName());
        MemberEntity member2 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John2")
                .secondName("Doe2")
                .email("sample2@mail.com")
                .pesel("77100614134").phoneNumber("+48222222222")
                .IDCard("AAA 999992")
                .club(createClub())
                .pzss(true)
                .legitimationNumber(2)
                .adult(true).active(true).erased(false)
                .license(createLicense())
                .shootingPatent(createShootingPatent())
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
                .club(createClub())
                .pzss(true)
                .legitimationNumber(3)
                .adult(true).active(false).erased(false)
                .license(createLicense())
                .shootingPatent(createShootingPatent())
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
                .club(createClub())
                .pzss(true)
                .legitimationNumber(4)
                .adult(false).active(false).erased(false)
                .license(createLicense())
                .shootingPatent(createShootingPatent())
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
                .club(createClub())
                .pzss(true)
                .legitimationNumber(5)
                .adult(true).active(false).erased(true)
                .license(createLicense())
                .shootingPatent(createShootingPatent())
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
                .club(createClub())
                .pzss(true)
                .legitimationNumber(6)
                .adult(false).active(false).erased(true)
                .license(createLicense())
                .shootingPatent(createShootingPatent())
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
                .club(createClub())
                .pzss(true)
                .legitimationNumber(8)
                .adult(false).active(true).erased(false)
                .license(createLicense())
                .shootingPatent(createShootingPatent())
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

    private LicenseEntity createLicense() {

        if (i == 1) {
            return LicenseEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .number(String.valueOf(i))
                    .validThru(LocalDate.of(LocalDate.now().getYear(), 12, 31))
                    .valid(true)
                    .pistolPermission(true)
                    .riflePermission(true)
                    .shotgunPermission(true)
                    .build();
        }
        if (i == 2) {
            return LicenseEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .number(String.valueOf(i))
                    .validThru(LocalDate.of(LocalDate.now().getYear() - 2, 12, 31))
                    .valid(false)
                    .pistolPermission(true)
                    .riflePermission(true)
                    .shotgunPermission(false)
                    .build();
        } else {
            return LicenseEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .number(String.valueOf(i))
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

    private ShootingPatentEntity createShootingPatent() {
        Random r = new Random();
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
        } else {
            int i = r.nextInt(100000);
            String year = String.valueOf(LocalDate.now().getYear());
            String monthValue = String.valueOf(LocalDate.now().getMonthValue());
            ShootingPatentEntity build = ShootingPatentEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .patentNumber(i + "/PAT/" + monthValue + year)
                    .dateOfPosting(LocalDate.now())
                    .pistolPermission(true)
                    .riflePermission(true)
                    .shotgunPermission(false)
                    .build();
            this.i++;
            return build;
        }
    }

    private HistoryEntity createHistory() {
        List<CompetitionHistoryEntity> competitionHistoryEntityList = new ArrayList<>();
        List<ContributionEntity> contributionEntityList = new ArrayList<>();
        List<LicensePaymentHistoryEntity> licensePaymentHistoryEntityList = new ArrayList<>();

        String historyUUID = String.valueOf(UUID.randomUUID());
        LicensePaymentHistoryEntity build = LicensePaymentHistoryEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .memberUUID("")
                .date(LocalDate.now().minusDays(6))
                .validForYear(2021)
                .build();
        if (i == 1) {
            licensePaymentHistoryEntityList.add(build);
        }
        return HistoryEntity.builder()
                .uuid(historyUUID)
                .competitionHistory(competitionHistoryEntityList)
                .contributionList(contributionEntityList)
                .licensePaymentHistory(licensePaymentHistoryEntityList)
                .pistolCounter(0)
                .rifleCounter(0)
                .shotgunCounter(0)
                .build();
    }

}