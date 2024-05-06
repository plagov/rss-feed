package io.plagov.rssfeed.dao;

import io.plagov.rssfeed.domain.Blog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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

    public int addNewBlog(String blogName, String feedUrl) {
        return simpleJdbcInsert
                .executeAndReturnKey(Map.of("name", blogName, "feed_url", feedUrl, "is_subscribed", true))
                .intValue();
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
