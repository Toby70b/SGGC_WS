package com.sggc;

import com.sggc.models.Game;
import com.sggc.repositories.GameRepository;
import com.sggc.services.GameService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.util.List;
import java.util.Set;

@SpringBootApplication
@Configuration
public class SteamGroupGamesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteamGroupGamesApplication.class, args);
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
