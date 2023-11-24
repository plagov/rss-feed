package io.plagov.rssfeed.view;

import io.plagov.rssfeed.E2eBaseTest;
import io.plagov.rssfeed.configuration.ContainersConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Import(ContainersConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostViewTest extends E2eBaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SuppressWarnings("'Delete' statement without 'where' clears all data in the table")
    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM posts");
        jdbcTemplate.execute("DELETE FROM blogs");
    }

    @Test
    @Sql("/sql/posts/add_posts.sql")
    void canViewTableOfUnreadPosts() {
        page.navigate("http://localhost:" + port);
        var posts = page.querySelectorAll("table tbody tr");
        Assertions.assertThat(posts).hasSize(2);
    }
}
