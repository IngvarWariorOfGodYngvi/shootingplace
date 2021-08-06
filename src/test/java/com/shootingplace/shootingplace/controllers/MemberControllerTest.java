package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.models.Member;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MemberControllerTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberController memberController;

    @Before
    public void init(){
        when(memberRepository.findAll()).thenReturn(namesList());
    }


    private final String pinCodeNotOK = "1578";
    private final String pinCodeOK = "0127";

    private Member memberNotOK = Member.builder()
            .firstName("John")
            .secondName("Doe")
            .email("sample@mail.com")
            .pesel("22222222222").phoneNumber("+48987654321").active(true)
            .build();
    private Member memberOK = Member.builder()
            .firstName("John")
            .secondName("Doe")
            .email("sample@mail.com")
            .pesel("22222222222").phoneNumber("+48987654321").active(true)
            .IDCard("AAA 999999")
            .build();


    @Test
    public void getMemberListTest() {

    }


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

    private List<MemberEntity> namesList() {
        List<MemberEntity> list = new ArrayList<>();
        MemberEntity member1 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John1")
                .secondName("Doe1")
                .email("sample1@mail.com")
                .pesel("90120510813").phoneNumber("+48111111111").active(true)
                .IDCard("AAA 999991")
                .build();
        MemberEntity member2 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John2")
                .secondName("Doe2")
                .email("sample2@mail.com")
                .pesel("22222222222").phoneNumber("+48222222222").active(true)
                .IDCard("AAA 999992")
                .build();
        MemberEntity member3 = MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John3")
                .secondName("Doe3")
                .email("sample3@mail.com")
                .pesel("22222222222").phoneNumber("+48333333333").active(true)
                .IDCard("AAA 999993")
                .build();
        list.add(member1);
        list.add(member2);
        list.add(member3);
        return list;
    }

}
