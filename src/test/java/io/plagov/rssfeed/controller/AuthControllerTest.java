package io.plagov.rssfeed.controller;

import com.fasterxml.jackson.databind.JsonNode;
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
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("UPDATE blogs SET user_id = NULL");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void shouldRegisterAndLoginUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "Password123!",
                                  "email": "test@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        String token = responseJson.get("token").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void shouldReturnUnauthorizedForMeWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowSecondRegistrationForDifferentUsername() throws Exception {
        var request = """
                {
                  "username": "admin",
                  "password": "Password123!",
                  "email": "test@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "another-admin",
                                  "password": "Password123!",
                                  "email": "test2@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("another-admin"));
    }

    @Test
    void shouldRejectDuplicateUsername() throws Exception {
        var request = """
                {
                  "username": "admin",
                  "password": "Password123!",
                  "email": "test@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }
}
