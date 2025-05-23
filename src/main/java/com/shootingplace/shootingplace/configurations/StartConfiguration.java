package com.shootingplace.shootingplace.configurations;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class StartConfiguration {

    private final UserRepository userRepository;

    public StartConfiguration(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public void createAdmin() {
        if (!userRepository.existsBySecondName("Admin")){

        UserEntity admin = UserEntity.builder()
                .pinCode("9966")
                .secondName("Admin")
                .firstName("Admin")
                .active(true)
                .otherID(null)
                .member(null)
                .subType("Admin")
                .superUser(true)
                .changeHistoryEntities(new ArrayList<>())
                .build();
        userRepository.save(admin);
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
