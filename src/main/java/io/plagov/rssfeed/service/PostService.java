package io.plagov.rssfeed.service;

import com.apptasticsoftware.rssreader.RssReader;
import io.plagov.rssfeed.dao.BlogDao;
import io.plagov.rssfeed.dao.PostDao;
import io.plagov.rssfeed.domain.Blog;
import io.plagov.rssfeed.domain.PostItem;
import io.plagov.rssfeed.domain.request.PostRequest;
import io.plagov.rssfeed.domain.response.PostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;
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

    @Scheduled(cron = "@midnight")
    public void recordLatestBlogPost() {
        logger.info("Running scheduled post service");
        var allBlogs = blogDao.getAllBlogs();
        allBlogs.forEach(blog -> {
            logger.info("Evaluate blog {}", blog.name());
            var latestSavedPost = postDao.getLatestPostForBlog(blog.name());
            var allEntries = getEntriesFromFeed(blog);

            if (latestSavedPost == null) {
                recordFirstEntryForBlog(blog, allEntries);
            } else if (!latestSavedPostIsLatestInFeed(latestSavedPost, allEntries)) {
                saveNewPostsFromFeed(blog, latestSavedPost, allEntries);
            }
        });
    }

    private void saveNewPostsFromFeed(Blog blog, PostResponse latestSavedPost, List<PostItem> allEntries) {
        var postIndex = getIndexOfLatestSavedPostInFeed(latestSavedPost, allEntries);
        logger.info("Save latest post for blog {}", blog.name());
        for (var i = postIndex - 1; i >= 0; i--) {
            var entry = allEntries.get(i);
            var post = new PostRequest(blog.id(), entry.title(), entry.link(), LocalDateTime.now(clock));
            saveNewPost(post);
        }
    }

    private boolean latestSavedPostIsLatestInFeed(PostResponse latestSavedPost, List<PostItem> allEntries) {
        var index = getIndexOfLatestSavedPostInFeed(latestSavedPost, allEntries);
        return index == 0;
    }

    private int getIndexOfLatestSavedPostInFeed(PostResponse latestSavedPost, List<PostItem> entriesFromFeed) {
        return IntStream.range(0, entriesFromFeed.size())
                .filter(i -> entriesFromFeed.get(i).title().equals(latestSavedPost.name()))
                .findFirst()
                .orElseThrow();
    }

    private void recordFirstEntryForBlog(Blog blog, List<PostItem> allEntries) {
        var latestEntryFromFeed = allEntries.get(0);
        logger.info("No saved posts in database for blog {}", blog.name());
        var post = new PostRequest(blog.id(),
                latestEntryFromFeed.title(),
                latestEntryFromFeed.link(),
                LocalDateTime.now(clock));
        saveNewPost(post);
    }

    public void saveNewPost(PostRequest post) {
        logger.info("Save new post {}", post.name());
        postDao.savePost(post);
    }

    private List<PostItem> getEntriesFromFeed(Blog blog) {
        RssReader rssReader = new RssReader();
        try {
            return rssReader.read(blog.url()).limit(5)
                    .filter(item -> item.getTitle().isPresent() && item.getLink().isPresent())
                    .map(item -> new PostItem(item.getTitle().get(), item.getLink().get()))
                    .toList();
        } catch (IOException exception) {
            var errorMessage = "An exception occurred while reading the feed for blog %s".formatted(blog.url());
            logger.error(errorMessage, exception);
            throw new RuntimeException(errorMessage);
        }
    }
}
