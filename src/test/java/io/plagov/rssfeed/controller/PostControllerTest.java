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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
                .andExpect(jsonPath("$[0].name").value("Post 1"))
                .andExpect(jsonPath("$[1].name").value("Post 2"));
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
