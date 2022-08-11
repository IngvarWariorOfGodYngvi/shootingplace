package com.shootingplace.shootingplace.controllers;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import com.shootingplace.shootingplace.member.MemberController;
import com.shootingplace.shootingplace.member.MemberEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class MemberControllerTest {

//    @Mock
//    MemberRepository memberRepository;

    @InjectMocks
    MemberController memberController;

    private final List<MemberEntity> membersList = getMemberEntities();

//    @Before
//    public void init() {
//        when(memberRepository.findAll()).thenReturn(membersList);
//
//        MockitoAnnotations.initMocks(memberController);
//
//    }
//
//    @After
//    public void tearDown() {
//        membersList = getMemberEntities();
//    }


    private final String pinCodeNotOK = "1578";
    private final String pinCodeOK = "0127";

    @Test
    public void doNothing() {
    }

//    @Test
//    public void getMember_return_OK() {
//        int number = 1;
//        ResponseEntity<?> responseEntity = memberController.getMember(number);
//
//
//
//        Assert.assertThat(responseEntity.getStatusCode(), Matchers.equalTo(ResponseEntity.ok()));
//        Assert.assertThat(responseEntity.getBody(), Matchers.equalTo(membersList.get(0)));
//
//    }


//
//    @Test
//    public void getMemberListTest() {
//        //given
//        when(memberController.getAllNames()).thenReturn(namesList());
//        //when
//        List<String> membersNames = memberController.getAllNames();
//        //then
//        MatcherAssert.assertThat(membersNames, Matchers.hasSize(2));
//
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void add_Member_test1() {
//        //given
//        MemberController memberController = mock(MemberController.class);
//        when(memberController.addMember(memberNotOK, pinCodeOK)).thenThrow(IllegalArgumentException.class);
//
//        //when
//        ResponseEntity<?> responseEntity = memberController.addMember(memberNotOK, pinCodeOK);
//        //then
//        Assert.assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.CONFLICT));
//
//    }
//
//    @Test
//    public void add_Member_tes2() {
//        //given
//        MemberController memberController = mock(MemberController.class);
//        when(memberController.addMember(memberOK, pinCodeOK)).thenReturn(ResponseEntity.status(201).build());
//        //when
//        ResponseEntity<?> responseEntity = memberController.addMember(memberOK, pinCodeOK);
//        //then
//        Assert.assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.CREATED));
//    }
//
//    @Test
//    public void add_Member_tes3() {
//        //given
//        MemberController memberController = mock(MemberController.class);
//        when(memberController.addMember(memberOK, pinCodeNotOK)).thenReturn(ResponseEntity.status(403).build());
//        //when
//        ResponseEntity<?> responseEntity = memberController.addMember(memberOK, pinCodeNotOK);
//        //then
//        Assert.assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.FORBIDDEN));
//    }


//    @Test
//    public void updateMemberTest() {
//        Member updatedMember = Member.builder()
//                .firstName("Jasko")
//                .build();
//        testRestTemplate.put(String.format("/memberEntity/%s", uuid), updatedMember);
//        System.out.println(uuid);
//        memberEntity = memberRepository.findById(uuid).orElseGet(MemberEntity::new);
//        assertEquals(updatedMember.getFirstName(), memberEntity.getFirstName());
//        Member updatedMember2 = Member.builder()
//                .secondName("Pie")
//                .build();
//        testRestTemplate.put(String.format("/memberEntity/%s", uuid), updatedMember2);
//        System.out.println(uuid);
//        memberEntity = memberRepository.findById(uuid).orElseGet(MemberEntity::new);
//        assertEquals(updatedMember2.getSecondName(), memberEntity.getSecondName());
//
//    }
//
//    @Test
//    public void failUpdateMemberTest() {
//        Member updatedMember = Member.builder()
//                .firstName("Jasko")
//                .build();
//
//    }
//
//    @Test
//    public void addNewMemberAndUpdateHimTest() {
//        MemberEntity memberEntity2 = MemberEntity.builder()
//                .firstName("Janusz")
//                .secondName("Piekutowski")
//                .email("cośta@mail.com")
//                .pesel("22222222222")
//                .build();
//        memberEntity2 = memberRepository.saveAndFlush(memberEntity2);
//        String uuid2 = memberEntity2.getUuid();
//
//        Member updatedMember = Member.builder()
//                .firstName("Jasko")
//                .build();
//        Member updatedMember2 = Member.builder()
//                .firstName("DRZWI KURWA")
//                .build();
//        testRestTemplate.put(String.format("/memberEntity/%s", uuid), updatedMember);
//        System.out.println(uuid);
//        testRestTemplate.put(String.format("/memberEntity/%s", uuid2), updatedMember2);
//        System.out.println(uuid2);
//        memberEntity = memberRepository.findById(uuid).orElseGet(MemberEntity::new);
//        memberEntity2 = memberRepository.findById(uuid2).orElseGet(MemberEntity::new);
//        assertEquals(updatedMember.getFirstName(), memberEntity.getFirstName());
//        assertEquals(updatedMember2.getFirstName(), memberEntity2.getFirstName());
//
//
//    }

//    private List<MemberEntity> namesList() {
//        List<MemberEntity> list = new ArrayList<>();
//        MemberEntity member1 = MemberEntity.builder()
//                .uuid(String.valueOf(UUID.randomUUID()))
//                .pesel("90120510813")
//                .active(true)
//                .IDCard("AAA 999991")
//                .build();
//        member1.setFirstName("John1");
//        member1.setSecondName("Doe1");
//        member1.setEmail("sample1@mail.com");
//        member1.setPhoneNumber("+48111111111");
//        MemberEntity member2 = MemberEntity.builder()
//                .uuid(String.valueOf(UUID.randomUUID()))
//                .pesel("22222222222").active(true)
//                .IDCard("AAA 999992")
//                .build();
//        member2.setFirstName("John2");
//        member2.setSecondName("Doe2");
//        member2.setEmail("sample2@mail.com");
//        member2.setPhoneNumber("+48111111112");
//        MemberEntity member3 = MemberEntity.builder()
//                .uuid(String.valueOf(UUID.randomUUID()))
//                .pesel("22222222222").active(true)
//                .IDCard("AAA 999993")
//                .build();
//        member3.setFirstName("John3");
//        member3.setSecondName("Doe3");
//        member3.setEmail("sample3@mail.com");
//        member3.setPhoneNumber("+48111111113");
//        list.add(member1);
//        list.add(member2);
//        list.add(member3);
//        return list;
//    }

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
//                .club(createClub())
                .pzss(true)
                .legitimationNumber(1)
                .adult(true).active(true).erased(false)
//                .license(createLicense())
//                .shootingPatent(createShootingPatent())
                .joinDate(LocalDate.now())
//                .history(createHistory())
                .build();
        MemberEntity member2 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John2")
                .secondName("Doe2")
                .email("sample2@mail.com")
                .pesel("77100614134").phoneNumber("+48222222222")
                .IDCard("AAA 999992")
//                .club(createClub())
                .pzss(true)
                .legitimationNumber(2)
                .adult(true).active(true).erased(false)
//                .license(createLicense())
//                .shootingPatent(createShootingPatent())
                .joinDate(LocalDate.now())
//                .history(createHistory())
                .build();
//        MemberEntity member3 = MemberEntity.builder()
//                .uuid(String.valueOf(UUID.randomUUID()))
//                .firstName("John3")
//                .secondName("Doe3")
//                .email("sample3@mail.com")
//                .pesel("84102078413").phoneNumber("+48333333333")
//                .IDCard("AAA 999993")
//                .club(createClub())
//                .pzss(true)
//                .legitimationNumber(3)
//                .adult(true).active(false).erased(false)
//                .license(createLicense())
//                .shootingPatent(createShootingPatent())
//                .joinDate(LocalDate.now())
//                .history(createHistory())
//                .build();
//        MemberEntity member4 = MemberEntity.builder()
//                .uuid(String.valueOf(UUID.randomUUID()))
//                .firstName("John4")
//                .secondName("Doe4")
//                .email("sample4@mail.com")
//                .pesel("87111483627").phoneNumber("+48444444444")
//                .IDCard("AAA 999994")
//                .club(createClub())
//                .pzss(true)
//                .legitimationNumber(4)
//                .adult(false).active(false).erased(false)
//                .license(createLicense())
//                .shootingPatent(createShootingPatent())
//                .joinDate(LocalDate.now())
//                .history(createHistory())
//                .build();
//        MemberEntity member5 = MemberEntity.builder()
//                .uuid(String.valueOf(UUID.randomUUID()))
//                .firstName("John5")
//                .secondName("Doe5")
//                .email("sample5@mail.com")
//                .pesel("79082032935").phoneNumber("+48555555555")
//                .IDCard("AAA 999995")
//                .club(createClub())
//                .pzss(true)
//                .legitimationNumber(5)
//                .adult(true).active(false).erased(true)
//                .license(createLicense())
//                .shootingPatent(createShootingPatent())
//                .joinDate(LocalDate.now())
//                .history(createHistory())
//                .build();
//        MemberEntity member6 = MemberEntity.builder()
//                .uuid(String.valueOf(UUID.randomUUID()))
//                .firstName("John6")
//                .secondName("Doe6")
//                .email("sample6@mail.com")
//                .pesel("90031875364").phoneNumber("+48666666666")
//                .IDCard("AAA 999996")
//                .club(createClub())
//                .pzss(true)
//                .legitimationNumber(6)
//                .adult(false).active(false).erased(true)
//                .license(createLicense())
//                .shootingPatent(createShootingPatent())
//                .joinDate(LocalDate.now())
//                .history(createHistory())
//                .build();
//        MemberEntity member8 = MemberEntity.builder()
//                .uuid(String.valueOf(UUID.randomUUID()))
//                .firstName("John8")
//                .secondName("Doe8")
//                .email("sample8@mail.com")
//                .pesel("72070186261").phoneNumber("+48888888888")
//                .IDCard("AAA 999998")
//                .club(createClub())
//                .pzss(true)
//                .legitimationNumber(8)
//                .adult(false).active(true).erased(false)
//                .license(createLicense())
//                .shootingPatent(createShootingPatent())
//                .joinDate(LocalDate.now().minusYears(3))
//                .history(createHistory())
//                .build();
        list.add(member1);
        list.add(member2);
//        list.add(member3);
//        list.add(member4);
//        list.add(member5);
//        list.add(member6);
//        list.add(member8);
        return list;
    }

    private boolean existsById(String uuid) {
        return membersList.stream().anyMatch(f -> f.getUuid().equals(uuid));
    }

    private MemberEntity findMemberByID(String uuid) {
        return membersList.stream().filter(f -> f.getUuid().equals(uuid)).findFirst().orElseThrow(EntityNotFoundException::new);
    }

    private ResponseEntity<?> response(Integer number) {
        return ResponseEntity.ok(membersList.stream().filter(f -> f.getLegitimationNumber().equals(number)));
    }

}
