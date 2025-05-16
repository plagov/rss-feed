package io.plagov.rssfeed.dao;

import io.plagov.rssfeed.domain.Blog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import java.util.List;

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
}
