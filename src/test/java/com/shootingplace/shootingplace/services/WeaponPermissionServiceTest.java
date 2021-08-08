package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.entities.WeaponPermissionEntity;
import com.shootingplace.shootingplace.domain.models.WeaponPermission;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import com.shootingplace.shootingplace.repositories.WeaponPermissionRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class WeaponPermissionServiceTest {

    int i = 1;
    List<MemberEntity> membersList = getMemberEntities();

    @Mock
    MemberRepository memberRepository;
    @Mock
    WeaponPermissionRepository weaponPermissionRepository;

    @InjectMocks
    WeaponPermissionService weaponPermissionService;

    @Before
    public void init() {
        when(memberRepository.findAll()).thenReturn(membersList);
    }

    @After
    public void tearDown() {
        membersList = getMemberEntities();
    }

    @Test
    public void update_weapon_permission_return_false() {
        //given
        String uuid = membersList.get(0).getUuid();
        Random r = new Random();
        int i = r.nextInt(100000);
        WeaponPermission weaponPermission = WeaponPermission.builder()
                .number(membersList.get(1).getWeaponPermission().getNumber())
                .isExist(true)
                .admissionToPossessAWeapon("AO" + i)
                .admissionToPossessAWeaponIsExist(true)
                .build();
        //when
        when(memberRepository.findById(uuid)).thenReturn(java.util.Optional.ofNullable(findByID(uuid)));
        boolean b = weaponPermissionService.updateWeaponPermission(uuid, weaponPermission);
        //then
        assertThat(b, Matchers.equalTo(false));
    }

    @Test
    public void update_weapon_permission_return_false1() {
        //given
        String uuid = membersList.get(0).getUuid();
        Random r = new Random();
        int i = r.nextInt(100000);
        WeaponPermission weaponPermission = WeaponPermission.builder()
                .number("AA " + i)
                .isExist(true)
                .admissionToPossessAWeapon(membersList.get(1).getWeaponPermission().getAdmissionToPossessAWeapon())
                .admissionToPossessAWeaponIsExist(true)
                .build();
        //when
        when(memberRepository.findById(uuid)).thenReturn(java.util.Optional.ofNullable(findByID(uuid)));
        boolean b = weaponPermissionService.updateWeaponPermission(uuid, weaponPermission);
        //then
        assertThat(b, Matchers.equalTo(false));
    }

    @Test
    public void update_weapon_permission_return_true() {
        //given
        String uuid = membersList.get(0).getUuid();
        Random r = new Random();
        int i = r.nextInt(100000);
        int y = r.nextInt(100000);
        WeaponPermission weaponPermission = WeaponPermission.builder()
                .number("AA " + i)
                .isExist(true)
                .admissionToPossessAWeapon("AO " + y)
                .admissionToPossessAWeaponIsExist(true)
                .build();
        //when
        when(memberRepository.findById(uuid)).thenReturn(java.util.Optional.ofNullable(findByID(uuid)));
        boolean b = weaponPermissionService.updateWeaponPermission(uuid, weaponPermission);
        //then
        assertThat(b, Matchers.equalTo(true));
    }

    @Test
    public void remove_weapon_permission_return_false() {
        //given
        String uuid = String.valueOf(UUID.randomUUID());
        //when
        when(memberRepository.existsById(uuid)).thenReturn(existsById(uuid));
        boolean b = weaponPermissionService.removeWeaponPermission(uuid,false,false);
        //then
        assertThat(b, Matchers.equalTo(false));
    }

    @Test
    public void remove_weapon_permission_return_true() {
        //given
        String uuid = membersList.get(1).getUuid();
        //when
        when(memberRepository.existsById(uuid)).thenReturn(existsById(uuid));
        when(memberRepository.findById(uuid)).thenReturn(java.util.Optional.ofNullable(findByID(uuid)));
        boolean b = weaponPermissionService.removeWeaponPermission(uuid, true, true);
        //then
        assertThat(b, Matchers.equalTo(true));
    }

    @Test
    public void getWeaponPermission() {
        //given
        //when
        WeaponPermission weaponPermission = weaponPermissionService.getWeaponPermission();
        //then
        assertThat(weaponPermission.getNumber(), Matchers.equalTo(null));
    }

    private MemberEntity findByID(String uuid) {
        return membersList.stream().filter(f -> f.getUuid().equals(uuid)).findFirst().orElseThrow(EntityNotFoundException::new);
    }

    private boolean existsById(String uuid) {
        return membersList.stream().anyMatch(f -> f.getUuid().equals(uuid));
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
                .weaponPermission(createWeaponPermission())
                .build();
    }

    private WeaponPermissionEntity createWeaponPermission() {
        Random r = new Random();
        if (i == 1) {
            WeaponPermissionEntity build = WeaponPermissionEntity.builder()
                    .uuid(String.valueOf(UUID.randomUUID()))
                    .isExist(false)
                    .number(null)
                    .admissionToPossessAWeaponIsExist(false)
                    .admissionToPossessAWeapon(null)
                    .build();
            i++;
            return build;
        }

        int i = r.nextInt(100000);
        int y = r.nextInt(100000);

        return WeaponPermissionEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .isExist(true)
                .number("AA " + i)
                .admissionToPossessAWeapon("AO " + y)
                .admissionToPossessAWeaponIsExist(true)
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