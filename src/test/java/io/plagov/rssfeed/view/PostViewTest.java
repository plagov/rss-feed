package io.plagov.rssfeed.view;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
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

    @Test
    @Sql("/sql/posts/add_posts.sql")
    void canViewCorrectNumberOfColumns() {
        page.navigate("http://localhost:" + port);
        var columnHeadings = page.locator("table thead tr th").allTextContents();
        Assertions.assertThat(columnHeadings).containsExactly("ID", "Title", "Date added");
    }

    @Test
    @Sql("/sql/posts/add_posts.sql")
    void titleColumnContainsLinks() {
        page.navigate("http://localhost:" + port);
        var postTitleCell = page.locator("tbody > tr:nth-child(1) > td:nth-child(2) > a");
        PlaywrightAssertions.assertThat(postTitleCell).hasAttribute("href", "post1.com");
    }
}
