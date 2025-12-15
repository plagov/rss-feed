package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.configuration.ContainersConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Import(ContainersConfig.class)
@TestPropertySource(properties = "ALLOWED_USER_EMAIL = test@example.com")
@AutoConfigureMockMvc
class PostTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvcTester mockMvc;

    private static final String TEST_TOKEN = "test-token-123";

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM posts");
    }

    @Test
    @Sql({"/sql/tokens/add_test_token.sql", "/sql/posts/add_read_posts.sql"})
    void shouldCleanupReadPostsOlderThan30Days() {
        // cleanup read posts older than 30 days
        var cleanupExchange = mockMvc.post().uri("/api/posts/cleanup")
                .header("X-API-Token", TEST_TOKEN).exchange();
        assertThat(cleanupExchange).hasStatus(HttpStatus.OK).body().isEmpty();

        // assert unread posts and posts read earlier than 30 days are not deleted
        var allPosts = jdbcTemplate.queryForList("SELECT post_name FROM posts");
        assertThat(allPosts)
                .hasSize(2)
                .extracting("post_name")
                .containsExactlyInAnyOrder("Post 2", "Post 3");
    }
}
