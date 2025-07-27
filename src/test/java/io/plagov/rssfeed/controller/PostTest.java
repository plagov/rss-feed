package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.configuration.ContainersConfig;
import io.plagov.rssfeed.configuration.FakeClockConfiguration;
import io.plagov.rssfeed.domain.request.PostRequest;
import io.plagov.rssfeed.domain.response.PostResponse;
import io.plagov.rssfeed.service.PostService;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;

@Testcontainers
@SpringBootTest(classes = FakeClockConfiguration.class)
@Import(ContainersConfig.class)
@TestPropertySource(properties = "ALLOWED_USER_EMAIL = test@example.com")
@AutoConfigureMockMvc
class PostTest {

    @Autowired
    private PostController postController;

    @Autowired
    private PostService postService;

    @MockitoBean
    private Clock fixedClock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvcTester mockMvc;

    private static final String TEST_TOKEN = "test-token-123";
    private static final String ALLOWED_EMAIL = "test@example.com";

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM posts");
    }

    @Test
    @Sql({"/sql/tokens/add_test_token.sql", "/sql/posts/add_posts.sql"})
    void shouldReturnListOfUnreadPosts() {
        var httpResponse = mockMvc.get()
            .uri("/api/posts/unread")
            .header("X-API-Token", TEST_TOKEN)
            .exchange();
        assertThat(httpResponse).hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(InstanceOfAssertFactories.list(PostResponse.class))
                .hasSize(2);
    }

    @Test
    void shouldAddNewPost() {
        var newPost = new PostRequest(1, "Post name", "http://post.com", LocalDateTime.now(fixedClock));
        postService.saveNewPost(newPost);

        var posts = postController.getAllUnreadPosts();
        assertThat(posts).hasSize(1);
        assertThat(posts).containsExactly(new PostResponse(1,
                1,
                "Post name",
                "http://post.com",
                false,
                LocalDateTime.parse("2023-01-01T12:00")));
    }

    @Test
    @Sql({"/sql/tokens/add_test_token.sql", "/sql/posts/add_posts.sql"})
    void shouldCleanupReadPostsOlderThan30Days() {
        // mark post 1 as read 40 days ago
        when(fixedClock.instant()).thenReturn(Instant.now().minus(40, ChronoUnit.DAYS));
        when(fixedClock.getZone()).thenReturn(ZoneOffset.UTC);

        mockMvc.post().uri("/mark-as-read")
                .param("id", "1")
                .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL)))
                .exchange();

        // mark post 3 as read 10 days ago
        when(fixedClock.instant()).thenReturn(Instant.now().minus(10, ChronoUnit.DAYS));
        when(fixedClock.getZone()).thenReturn(ZoneOffset.UTC);

        mockMvc.post().uri("/mark-as-read")
                .param("id", "3")
                .with(oauth2Login().attributes(attr -> attr.put("email", ALLOWED_EMAIL)))
                .exchange();

        // cleanup read posts older than 30 days
        when(fixedClock.instant()).thenReturn(Instant.now());
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
