package io.plagov.rssfeed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
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

import java.io.IOException;
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

    @Autowired
    private ObjectMapper objectMapper;

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
    void shouldUpdateBlogFeedUrlByBlogId() throws IOException {
        var newBlog = new NewBlog("Test Name", "blog.com/feed");
        int blogId = Objects.requireNonNull(blogController.addNewBlog(newBlog).getBody());

        ObjectNode patchOperation = objectMapper.createObjectNode()
                .put("op", "replace")
                .put("path", "/url")
                .put("value", "blog.com/rss");
        var patchArray = objectMapper.createArrayNode().add(patchOperation);
        var jsonPatch = JsonPatch.fromJson(patchArray);
        blogController.updateBlog(blogId, jsonPatch);

        var updatedBlog = blogController.getBlogById(blogId);
        assertThat(updatedBlog)
                .extracting("name", "url")
                .containsExactly("Test Name", "blog.com/rss");
    }
}
