package com.mat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Military Asset Tracker (MAT) Spring Boot application.
 *
 * @SpringBootApplication enables:
 *   - @Configuration:     marks this class as a source of bean definitions
 *   - @EnableAutoConfiguration: tells Spring Boot to auto-configure based on classpath
 *   - @ComponentScan:     scans this package and sub-packages for Spring components
 */
@SpringBootApplication
public class MatApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatApplication.class, args);
    }
}
