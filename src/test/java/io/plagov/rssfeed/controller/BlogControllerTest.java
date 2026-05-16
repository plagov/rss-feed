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
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class BlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("""
                UPDATE blogs
                SET name = 'Of Dollars And Data',
                    feed_url = 'https://ofdollarsanddata.com/feed/',
                    is_subscribed = true,
                    use_ai_filtering = false,
                    user_id = NULL
                WHERE id = 1
                """);
        jdbcTemplate.update("UPDATE blogs SET user_id = NULL");
        jdbcTemplate.update("DELETE FROM blogs WHERE name = 'Test Blog'");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void shouldReturnBlogsForAuthenticatedUser() throws Exception {
        var token = registerLoginAndAssignSeedBlog("blogger");

        mockMvc.perform(get("/api/blogs")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Of Dollars And Data"));
    }

    @Test
    void shouldCreateAndUnsubscribeBlogForAuthenticatedUser() throws Exception {
        var token = registerLoginAndAssignSeedBlog("blogger");

        mockMvc.perform(post("/api/blogs")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test Blog",
                                  "feedUrl": "https://example.com/feed.xml"
                                }
                                """))
                .andExpect(status().isOk());

        var createdBlogId = jdbcTemplate.queryForObject(
                "SELECT id FROM blogs WHERE name = 'Test Blog'",
                Integer.class
        );

        mockMvc.perform(post("/api/blogs/%s/unsubscribe".formatted(createdBlogId))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        var isSubscribed = jdbcTemplate.queryForObject(
                "SELECT is_subscribed FROM blogs WHERE id = ?",
                Boolean.class,
                createdBlogId
        );
        assertThat(isSubscribed).isFalse();
    }

    @Test
    void shouldUpdateBlogForAuthenticatedUser() throws Exception {
        var token = registerLoginAndAssignSeedBlog("blogger");

        mockMvc.perform(put("/api/blogs/1")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Blog",
                                  "feedUrl": "https://example.com/updated-feed.xml",
                                  "isSubscribed": false,
                                  "useAiFiltering": true
                                }
                                """))
                .andExpect(status().isOk());

        var updatedBlog = jdbcTemplate.queryForMap(
                "SELECT name, feed_url, is_subscribed, use_ai_filtering FROM blogs WHERE id = 1"
        );
        assertThat(updatedBlog.get("name")).isEqualTo("Updated Blog");
        assertThat(updatedBlog.get("feed_url")).isEqualTo("https://example.com/updated-feed.xml");
        assertThat(updatedBlog.get("is_subscribed")).isEqualTo(false);
        assertThat(updatedBlog.get("use_ai_filtering")).isEqualTo(true);
    }

    @Test
    void shouldNotUpdateBlogOwnedByAnotherUser() throws Exception {
        registerLoginAndAssignSeedBlog("owner");
        var beforeUpdate = jdbcTemplate.queryForMap(
                "SELECT name, feed_url, is_subscribed, use_ai_filtering FROM blogs WHERE id = 1"
        );

        var attackerToken = registerLogin("attacker");
        mockMvc.perform(put("/api/blogs/1")
                        .header("Authorization", bearer(attackerToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Malicious Update",
                                  "feedUrl": "https://example.com/malicious.xml",
                                  "isSubscribed": false,
                                  "useAiFiltering": true
                                }
                                """))
                .andExpect(status().isOk());

        var afterUpdate = jdbcTemplate.queryForMap(
                "SELECT name, feed_url, is_subscribed, use_ai_filtering FROM blogs WHERE id = 1"
        );
        assertThat(afterUpdate).isEqualTo(beforeUpdate);
    }

    private String registerLoginAndAssignSeedBlog(String username) throws Exception {
        var token = registerLogin(username);
        var userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                UUID.class,
                username
        );
        jdbcTemplate.update("UPDATE blogs SET user_id = ? WHERE id = 1", userId);
        return token;
    }

    private String registerLogin(String username) throws Exception {
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
