package com.bespin.dateservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Simple Date Service.
 * Provides a REST API that returns the current date in ISO-8601 format.
 */
@SpringBootApplication
public class SimpleDateServiceApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SimpleDateServiceApplication.class, args);
    }
}
