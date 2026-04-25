package io.plagov.rssfeed.dao;

import io.plagov.rssfeed.domain.Blog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BlogDao {

    private final SimpleJdbcInsert simpleJdbcInsert;
    private final JdbcClient jdbcClient;

    public BlogDao(JdbcTemplate jdbcTemplate, JdbcClient jdbcClient) {
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("blogs")
                .usingGeneratedKeyColumns("id");
        this.jdbcClient = jdbcClient;
    }

    public List<Blog>  getBlogs(boolean isSubscribed) {
        var query = "SELECT * FROM blogs WHERE is_subscribed = ?";
        return jdbcClient
                .sql(query)
                .params(isSubscribed)
                .query(Blog.class)
                .list();
    }

    public void addNewBlog(String feedUrl, String blogName) {
        var query = "INSERT INTO blogs (name, feed_url, is_subscribed) VALUES (?, ?, ?);";
        jdbcClient
                .sql(query)
                .params(blogName, feedUrl, true)
                .update();
    }

    public List<Blog> getBlogsForUser(boolean isSubscribed, UUID userId) {
        var query = """
                SELECT *
                FROM blogs
                WHERE is_subscribed = :isSubscribed
                  AND (user_id = :userId OR user_id IS NULL)
                """;
        return jdbcClient
                .sql(query)
                .param("isSubscribed", isSubscribed)
                .param("userId", userId)
                .query(Blog.class)
                .list();
    }

    public void addNewBlogForUser(String feedUrl, String blogName, UUID userId) {
        var query = "INSERT INTO blogs (name, feed_url, is_subscribed, user_id) VALUES (?, ?, ?, ?);";
        jdbcClient
                .sql(query)
                .params(blogName, feedUrl, true, userId)
                .update();
    }

    public void updateBlog(int blogId, String blogName, String feedUrl, boolean isSubscribed) {
        var sql = "UPDATE blogs SET name = ?, feed_url = ?, is_subscribed = ? WHERE id = ?";
        jdbcClient
                .sql(sql)
                .params(blogName, feedUrl, isSubscribed, blogId)
                .update();
    }

    public Blog getBlog(int blogId) {
        var sql = "SELECT * FROM blogs WHERE id = ?";
        return jdbcClient
                .sql(sql)
                .params(blogId)
                .query(Blog.class)
                .single();
    }

    public Blog getBlogForUser(int blogId, UUID userId) {
        var sql = """
                SELECT *
                FROM blogs
                WHERE id = :blogId
                  AND (user_id = :userId OR user_id IS NULL)
                """;
        return jdbcClient
                .sql(sql)
                .param("blogId", blogId)
                .param("userId", userId)
                .query(Blog.class)
                .single();
    }

    public void updateBlogForUser(int blogId, String blogName, String feedUrl, boolean isSubscribed, UUID userId) {
        var sql = """
                UPDATE blogs
                SET name = :blogName,
                    feed_url = :feedUrl,
                    is_subscribed = :isSubscribed,
                    user_id = COALESCE(user_id, :userId)
                WHERE id = :blogId
                  AND (user_id = :userId OR user_id IS NULL)
                """;
        jdbcClient
                .sql(sql)
                .param("blogName", blogName)
                .param("feedUrl", feedUrl)
                .param("isSubscribed", isSubscribed)
                .param("userId", userId)
                .param("blogId", blogId)
                .update();
    }
}
