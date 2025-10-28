package com.shootingplace.shootingplace.configurations;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.armory.GunUsedRepository;
import com.shootingplace.shootingplace.armory.gunRepresentation.GunRepresentationService;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.email.EmailService;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
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
import java.util.Arrays;
import java.util.List;

@Configuration
public class StartConfiguration {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final Environment environment;
    private final GunRepresentationService gunRepresentationService;
    private final GunUsedRepository gunUsedRepository;

    private final EmailService emailService;

    private final Logger LOG = LogManager.getLogger(getClass());

    public StartConfiguration(UserRepository userRepository, ClubRepository clubRepository, Environment environment, GunRepresentationService gunRepresentationService, GunUsedRepository gunUsedRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.environment = environment;
        this.gunRepresentationService = gunRepresentationService;
        this.gunUsedRepository = gunUsedRepository;
        this.emailService = emailService;
    }

    @Bean
    public void setProperties() {
        System.setProperty("dateTime", LocalDateTime.now().toString());
        String activeProfile = environment.getActiveProfiles()[0];

        ProfilesEnum profile = Arrays.stream(ProfilesEnum.values())
                .filter(p -> p.getName().equalsIgnoreCase(activeProfile))
                .findFirst()
                .orElse(null);

        if (profile != null) {
            switch (profile) {
                case MECHANIK:
                    System.setProperty("sendMail", "false");
                    System.setProperty("shootingPlaceName", "MECHANIKTOMASZÓW");
                    break;
                case PANASZEW:
                    System.setProperty("sendMail", "true");
                    System.setProperty("shootingPlaceName", "RCSPANASZEW");
                    break;
                case DZIESIATKA:
                    System.setProperty("sendMail", "true");
                    System.setProperty("shootingPlaceName", "DZIESIĄTKAŁÓDŹ");
                    break;
                default:
                    System.setProperty("sendMail", "true");
                    System.setProperty("shootingPlaceName", "TEST");
                    break;

            }
        }
        LOG.info("dateTime property: " + environment.getProperty("dateTime"));
        LOG.info("sendMail property: " + environment.getProperty("sendMail"));
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
        try {
            System.out.println("Your current IP address : " + InetAddress.getLocalHost());
            System.out.println("Your current Hostname : " + InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    @Bean
    @Transactional
    public void function() {
        gunUsedRepository.findAll().forEach(gunRepresentationService::function);
    }

}
