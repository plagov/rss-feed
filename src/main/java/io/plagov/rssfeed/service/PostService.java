package io.plagov.rssfeed.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.dao.PostDao;
import io.plagov.rssfeed.domain.Blog;
import io.plagov.rssfeed.domain.request.PostRequest;
import io.plagov.rssfeed.domain.response.PostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class PostService {

    private final BlogDao blogDao;
    private final PostDao postDao;
    private final Clock clock;
    private final Logger logger = LoggerFactory.getLogger(PostService.class);

    public PostService(BlogDao blogDao, PostDao postDao, Clock clock) {
        this.blogDao = blogDao;
        this.postDao = postDao;
        this.clock = clock;
    }

    public void recordLatestBlogPosts() {
        logger.info("Evaluate all blogs");
        var allBlogs = blogDao.getBlogs(true);
        allBlogs.forEach(this::recordLatestForBlog);
        logger.info("Finish evaluating blogs");
    }

    public void recordLatestPostsForBlog(int blogId) {
        var blog = blogDao.getBlog(blogId);
        recordLatestForBlog(blog);
        logger.info("Finish evaluating blog {}", blog.name());
    }

    private void recordLatestForBlog(Blog blog) {
        logger.info("Evaluate blog {}", blog.name());
        var latestSavedPost = postDao.getLatestPostForBlog(blog.name());
        var allEntries = getEntriesFromFeed(blog);

        if (latestSavedPost.isEmpty()) {
            recordFirstEntryForBlog(blog, allEntries);
        } else if (!latestSavedPostIsLatestInFeed(latestSavedPost.get(), allEntries)) {
            saveNewPostsFromFeed(blog, latestSavedPost.get(), allEntries);
        }
    }

    private void saveNewPostsFromFeed(Blog blog, PostResponse latestSavedPost, List<SyndEntry> allEntries) {
        var postIndex = getIndexOfLatestSavedPostInFeed(latestSavedPost, allEntries);
        logger.info("Save latest post for blog {}", blog.name());
        for (var i = postIndex - 1; i >= 0; i--) {
            var entry = allEntries.get(i);
            var post = new PostRequest(blog.id(), entry.getTitle(), entry.getLink(), LocalDateTime.now(clock));
            saveNewPost(post);
        }
    }

    private boolean latestSavedPostIsLatestInFeed(PostResponse latestSavedPost, List<SyndEntry> allEntries) {
        var index = getIndexOfLatestSavedPostInFeed(latestSavedPost, allEntries);
        return index == 0;
    }

    private int getIndexOfLatestSavedPostInFeed(PostResponse latestSavedPost, List<SyndEntry> entriesFromFeed) {
        return IntStream.range(0, entriesFromFeed.size())
                .filter(i -> entriesFromFeed.get(i).getLink().equals(latestSavedPost.url()))
                .findFirst()
                .orElseThrow();
    }

    private void recordFirstEntryForBlog(Blog blog, List<SyndEntry> allEntries) {
        var latestEntryFromFeed = allEntries.getFirst();
        logger.info("No saved posts in database for blog {}", blog.name());
        var post = new PostRequest(blog.id(),
                latestEntryFromFeed.getTitle(),
                latestEntryFromFeed.getLink(),
                LocalDateTime.now(clock));
        saveNewPost(post);
    }

    public void saveNewPost(PostRequest post) {
        logger.info("Save new post {}", post.name());
        postDao.savePost(post);
    }

    private List<SyndEntry> getEntriesFromFeed(Blog blog) {
        try {
            var uri = new URI(blog.feedUrl());
            return new SyndFeedInput().build(new InputSource(uri.toURL().openStream()))
                    .getEntries().stream().toList();
        } catch (FeedException | IOException | URISyntaxException exception) {
            var errorMessage = "An exception occurred while reading the feed for blog %s".formatted(blog.feedUrl());
            logger.error(errorMessage, exception);
            throw new RuntimeException(errorMessage);
        }
    }

    public void markPostAsRead(String postId) {
        var now = Timestamp.from(Instant.now(clock));
        postDao.markPostAsRead(Integer.parseInt(postId), now);
    }

    public void deleteReadPostsOlderThan30Days() {
        postDao.deleteReadPostsOlderThanDays(30);
    }

    public List<PostResponse> getUnreadPosts() {
        return postDao.getUnreadPosts();
    }
}
