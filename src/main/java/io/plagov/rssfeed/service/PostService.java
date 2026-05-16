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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Component
public class PostService {

    private final BlogDao blogDao;
    private final PostDao postDao;
    private final Clock clock;
    private final AiService aiService;
    private final ScrapperService scrapperService;
    private final Logger logger = LoggerFactory.getLogger(PostService.class);

    public PostService(BlogDao blogDao, PostDao postDao, Clock clock, AiService aiService, ScrapperService scrapperService) {
        this.blogDao = blogDao;
        this.postDao = postDao;
        this.clock = clock;
        this.aiService = aiService;
        this.scrapperService = scrapperService;
    }

    public void recordLatestBlogPosts(UUID userId) {
        logger.info("Evaluate all blogs");
        var allBlogs = blogDao.getBlogsForUser(true, userId);
        allBlogs.forEach(this::recordLatestForBlog);
        logger.info("Finish evaluating blogs");
    }

    public void recordLatestPostsForBlog(int blogId, UUID userId) {
        var blog = blogDao.getBlogForUser(blogId, userId);
        recordLatestForBlog(blog);
        logger.info("Finish evaluating blog {}", blog.name());
    }

    private void recordLatestForBlog(Blog blog) {
        logger.info("Evaluate blog {}", blog.name());

        if (postDao.countUnreadPostsForBlog(blog.id()) > 0) {
            logger.info("Skip fetching for blog {} - unread posts already exist", blog.name());
            return;
        }

        var latestSavedPost = postDao.getLatestPostForBlog(blog.id());
        var allEntries = getEntriesFromFeed(blog);

        if (latestSavedPost.isEmpty()) {
            recordNewestEntryForBlog(blog, allEntries);
        } else {
            recordNextEntryForBlog(blog, latestSavedPost.get(), allEntries);
        }
    }

    private void recordNextEntryForBlog(Blog blog, PostResponse latestSavedPost, List<SyndEntry> allEntries) {
        var latestSavedPostIndex = getIndexOfLatestSavedPostInFeed(latestSavedPost, allEntries);

        if (latestSavedPostIndex.isPresent()) {
            int index = latestSavedPostIndex.get();
            int maxAttempts = 5;
            int attempts = 0;

            for (int i = index - 1; i >= 0 && attempts < maxAttempts; i--) {
                var nextEntry = allEntries.get(i);
                if (processEntry(blog, nextEntry)) {
                    return;
                }
                attempts++;
            }
        } else {
            logger.info("Latest saved post for blog {} is not in the feed. Fallback to oldest.", blog.name());
            recordOldestEntryForBlog(blog, allEntries);
        }
    }

    private boolean processEntry(Blog blog, SyndEntry entry) {
        boolean isIgnored = false;
        String reasoning = null;
        var dateRead = Timestamp.from(Instant.now(clock));

        if (blog.useAiFiltering()) {
            try {
                logger.info("Scraping and evaluating entry {} for blog {}", entry.getTitle(), blog.name());
                String content = scrapperService.scrape(entry.getLink());
                var evaluation = aiService.evaluatePost(content);
                logger.info("AI evaluation result for blog {}: {}", blog.name(), evaluation);
                isIgnored = !evaluation.worthReading();
                reasoning = isIgnored ? evaluation.reasoning() : null;
            } catch (Exception e) {
                logger.error("Error during AI evaluation for blog {}. Defaulting to NOT ignored.", blog.name(), e);
            }
        }

        var post = new PostRequest(blog.id(), entry.getTitle(), entry.getLink(), LocalDateTime.now(clock), isIgnored, reasoning);
        postDao.savePost(post, isIgnored, dateRead);
        return !isIgnored;
    }

    private Optional<Integer> getIndexOfLatestSavedPostInFeed(PostResponse latestSavedPost, List<SyndEntry> entriesFromFeed) {
        return IntStream.range(0, entriesFromFeed.size())
                .filter(i -> entriesFromFeed.get(i).getLink().equals(latestSavedPost.url()))
                .boxed()
                .findFirst();
    }

    private void recordNewestEntryForBlog(Blog blog, List<SyndEntry> allEntries) {
        if (allEntries.isEmpty()) {
            logger.info("No posts available for blog {} yet", blog.name());
            return;
        }

        int maxAttempts = 5;
        for (int i = 0; i < Math.min(allEntries.size(), maxAttempts); i++) {
            if (processEntry(blog, allEntries.get(i))) {
                return;
            }
        }
    }

    private void recordOldestEntryForBlog(Blog blog, List<SyndEntry> allEntries) {
        if (allEntries.isEmpty()) {
            logger.info("No posts available for blog {} yet", blog.name());
            return;
        }

        int maxAttempts = 5;
        int size = allEntries.size();
        for (int i = 0; i < maxAttempts && i < size; i++) {
            var entry = allEntries.get(size - 1 - i);
            if (processEntry(blog, entry)) {
                return;
            }
        }
    }

    List<SyndEntry> getEntriesFromFeed(Blog blog) {
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

    public void markPostAsRead(int postId, UUID userId) {
        var now = Timestamp.from(Instant.now(clock));
        postDao.markPostAsReadForUser(postId, now, userId);
        CompletableFuture.runAsync(() -> recordLatestBlogPosts(userId));
    }

    public void deleteReadPostsOlderThan30Days(UUID userId) {
        postDao.deleteReadPostsOlderThanDaysForUser(30, userId);
    }

    public List<PostResponse> getUnreadPosts(UUID userId) {
        return postDao.getUnreadPostsForUser(userId);
    }
}
