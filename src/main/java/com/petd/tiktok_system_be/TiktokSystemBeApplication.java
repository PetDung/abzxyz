package com.petd.tiktok_system_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class TiktokSystemBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiktokSystemBeApplication.class, args);
    }

}
