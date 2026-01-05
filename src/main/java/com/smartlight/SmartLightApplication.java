package com.smartlight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartLightApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartLightApplication.class, args);
    }
}
