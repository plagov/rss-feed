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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Autowired
    private Clock fixedClock;

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
}
