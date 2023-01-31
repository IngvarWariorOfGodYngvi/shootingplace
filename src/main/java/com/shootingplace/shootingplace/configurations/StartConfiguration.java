package com.shootingplace.shootingplace.configurations;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class StartConfiguration {

    private final UserRepository userRepository;

    public StartConfiguration(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
@Bean
    public void hashPinForAll() {
        List<UserEntity> all = userRepository.findAll();
        all.forEach(u->{
            if(u.getPinCode().length()==4){
            String pin = Hashing.sha256().hashString(u.getPinCode(), StandardCharsets.UTF_8).toString();
            u.setPinCode(pin);
            userRepository.save(u);
            }
        });
    }

}
