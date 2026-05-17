package io.plagov.rssfeed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.plagov.rssfeed.configuration.ContainersConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@Import(ContainersConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=12345678901234567890123456789012",
        "app.jwt.issuer=test-issuer",
        "app.jwt.audience=test-audience",
        "app.jwt.expiration-minutes=60"
})
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("UPDATE blogs SET user_id = NULL");
        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    @Sql("/sql/posts/add_posts.sql")
    void shouldReturnUnreadPostsForAuthenticatedUser() throws Exception {
        var token = registerLoginAndAssignSeedBlog("reader");

        mockMvc.perform(get("/api/posts")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].blogName").value("Of Dollars And Data"))
                .andExpect(jsonPath("$[0].name").value("Post 1"))
                .andExpect(jsonPath("$[0].dateRead").value(nullValue()))
                .andExpect(jsonPath("$[1].name").value("Post 2"));
    }

    @Test
    @Sql("/sql/posts/add_archived_read_posts.sql")
    void shouldReturnArchivedReadPostsForAuthenticatedUser() throws Exception {
        var token = registerLoginAndAssignSeedBlog("reader");
        var userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                UUID.class,
                "reader"
        );
        jdbcTemplate.update("""
                        INSERT INTO blogs (name, feed_url, is_subscribed, user_id, use_ai_filtering)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                "Second Blog", "https://example.com/second-feed", true, userId, false);
        var secondBlogId = jdbcTemplate.queryForObject(
                "SELECT id FROM blogs WHERE name = ?",
                Integer.class,
                "Second Blog"
        );
        jdbcTemplate.update("""
                        INSERT INTO posts (blog_id, post_name, post_url, is_read, is_ignored, ai_reason, date_added, date_read)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                secondBlogId, "Post 4", "https://post4.com", true, true, "Low quality duplicate",
                Timestamp.valueOf("2021-01-04 00:00:00"), Timestamp.valueOf("2021-01-05 00:00:00"));

        mockMvc.perform(get("/api/posts/archive")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].blogName").value("Second Blog"))
                .andExpect(jsonPath("$[0].name").value("Post 4"))
                .andExpect(jsonPath("$[0].url").value("https://post4.com"))
                .andExpect(jsonPath("$[0].isRead").value(true))
                .andExpect(jsonPath("$[0].isIgnored").value(true))
                .andExpect(jsonPath("$[0].aiReason").value("Low quality duplicate"))
                .andExpect(jsonPath("$[0].dateRead").value("2021-01-05T00:00:00"))
                .andExpect(jsonPath("$[1].name").value("Post 2"))
                .andExpect(jsonPath("$[1].url").value("https://post2.com"))
                .andExpect(jsonPath("$[1].isRead").value(true))
                .andExpect(jsonPath("$[1].isIgnored").value(true))
                .andExpect(jsonPath("$[1].aiReason").value("AI flagged for archive"))
                .andExpect(jsonPath("$[1].dateRead").value("2021-01-04T00:00:00"))
                .andExpect(jsonPath("$[2].name").value("Post 1"))
                .andExpect(jsonPath("$[2].isRead").value(true))
                .andExpect(jsonPath("$[2].isIgnored").value(true))
                .andExpect(jsonPath("$[2].aiReason").value("Duplicate content"))
                .andExpect(jsonPath("$[2].dateRead").value("2021-01-02T00:00:00"));
    }

    @Test
    @Sql("/sql/posts/add_posts.sql")
    void shouldMarkPostAsReadForAuthenticatedUser() throws Exception {
        var token = registerLoginAndAssignSeedBlog("reader");
        var postId = jdbcTemplate.queryForObject(
                "SELECT id FROM posts WHERE post_name = 'Post 2'",
                Integer.class
        );

        mockMvc.perform(post("/api/posts/%s/mark-as-read".formatted(postId))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        var isRead = jdbcTemplate.queryForObject(
                "SELECT is_read FROM posts WHERE id = ?",
                Boolean.class,
                postId
        );
        assertThat(isRead).isTrue();
    }

    @Test
    @Sql("/sql/posts/add_read_posts.sql")
    void shouldCleanupReadPostsOlderThan30DaysForAuthenticatedUser() throws Exception {
        var token = registerLoginAndAssignSeedBlog("reader");

        mockMvc.perform(post("/api/posts/cleanup")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        var allPosts = jdbcTemplate.queryForList("SELECT post_name FROM posts ORDER BY post_name");
        assertThat(allPosts)
                .hasSize(2)
                .extracting("post_name")
                .containsExactly("Post 2", "Post 3");
    }

    @Test
    void shouldRejectRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isUnauthorized());
    }

    private String registerLoginAndAssignSeedBlog(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "Password123!",
                                  "email": "%s@example.com"
                                }
                                """.formatted(username, username)))
                .andExpect(status().isCreated());

        var userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                UUID.class,
                username
        );
        jdbcTemplate.update("UPDATE blogs SET user_id = ? WHERE id = 1", userId);

        var loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "Password123!"
                                }
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(loginResponse, Map.class).get("token").toString();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
