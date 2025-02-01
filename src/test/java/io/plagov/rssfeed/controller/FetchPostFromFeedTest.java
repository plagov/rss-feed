package io.plagov.rssfeed.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
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

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void sampleTest() {
        wireMock.stubFor(
                WireMock.get(urlMatching("/feed"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/atom+xml")
                                        .withBody(
                                                """
                                                        <?xml version="1.0" encoding="utf-8"?>
                                                                    <feed xmlns="http://www.w3.org/2005/Atom">
                                                                      <title>Dummy Atom Feed</title>
                                                                      <entry>
                                                                        <title>Dummy Item</title>
                                                                        <id>urn:uuid:dummy-item</id>
                                                                        <updated>2025-02-01T00:00:00Z</updated>
                                                                        <summary>Dummy summary</summary>
                                                                      </entry>
                                                                    </feed>
                                                        """
                                        )
                        )
        );


    }

    // TODO
    // https://testcontainers.com/guides/testing-rest-api-integrations-using-wiremock/#_write_test_for_photo_service_api_integration
    // Call fetch-latest endpoint
    // Verify that the post was added to DB
}
