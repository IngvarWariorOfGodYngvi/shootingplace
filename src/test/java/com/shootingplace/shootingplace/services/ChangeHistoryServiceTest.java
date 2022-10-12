package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.history.ChangeHistoryEntity;
import com.shootingplace.shootingplace.history.ChangeHistoryRepository;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class ChangeHistoryServiceTest {
    private int i = 1;
    private List<ChangeHistoryEntity> list = new ArrayList<>();

    @Mock
    ChangeHistoryRepository changeHistoryRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    WorkingTimeEvidenceService workingTimeEvidenceService;
    @InjectMocks
    ChangeHistoryService changeHistoryService;

    @Test
    public void add_record() {
        //given
        UserEntity user = createUserEntity();
        String desc = "Some change";
        String uuid = String.valueOf(UUID.randomUUID());
        ChangeHistoryEntity changeHistoryEntity = ChangeHistoryEntity.builder()
                .userEntity(user)
                .classNamePlusMethod(desc)
                .belongsTo(uuid)
                .dayNow(LocalDate.now())
                .timeNow(String.valueOf(LocalTime.now()))
                .build();
        //when
        when(changeHistoryRepository.save(any(ChangeHistoryEntity.class))).thenReturn(save(changeHistoryEntity));
        ChangeHistoryEntity changeHistoryEntity1 = changeHistoryService.addRecord(user, desc, uuid);
        //then
        assertThat(changeHistoryEntity1.getUserEntity().getFirstName(), Matchers.equalTo("User1"));
        assertThat(changeHistoryEntity1.getBelongsTo(), Matchers.equalTo(uuid));
    }

    @Test
    public void compare_pin_code_return_true() {
        //given
        List<UserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            userEntities.add(createUserEntity());
            this.i++;
        }
        //when
        when(userRepository.findAll()).thenReturn(userEntities);
        boolean b = changeHistoryService.comparePinCode(String.valueOf(userEntities.get(0).getPinCode()));
        //then
        assertThat(b, Matchers.equalTo(true));
    }

    @Test
    public void compare_pin_code_return_false() {
        //given
        List<UserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            userEntities.add(createUserEntity());
            this.i++;
        }
        //when
        when(userRepository.findAll()).thenReturn(userEntities);
        boolean b = changeHistoryService.comparePinCode("0157");
        //then
        assertThat(b, Matchers.equalTo(false));
    }
//
//    @Test
//    public void add_record_to_change_history() {
//        //given
//        String desc = "Some change";
//        String uuid = String.valueOf(UUID.randomUUID());
//        List<UserEntity> userEntities = new ArrayList<>();
//        for (int i = 0; i < 6; i++) {
//            userEntities.add(createUserEntity());
//            this.i++;
//        }
//        ChangeHistoryEntity changeHistoryEntity = ChangeHistoryEntity.builder()
//                .userEntity(userEntities.get(1))
//                .classNamePlusMethod(desc)
//                .belongsTo(uuid)
//                .dayNow(LocalDate.now())
//                .timeNow(String.valueOf(LocalTime.now()))
//                .build();
//        //when
//        when(userRepository.findAll()).thenReturn(userEntities);
//        when(changeHistoryRepository.save(any(ChangeHistoryEntity.class))).thenReturn(save(changeHistoryEntity));
//        changeHistoryService.addRecordToChangeHistory(userEntities.get(1).getPinCode(),desc,uuid);
//        //then
//        assertThat(userEntities.get(1).getList().get(0).getClassNamePlusMethod(), Matchers.equalTo(desc));
//    }

    private ChangeHistoryEntity save(ChangeHistoryEntity changeHistoryEntity) {
        ChangeHistoryEntity changeHistoryEntity1 = ChangeHistoryEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .userEntity(changeHistoryEntity.getUserEntity())
                .classNamePlusMethod(changeHistoryEntity.getClassNamePlusMethod())
                .belongsTo(changeHistoryEntity.getBelongsTo())
                .dayNow(changeHistoryEntity.getDayNow())
                .timeNow(changeHistoryEntity.getTimeNow())
                .build();
        list.add(changeHistoryEntity1);
        return changeHistoryEntity1;

    }

    private UserEntity createUserEntity() {
        Random r = new Random();
        int y = r.nextInt(9000) + 1000;
            boolean superUser;
        if(i==1){
            superUser = true;
        }else {
            superUser = false;
        }

        return UserEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .active(true)
                .firstName("User" + i)
                .changeHistoryEntities(new ArrayList<>())
                .pinCode(String.valueOf(y))
                .superUser(superUser)
                .build();
    }

}