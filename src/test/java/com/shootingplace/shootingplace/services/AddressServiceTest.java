package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.AddressEntity;
import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.models.Address;
import com.shootingplace.shootingplace.repositories.AddressRepository;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class AddressServiceTest {

    @Mock
    AddressRepository addressRepository;
    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    AddressService addressService;

    @Test
    public void update_address_success() {
        //given
        MemberEntity memberEntity = getMember();
        System.out.println(memberEntity.getUuid());
        String uuid = memberEntity.getUuid();
        Address address = getBuild();
        System.out.println(address.toString());
        //when
        when(memberRepository.findById(uuid)).thenReturn(java.util.Optional.of(memberEntity));
        System.out.println(uuid);
        boolean b = addressService.updateAddress(uuid, address);
        //then
        assertThat(b, Matchers.equalTo(true));
    }

    @Test
    public void getAddress() {
        //given
        //when
        Address address = addressService.getAddress();
        //then
        assertThat(address.getFlatNumber(), Matchers.equalTo(null));
    }

    private Address getBuild() {
        return Address.builder()
                .postOfficeCity("AngelCity")
                .zipCode("33-333")
                .street("AngelStreet")
                .streetNumber("33")
                .flatNumber("3")
                .build();
    }

    private MemberEntity getMember() {
        return MemberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .firstName("John1")
                .secondName("Doe1")
                .email("sample1@mail.com")
                .pesel("63011744727").phoneNumber("+48111111111")
                .IDCard("AAA 999991")
                .legitimationNumber(1)
                .adult(true).active(true).erased(false)
                .address(createAddress())
                .build();
    }

    private AddressEntity createAddress() {
        return AddressEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .postOfficeCity("SinCity")
                .zipCode("00-000")
                .street("SinStreet")
                .streetNumber("66")
                .flatNumber("6")
                .build();
    }
}