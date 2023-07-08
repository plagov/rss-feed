package io.plagov.rssfeedtonotion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RssFeedToNotionApplication {

    public static void main(String[] args) {
        SpringApplication.run(RssFeedToNotionApplication.class, args);
    }
}
