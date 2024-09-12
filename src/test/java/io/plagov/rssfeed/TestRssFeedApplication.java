package io.plagov.rssfeed;

import io.plagov.rssfeed.configuration.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestRssFeedApplication {

    public static void main(String[] args) {
        SpringApplication.from(RssFeedApplication::main)
                .with(ContainersConfig.class)
                .run(args);
    }
}
