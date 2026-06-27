package com.narrativewatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NarrativewatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(NarrativewatchApplication.class, args);
    }

}
