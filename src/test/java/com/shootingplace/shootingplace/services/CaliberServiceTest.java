package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.CaliberEntity;
import com.shootingplace.shootingplace.domain.models.Caliber;
import com.shootingplace.shootingplace.repositories.CaliberRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class CaliberServiceTest {
    private int i = 1;

    private List<CaliberEntity> caliberEntityList = getCaliberEntities();

    @Mock
    CaliberRepository caliberRepository;

    @InjectMocks
    CaliberService caliberService;

    @Before
    public void init() {
        when(caliberRepository.findAll()).thenReturn(caliberEntityList);
    }

    @After
    public void tearDown() {
        caliberEntityList = getCaliberEntities();
    }

    @Test
    public void get_calibers_List_filled_list() {
        //given
        //when
        List<CaliberEntity> calibersList = caliberService.getCalibersList();
        //then
        assertThat(calibersList.get(0).getName(), Matchers.equalTo("5,6mm"));
        assertThat(calibersList.get(2).getName(), Matchers.equalTo("12/76"));
        assertThat(calibersList.get(3).getName(), Matchers.equalTo(".357"));
    }

    @Test
    public void get_calibers_List_empty_list() {
        //given
        List<CaliberEntity> list = new ArrayList<>();
        CaliberEntity build = CaliberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .name("5,6mm")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        //when
        when(caliberRepository.findAll()).thenReturn(list);
        when(caliberRepository.save(any(CaliberEntity.class))).thenReturn(save(build));
        List<CaliberEntity> calibersList = caliberService.getCalibersList();
        //then
        assertThat(calibersList.get(0).getName(), Matchers.equalTo("5,6mm"));
        assertThat(calibersList.get(1).getName(), Matchers.equalTo("9x19mm"));
    }

    @Test
    public void get_calibers_names_list_filled_list() {
        //given
        CaliberEntity build = CaliberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .name("5,6mm")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        //when
        List<String> calibersList = caliberService.getCalibersNamesList();
        //then
        assertThat(calibersList.get(0), Matchers.equalTo("5,6mm"));
    }

    @Test
    public void get_calibers_names_list_empty_list() {
        //given
        List<CaliberEntity> list = new ArrayList<>();
        CaliberEntity build = CaliberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .name("5,6mm")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        //when
        when(caliberRepository.findAll()).thenReturn(list);
        when(caliberRepository.save(any(CaliberEntity.class))).thenReturn(save(build));
        List<String> calibersList = caliberService.getCalibersNamesList();
        //then
        assertThat(calibersList.get(0), Matchers.equalTo("5,6mm"));
    }

    @Test
    public void create_new_caliber_return_true() {
        //given
        CaliberEntity build = CaliberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .name("newCaliber")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        Caliber build1 = Caliber.builder()
                .name("newCaliber")
                .quantity(0)
                .build();
        //when
//        when(caliberRepository.save(any(CaliberEntity.class))).thenReturn(save(build));
        boolean b = caliberService.createNewCaliber(build1.getName());
        //then
        assertThat(b, Matchers.equalTo(true));
    }

    @Test
    public void create_new_caliber_return_false() {
        //given
        CaliberEntity build = CaliberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .name("5,6mm")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        Caliber build1 = Caliber.builder()
                .name("5,6mm")
                .quantity(0)
                .build();
        //when
//        when(caliberRepository.save(any(CaliberEntity.class))).thenReturn(save(build));
        boolean b = caliberService.createNewCaliber(build1.getName());
        //then
        assertThat(b, Matchers.equalTo(false));
    }

    private CaliberEntity save(CaliberEntity c) {
        String name = c.getName();
        switch (i) {
            case 1:
                name = "5,6mm";
                break;
            case 2:
                name = "9x19mm";
                break;
            case 3:
                name = "7,62x39mm";
                break;
            case 4:
                name = ".38";
                break;
            case 5:
                name = ".357";
                break;
            case 6:
                name = "12/76";
                break;

        }
        CaliberEntity build = CaliberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .name(name)
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        caliberEntityList.add(build);
        return build;
    }

    private List<CaliberEntity> getCaliberEntities() {
        List<CaliberEntity> list = new ArrayList<>();
        CaliberEntity caliberEntity = CaliberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .name("5,6mm")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        list.add(caliberEntity);
        CaliberEntity caliberEntity1 = CaliberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .name("9x19mm")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        list.add(caliberEntity1);
        CaliberEntity caliberEntity2 = CaliberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .name("7,62x39mm")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        list.add(caliberEntity2);
        CaliberEntity caliberEntity3 = CaliberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .name(".38")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        list.add(caliberEntity3);
        CaliberEntity caliberEntity4 = CaliberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .name(".357")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        list.add(caliberEntity4);
        CaliberEntity caliberEntity5 = CaliberEntity.builder()
                .uuid(String.valueOf(UUID.randomUUID()))
                .name("12/76")
                .quantity(0)
                .ammoAdded(null)
                .ammoUsed(null)
                .build();
        list.add(caliberEntity5);
        return list;
    }
}