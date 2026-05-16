package io.plagov.rssfeed.service;

import io.plagov.rssfeed.configuration.ContainersConfig;
import io.plagov.rssfeed.configuration.FakeClockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Import({ContainersConfig.class, FakeClockConfiguration.class})
@TestPropertySource(properties = {
        "app.jwt.secret=12345678901234567890123456789012",
        "app.jwt.issuer=test-issuer",
        "app.jwt.audience=test-audience",
        "app.jwt.expiration-minutes=60"
})
class PostServiceTest {

    @Container
    WireMockContainer wiremockServer = new WireMockContainer("wiremock/wiremock:3.13.2")
            .withMappingFromResource("feed-three-posts", PostServiceTest.class, "wiremock/mappings/feed-three-posts.json")
            .withMappingFromResource("feed-empty", PostServiceTest.class, "wiremock/mappings/feed-empty.json")
            .withFileFromResource("feed-three-posts.xml", "io/plagov/rssfeed/service/wiremock/__files/feed-three-posts.xml")
            .withFileFromResource("feed-empty.xml", "io/plagov/rssfeed/service/wiremock/__files/feed-empty.xml");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private AiService aiService;

    @MockitoBean
    private ScrapperService scrapperService;

    @Autowired
    private PostService postService;

    private final UUID userId = UUID.randomUUID();
    private int blogId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("DELETE FROM blogs");
        jdbcTemplate.update("DELETE FROM users");

        jdbcTemplate.update("""
                        INSERT INTO users (id, username, password_hash, email, created_at)
                        VALUES (?, ?, ?, ?, NOW())
                        """,
                userId, "testuser", "hash", "test@example.com");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("DELETE FROM blogs");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void shouldNotFetchIfUnreadPostsExist() {
        insertBlog("/feed-three-posts");
        jdbcTemplate.update("""
                        INSERT INTO posts (blog_id, post_name, post_url, is_read, date_added)
                        VALUES (?, ?, ?, ?, NOW())
                        """,
                blogId, "Unread Post", "https://example.com/unread-post", false);

        postService.recordLatestBlogPosts(userId);

        var postsCount = jdbcTemplate.queryForObject("SELECT count(*) FROM posts", Integer.class);
        assertThat(postsCount).isEqualTo(1);
    }

    @Test
    void shouldFetchNewestPostForNewBlog() {
        insertBlog("/feed-three-posts");

        postService.recordLatestBlogPosts(userId);

        assertThat(postNames()).containsExactly("Post 3");
        assertRssServed("/feed-three-posts");
    }

    @Test
    void shouldDoNothingForNewBlogWhenFeedIsEmpty() {
        insertBlog("/feed-empty");

        postService.recordLatestBlogPosts(userId);

        var postsCount = jdbcTemplate.queryForObject("SELECT count(*) FROM posts", Integer.class);
        assertThat(postsCount).isZero();
        assertRssServed("/feed-empty");
    }

    @Test
    void shouldFetchNextPostIfLatestSavedFoundInFeed() {
        insertBlog("/feed-three-posts");
        jdbcTemplate.update("""
                        INSERT INTO posts (blog_id, post_name, post_url, is_read, date_added)
                        VALUES (?, ?, ?, ?, NOW())
                        """,
                blogId, "Post 1", "https://example.com/post-1", true);

        postService.recordLatestBlogPosts(userId);

        assertThat(postNames()).containsExactly("Post 1", "Post 2");
        assertRssServed("/feed-three-posts");
    }

    @Test
    void shouldFallbackToOldestIfLatestSavedNotFoundInFeed() {
        insertBlog("/feed-three-posts");
        jdbcTemplate.update("""
                        INSERT INTO posts (blog_id, post_name, post_url, is_read, date_added)
                        VALUES (?, ?, ?, ?, NOW())
                        """,
                blogId, "Old Post", "https://example.com/old-post", true);

        postService.recordLatestBlogPosts(userId);

        assertThat(postNames()).containsExactly("Old Post", "Post 1");
        assertRssServed("/feed-three-posts");
    }

    @Test
    void shouldTriggerGlobalFetchWhenPostIsMarkedAsRead() throws Exception {
        insertBlog("/feed-three-posts");
        jdbcTemplate.update("""
                        INSERT INTO posts (blog_id, post_name, post_url, is_read, date_added)
                        VALUES (?, ?, ?, ?, NOW())
                        """,
                blogId, "Post 1", "https://example.com/post-1", false);
        var postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE post_name = 'Post 1'", Integer.class);

        postService.markPostAsRead(postId, userId);

        awaitPostNames("Post 1", "Post 2");
        var isRead = jdbcTemplate.queryForObject("SELECT is_read FROM posts WHERE id = ?", Boolean.class, postId);
        assertThat(isRead).isTrue();
        assertRssServed("/feed-three-posts");
    }

    private void insertBlog(String feedPath) {
        jdbcTemplate.update("""
                        INSERT INTO blogs (name, feed_url, is_subscribed, user_id)
                        VALUES (?, ?, ?, ?)
                        """,
                "Test Blog", wiremockServer.getUrl(feedPath), true, userId);
        blogId = jdbcTemplate.queryForObject("SELECT id FROM blogs WHERE name = 'Test Blog'", Integer.class);
    }

    private void assertRssServed(String feedPath) {
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create(wiremockServer.getUrl(feedPath)))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("<rss version=\"2.0\">");
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to call WireMock feed endpoint", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling WireMock feed endpoint", exception);
        }
    }

    private List<String> postNames() {
        return jdbcTemplate.queryForList("SELECT post_name FROM posts ORDER BY id", String.class);
    }

    private void awaitPostNames(String... expectedNames) throws InterruptedException {
        AssertionError lastError = null;
        var deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();

        while (System.nanoTime() < deadline) {
            try {
                assertThat(postNames()).containsExactly(expectedNames);
                return;
            } catch (AssertionError error) {
                lastError = error;
                Thread.sleep(100);
            }
        }

        if (lastError != null) {
            throw lastError;
        }

        throw new AssertionError("Timed out waiting for posts " + Arrays.toString(expectedNames));
    }
}
