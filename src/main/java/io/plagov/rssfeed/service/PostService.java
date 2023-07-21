package io.plagov.rssfeed.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.dao.PostDao;
import io.plagov.rssfeed.domain.Blog;
import io.plagov.rssfeed.domain.request.PostRequest;
import io.plagov.rssfeed.domain.response.PostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class PostService {

    private final BlogDao blogDao;
    private final PostDao postDao;
    private final Clock clock;
    private final SyndFeedInput feedInput = new SyndFeedInput();

    private final Logger logger = LoggerFactory.getLogger(PostService.class);

    public PostService(BlogDao blogDao, PostDao postDao, Clock clock) {
        this.blogDao = blogDao;
        this.postDao = postDao;
        this.clock = clock;
    }

    @Scheduled(cron = "@midnight")
    public void recordLatestBlogPost() {
        logger.info("Running scheduled service");
        var allBlogs = blogDao.getAllBlogs();
        allBlogs.forEach(blog -> {
            var latestSavedPost = postDao.getLatestPostForBlog(blog.name());

            if (latestSavedPost == null) {
                recordFirstEntryForBlog(blog);
            } else if (!latestSavedPostIsLatestInFeed(latestSavedPost, blog)) {
                saveNewPostsFromFeed(blog, latestSavedPost);
            }
        });
    }

    private void saveNewPostsFromFeed(Blog blog, PostResponse latestSavedPost) {
        var postIndex = getIndexOfLatestSavedPostInFeed(latestSavedPost, blog);
        var allEntries = getEntriesFromFeed(blog);
        for (var i = postIndex - 1; i >= 0; i--) {
            var entry = allEntries.get(i);
            var post = new PostRequest(blog.id(), entry.getTitle(), entry.getLink(), LocalDateTime.now(clock));
            saveNewPost(post);
        }
    }

    private boolean latestSavedPostIsLatestInFeed(PostResponse latestSavedPost, Blog blog) {
        var index = getIndexOfLatestSavedPostInFeed(latestSavedPost, blog);
        return index == 0;
    }

    private int getIndexOfLatestSavedPostInFeed(PostResponse latestSavedPost, Blog blog) {
        var allEntriesFromFeed = getEntriesFromFeed(blog);
        return IntStream.range(0, allEntriesFromFeed.size())
                .filter(i -> allEntriesFromFeed.get(i).getTitle().equals(latestSavedPost.name()))
                .findFirst()
                .orElseThrow();
    }

    private void recordFirstEntryForBlog(Blog blog) {
        var latestEntryFromFeed = getEntriesFromFeed(blog).get(0);
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
            return feedInput.build(new XmlReader(new URL(blog.url()))).getEntries();
        } catch (FeedException | IOException exception) {
            var errorMessage = "An exception occurred while reading the feed for blog %s".formatted(blog.url());
            logger.error(errorMessage, exception);
            throw new RuntimeException(errorMessage);
        }
    }
}
