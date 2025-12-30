package com.personalfin.server.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
//comment
    @Bean
    public Clock systemClock() {


        System.out.println("Hi hows u");
        return Clock.systemDefaultZone();
    }
}

