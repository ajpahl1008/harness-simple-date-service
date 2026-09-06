package com.bespin.dateservice.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Supplies the {@link Clock} the service reads the current date from. Injecting a clock rather
 * than calling {@code LocalDate.now()} directly keeps the date deterministic in tests.
 */
@Configuration
public class ClockConfig {

    /**
     * Provides a system UTC clock bean for dependency injection.
     *
     * @return a Clock instance using the system time in UTC
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
