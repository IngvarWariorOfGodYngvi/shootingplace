package com.shootingplace.shootingplace.configurations;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.armory.AmmoUsedService;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class StartConfiguration {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final AmmoUsedService ammoUsedService;
    private final Environment environment;
    private final Logger LOG = LogManager.getLogger(getClass());


    public StartConfiguration(UserRepository userRepository, ClubRepository clubRepository, AmmoUsedService ammoUsedService, Environment environment) {
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.ammoUsedService = ammoUsedService;
        this.environment = environment;
    }

    @Bean
    public void setDateTimeProperty() {
        System.setProperty("dateTime", LocalDateTime.now().toString());
        LOG.info("dateTime property: " + environment.getProperty("dateTime"));
    }

    @Transactional
    @Bean
    public void recountAmmo() {
        ammoUsedService.recountAmmo();
    }

    @Bean
    public void createAdmin() {
        if (!userRepository.existsBySecondName("Admin")) {

            UserEntity admin = UserEntity.builder()
                    .pinCode("9966")
                    .secondName("Admin")
                    .firstName("Admin")
                    .active(true)
                    .otherID(null)
                    .member(null)
                    .changeHistoryEntities(new ArrayList<>())
                    .build();
            List<String> permissions = new ArrayList<>();
            permissions.add(UserSubType.ADMIN.getName());
            permissions.add(UserSubType.SUPER_USER.getName());
            admin.setUserPermissionsList(permissions);
            userRepository.save(admin);
        }
    }

    @Bean
    public void createEmptyClub() {
        if (!clubRepository.existsById(2)) {
            ClubEntity club = ClubEntity.builder()
                    .id(2)
                    .city("")
                    .wzss("")
                    .vovoidership("")
                    .url("")
                    .phoneNumber("")
                    .appartmentNumber("")
                    .houseNumber("")
                    .licenseNumber("")
                    .email("")
                    .fullName("BRAK")
                    .shortName("BRAK")
                    .build();
            clubRepository.save(club);
        }
    }

    @Bean
    public void hashPinForAll() {
        List<UserEntity> all = userRepository.findAll();
        all.forEach(u -> {
            if (u != null) {
                if (u.getPinCode().length() == 4) {
                    String pin = Hashing.sha256().hashString(u.getPinCode(), StandardCharsets.UTF_8).toString();
                    u.setPinCode(pin);
                    userRepository.save(u);
                }
            }
        });
    }

    @Bean
    public void checkIP() {
        InetAddress ip;
        String hostname;
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            System.out.println("Your current IP address : " + ip);
            System.out.println("Your current Hostname : " + hostname);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

}
