package com.sggc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

//TODO: Is this needed? Was this required for mocking? If not Look to remove this in future

/**
 * Represents the application's internal clock configuration
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }

}
