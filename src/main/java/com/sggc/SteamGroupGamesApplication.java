package com.sggc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

@SpringBootApplication
@EnableCaching
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
