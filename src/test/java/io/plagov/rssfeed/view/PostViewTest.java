package io.plagov.rssfeed.view;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.junit.UsePlaywright;
import io.plagov.rssfeed.configuration.ContainersConfig;
import org.assertj.core.api.Assertions;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Import(ContainersConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FlywayTest
@UsePlaywright
@ActiveProfiles(value = "uitest")
class PostViewTest {

    @LocalServerPort
    private int port;

    @Test
    @Sql("/sql/posts/add_posts.sql")
    void canViewTableOfUnreadPosts(Page page) {
        page.navigate("http://localhost:" + port);
        var posts = page.querySelectorAll("table tbody tr");
        Assertions.assertThat(posts).hasSize(2);
    }

    @Test
    @Sql("/sql/posts/add_posts.sql")
    void canViewCorrectNumberOfColumns(Page page) {
        page.navigate("http://localhost:" + port);
        var columnHeadings = page.locator("table thead tr th").allTextContents();
        Assertions.assertThat(columnHeadings).containsExactly("ID", "Title", "Date added", "Mark as read");
    }

    @Test
    @Sql("/sql/posts/add_posts.sql")
    void titleColumnContainsLinks(Page page) {
        page.navigate("http://localhost:" + port);
        var postTitleCell = page.locator("tbody > tr:nth-child(1) > td:nth-child(2) > a");
        PlaywrightAssertions.assertThat(postTitleCell).hasAttribute("href", "https://post1.com");
    }

    @Test
    @Sql("/sql/posts/add_posts.sql")
    void userCanMarkPostAsRead(Page page) {
        page.navigate("http://localhost:" + port);
        page.locator("tr[data-testid='post-2'] button").click();
        var remainingRow = page.locator("tr[data-testid='post-1'] td:nth-child(2)");
        PlaywrightAssertions.assertThat(remainingRow).hasText("Post 1");
    }
}
