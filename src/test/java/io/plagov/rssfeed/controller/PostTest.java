package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.configuration.FakeClockConfiguration;
import io.plagov.rssfeed.domain.request.PostRequest;
import io.plagov.rssfeed.domain.response.PostResponse;
import io.plagov.rssfeed.service.PostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(classes = FakeClockConfiguration.class)
class PostTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.1"));

    @Autowired
    private PostController postController;

    @Autowired
    private PostService postService;

    @Autowired
    private Clock fixedClock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM posts");
    }

    @Test
    @Sql("/sql/posts/add_posts.sql")
    void shouldReturnListOfUnreadPosts() {
        var allUnreadPosts = postController.getAllUnreadPosts();
        assertThat(allUnreadPosts).hasSize(2);
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
    @Sql("/sql/posts/add_posts.sql")
    void shouldMarkPostAsRead() {
        postController.markPostAsRead(2);

        var unreadPosts = postController.getAllUnreadPosts();
        assertThat(unreadPosts).hasSize(1);
    }
}
