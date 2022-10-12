package com.shootingplace.shootingplace.configurations;

import com.shootingplace.shootingplace.repositories.ClubRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class StartConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    private final ClubRepository clubRepository;

    public StartConfiguration(ClubRepository clubRepository) {


        this.clubRepository = clubRepository;
    }

}
