package io.plagov.rssfeed.dao;

import io.plagov.rssfeed.domain.Blog;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BlogDao {

    private final JdbcClient jdbcClient;

    public BlogDao(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Blog> getBlogsForUser(boolean isSubscribed, UUID userId) {
        var query = """
                SELECT *
                FROM blogs
                WHERE is_subscribed = :isSubscribed
                  AND user_id = :userId
                ORDER BY id
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

    public Blog getBlogForUser(int blogId, UUID userId) {
        var sql = """
                SELECT *
                FROM blogs
                WHERE id = :blogId
                  AND user_id = :userId
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
                    is_subscribed = :isSubscribed
                WHERE id = :blogId
                  AND user_id = :userId
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
