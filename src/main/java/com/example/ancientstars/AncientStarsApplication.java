package com.example.ancientstars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AncientStarsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AncientStarsApplication.class, args);
    }

}
