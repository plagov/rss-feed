package io.plagov.rssfeed.dao;

import io.plagov.rssfeed.domain.Blog;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BlogDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final JdbcClient jdbcClient;

    public BlogDao(JdbcTemplate jdbcTemplate, JdbcClient jdbcClient) {
        this.jdbcTemplate = jdbcTemplate;
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
                .query(mapBlogRow())
                .list();
    }

    @NotNull
    private static RowMapper<Blog> mapBlogRow() {
        return (rs, i) -> new Blog(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("feed_url"),
                rs.getBoolean("is_subscribed")
        );
    }

    public int addNewBlog(String blogName, String feedUrl) {
        return simpleJdbcInsert
                .executeAndReturnKey(Map.of("name", blogName, "feed_url", feedUrl, "is_subscribed", true))
                .intValue();
    }

    public void updateBlog(int blogId, String blogName, String feedUrl, boolean isSubscribed) {
        var sql = "UPDATE blogs SET name = ?, feed_url = ?, is_subscribed = ? WHERE id = ?";
        jdbcTemplate.update(sql, blogName, feedUrl, isSubscribed, blogId);
    }

    public Blog getBlog(int blogId) {
        var sql = "SELECT * FROM blogs WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, mapBlogRow(), blogId);
    }
}
