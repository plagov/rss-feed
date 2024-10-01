package io.plagov.rssfeed.controller;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class FetchPostFromFeedTest {

    @LocalServerPort
    private Integer port;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension
            .newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    // TODO
    // add a blog to DB with URL set to localhost
    // Define a mocked XML response with WireMock
    // Call fetch-latest endpoint
    // Verify that the post was added to DB
}
