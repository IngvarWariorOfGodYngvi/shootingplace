package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.ClubEntity;
import com.shootingplace.shootingplace.domain.models.Club;
import com.shootingplace.shootingplace.repositories.ClubRepository;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class ClubServiceTest {

    private int i = 1;
    private final int clubCount = 5;
    private List<ClubEntity> clubEntityList = getClubEntities();


    @Mock
    ClubRepository clubRepository;

    @InjectMocks
    ClubService clubService;

    @Before
    public void init() {
        when(clubRepository.findAll()).thenReturn(clubEntityList);
        i = 1;
    }

    @After
    public void tearDown() {
        clubEntityList = getClubEntities();
    }

    @Test
    public void get_all_clubs() {
        //given
        //when
        List<ClubEntity> allClubs = clubService.getAllClubs();
        allClubs.forEach(System.out::println);
        //then
        assertThat(allClubs, Matchers.hasSize(clubCount));
    }

    @Test
    public void get_all_clubs_to_tournament() {
        //given
        //when
        List<String> allClubs = clubService.getAllClubsToTournament();
        //then
        assertThat(allClubs, Matchers.hasSize(clubCount - 2));
        assertThat(allClubs.get(0), Matchers.equalTo(clubEntityList.get(2).getName()));
    }

    @Test
    public void update_club_return_false1() {
        //given
        int id = 7;
        Club club = Club.builder()
                .name("Changed Name")
                .fullName("Changed Name in Some City")
                .email("")
                .build();
        //when
        when(clubRepository.existsById(id)).thenReturn((existsById(id)));
        boolean b = clubService.updateClub(id, club);
        //then
        assertThat(b, Matchers.equalTo(false));
    }

    @Test
    public void update_club_return_false2() {
        //given
        int id = 2;
        Club club = Club.builder()
                .name("Changed Name")
                .fullName("Changed Name in Some City")
                .email("")
                .build();
        //when
        when(clubRepository.existsById(id)).thenReturn((existsById(id)));
        boolean b = clubService.updateClub(id, club);
        //then
        assertThat(b, Matchers.equalTo(false));
    }

    @Test
    public void update_club_return_true() {
        //given
        Random r = new Random();
        int y = r.nextInt(10000);
        int id = 1;
        Club club = Club.builder()
                .name("Changed Name")
                .fullName("Changed Name in Some City")
                .email("changedEmail@mail.com")
                .address("changed address")
                .url("www.changedsite.com")
                .phoneNumber("+48999999999")
                .licenseNumber(y + "/2021")
                .build();
        //when
        when(clubRepository.existsById(id)).thenReturn((existsById(id)));
        when(clubRepository.findById(id)).thenReturn(java.util.Optional.ofNullable(findById(id)));
        boolean b = clubService.updateClub(id, club);
        //then
        assertThat(b, Matchers.equalTo(true));
    }

    @Test
    public void create_mother_Club_return_false() {
        //given
        Random r = new Random();
        int y = r.nextInt(10000);
        int id = 1;
        Club club = Club.builder()
                .name("MotherClub Name")
                .fullName("MotherClub Name in Some City")
                .email("MotherClubEmail@mail.com")
                .address("MotherClub address")
                .url("www.MotherClub.com")
                .phoneNumber("+48999999999")
                .licenseNumber(y + "/2021")
                .build();
        //when
        when(clubRepository.findById(id)).thenReturn(java.util.Optional.ofNullable((findById(id))));
        boolean b = clubService.createMotherClub(club);
        //then
        assertThat(b, Matchers.equalTo(false));
    }

    @Test
    public void create_mother_Club_return_false1() {
        //given
        Random r = new Random();
        int y = r.nextInt(10000);
        int id = 1;
        Club club = Club.builder()
                .name("")
                .fullName("MotherClub Name in Some City")
                .email("MotherClubEmail@mail.com")
                .address("MotherClub address")
                .url("www.MotherClub.com")
                .phoneNumber("+48999999999")
                .licenseNumber(y + "/2021")
                .build();
        //when
        when(clubRepository.findById(id)).thenReturn(java.util.Optional.ofNullable(null));
        boolean b = clubService.createMotherClub(club);
        //then
        assertThat(b, Matchers.equalTo(false));
    }

    @Test
    public void create_mother_Club_return_true() {
        //given
        Random r = new Random();
        int y = r.nextInt(10000);
        int id = 1;
        Club club = Club.builder()
                .name("MotherClub Name")
                .fullName("MotherClub Name in Some City")
                .email("MotherClubEmail@mail.com")
                .address("MotherClub address")
                .url("www.MotherClub.com")
                .phoneNumber("+48999999999")
                .licenseNumber(y + "/2021")
                .build();
        //when
        when(clubRepository.findById(id)).thenReturn(java.util.Optional.ofNullable(null));
        boolean b = clubService.createMotherClub(club);
        //then
        assertThat(b, Matchers.equalTo(true));
    }

    private ClubEntity findById(int id) {
        return clubEntityList.stream().filter(f -> f.getId().equals(id)).findFirst().orElseThrow(EntityExistsException::new);
    }

    private boolean existsById(int id) {
        return clubEntityList.stream().anyMatch(f -> f.getId().equals(id));
    }

    private List<ClubEntity> getClubEntities() {
        List<ClubEntity> list = new ArrayList<>();
        for (int i = 0; i < clubCount; i++) {
            ClubEntity clubEntity = createClub();
            list.add(clubEntity);
        }
        return list;

    }

    private ClubEntity createClub() {
        if (i == 2) {
            ClubEntity brak = ClubEntity.builder()
                    .id(i)
                    .name("BRAK")
                    .fullName(null)
                    .licenseNumber(null)
                    .build();
            i++;
            return brak;
        }
        Random r = new Random();
        int y = r.nextInt(10000);

        return ClubEntity.builder()
                .id(i)
                .name("Some Club" + i)
                .email("sample" + i + "@mail.com")
                .address("Some address" + i)
                .url("www.someSite" + i + ".com")
                .phoneNumber("+4811111111" + i)
                .fullName("Some Club in Some City" + i++)
                .licenseNumber(y + "/2021")
                .build();
    }
}