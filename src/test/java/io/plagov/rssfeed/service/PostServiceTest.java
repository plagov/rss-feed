package io.plagov.rssfeed.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import io.plagov.rssfeed.configuration.ContainersConfig;
import io.plagov.rssfeed.configuration.FakeClockConfiguration;
import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.dao.PostDao;
import io.plagov.rssfeed.domain.Blog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;

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

    @Autowired
    private BlogDao blogDao;

    @Autowired
    private PostDao postDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private PostService postService;

    private final UUID userId = UUID.randomUUID();
    private int blogId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("DELETE FROM blogs");
        jdbcTemplate.update("DELETE FROM users");

        jdbcTemplate.update("INSERT INTO users (id, username, password_hash, created_at) VALUES (?, ?, ?, NOW())",
                userId, "testuser", "hash");
        jdbcTemplate.update("INSERT INTO blogs (name, feed_url, is_subscribed, user_id) VALUES (?, ?, ?, ?)",
                "Test Blog", "http://test.com/feed", true, userId);
        blogId = jdbcTemplate.queryForObject("SELECT id FROM blogs WHERE name = 'Test Blog'", Integer.class);
    }

    @Test
    void shouldNotFetchIfUnreadPostsExist() {
        jdbcTemplate.update("INSERT INTO posts (blog_id, post_name, post_url, is_read, date_added) VALUES (?, ?, ?, ?, NOW())",
                blogId, "Unread Post", "http://unread.com", false);

        postService.recordLatestBlogPosts(userId);

        var postsCount = jdbcTemplate.queryForObject("SELECT count(*) FROM posts", Integer.class);
        assertThat(postsCount).isEqualTo(1);
    }

    @Test
    void shouldFetchOldestPostForNewBlog() {
        var blog = blogDao.getBlogForUser(blogId, userId);
        List<SyndEntry> entries = createEntries("Post 3", "Post 2", "Post 1");
        doReturn(entries).when(postService).getEntriesFromFeed(blog);

        postService.recordLatestBlogPosts(userId);

        var posts = jdbcTemplate.queryForList("SELECT post_name FROM posts", String.class);
        assertThat(posts).containsExactly("Post 1"); // Oldest
    }

    @Test
    void shouldFetchNextPostIfLatestSavedFoundInFeed() {
        jdbcTemplate.update("INSERT INTO posts (blog_id, post_name, post_url, is_read, date_added) VALUES (?, ?, ?, ?, NOW())",
                blogId, "Post 1", "url1", true);

        var blog = blogDao.getBlogForUser(blogId, userId);
        List<SyndEntry> entries = createEntries("Post 3", "Post 2", "Post 1");
        doReturn(entries).when(postService).getEntriesFromFeed(blog);

        postService.recordLatestBlogPosts(userId);

        var posts = jdbcTemplate.queryForList("SELECT post_name FROM posts ORDER BY id", String.class);
        assertThat(posts).containsExactly("Post 1", "Post 2");
    }

    @Test
    void shouldFallbackToOldestIfLatestSavedNotFoundInFeed() {
        jdbcTemplate.update("INSERT INTO posts (blog_id, post_name, post_url, is_read, date_added) VALUES (?, ?, ?, ?, NOW())",
                blogId, "Old Post", "old-url", true);

        var blog = blogDao.getBlogForUser(blogId, userId);
        List<SyndEntry> entries = createEntries("Post 3", "Post 2", "Post 1");
        doReturn(entries).when(postService).getEntriesFromFeed(blog);

        postService.recordLatestBlogPosts(userId);

        var posts = jdbcTemplate.queryForList("SELECT post_name FROM posts ORDER BY id", String.class);
        assertThat(posts).containsExactly("Old Post", "Post 1");
    }

    @Test
    void shouldDoNothingIfLatestSavedIsAlreadyLatestInFeed() {
        jdbcTemplate.update("INSERT INTO posts (blog_id, post_name, post_url, is_read, date_added) VALUES (?, ?, ?, ?, NOW())",
                blogId, "Post 3", "url3", true);

        var blog = blogDao.getBlogForUser(blogId, userId);
        List<SyndEntry> entries = createEntries("Post 3", "Post 2", "Post 1");
        doReturn(entries).when(postService).getEntriesFromFeed(blog);

        postService.recordLatestBlogPosts(userId);

        var posts = jdbcTemplate.queryForList("SELECT post_name FROM posts", String.class);
        assertThat(posts).containsExactly("Post 3");
    }

    @Test
    void shouldTriggerGlobalFetchWhenPostIsMarkedAsRead() throws InterruptedException {
        jdbcTemplate.update("INSERT INTO posts (blog_id, post_name, post_url, is_read, date_added) VALUES (?, ?, ?, ?, NOW())",
                blogId, "Post to read", "url-to-read", false);
        var postId = jdbcTemplate.queryForObject("SELECT id FROM posts WHERE post_name = 'Post to read'", Integer.class);

        postService.markPostAsRead(postId, userId);

        // Wait a bit for the async call
        Thread.sleep(1000);

        verify(postService, atLeastOnce()).recordLatestBlogPosts(userId);
    }

    private List<SyndEntry> createEntries(String... titles) {
        List<SyndEntry> entries = new ArrayList<>();
        int i = titles.length;
        for (String title : titles) {
            SyndEntry entry = new SyndEntryImpl();
            entry.setTitle(title);
            entry.setLink("url" + i--);
            entries.add(entry);
        }
        return entries;
    }
}
