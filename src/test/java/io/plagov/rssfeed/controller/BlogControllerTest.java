package io.plagov.rssfeed.controller;

import io.plagov.rssfeed.configuration.ContainersConfig;
import io.plagov.rssfeed.domain.request.NewBlog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Import(ContainersConfig.class)
class BlogControllerTest {

    @Autowired
    private BlogController blogController;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SuppressWarnings("'Delete' statement without 'where' clears all data in the table")
    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM blogs");
        jdbcTemplate.execute("DELETE FROM posts");
    }

    @Test
    void shouldAddNewBlog() {
        var newBlog = new NewBlog("Test Name", "blog.com/feed");
        var response = blogController.addNewBlog(newBlog);

        assertThat(blogController.getAllBlogs()).hasSize(1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var blog = blogController.getBlogById(Objects.requireNonNull(response.getBody()));
        assertThat(blog).extracting("name", "url").containsExactly(newBlog.name(), newBlog.feedUrl());
    }

    @Test
    void shouldUpdateBlogFeedUrlByBlogId() {
        var newBlog = new NewBlog("Test Name", "blog.com/feed");
        int blogId = Objects.requireNonNull(blogController.addNewBlog(newBlog).getBody());

        var updatedBlogRequest = new NewBlog("Test Name One", "blog.com/rss");
        blogController.updateBlog(blogId, updatedBlogRequest);

        var updatedBlog = blogController.getBlogById(blogId);
        assertThat(updatedBlog)
                .extracting("name", "url")
                .containsExactly(updatedBlogRequest.name(), updatedBlogRequest.feedUrl());
    }
}
