package io.plagov.rssfeed.dao;

import io.plagov.rssfeed.domain.Blog;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BlogDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public BlogDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("blogs")
                .usingGeneratedKeyColumns("id");
    }

    public List<Blog> getAllBlogs() {
        return jdbcTemplate.query("SELECT * FROM blogs", mapBlogRow());
    }

    @NotNull
    private static RowMapper<Blog> mapBlogRow() {
        return (rs, i) -> new Blog(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("feed_url")
        );
    }

    public int addNewBlog(String blogName, String feedUrl) {
        return simpleJdbcInsert.executeAndReturnKey(Map.of("name", blogName, "feed_url", feedUrl)).intValue();
    }

    public void updateBlogById(int blogId, String blogName, String feedUrl) {
        var sql = "UPDATE blogs SET name = ?, feed_url = ? WHERE id = ?";
        jdbcTemplate.update(sql, blogName, feedUrl, blogId);
    }

    public Blog getBlogById(int blogId) {
        var sql = "SELECT * FROM blogs WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, mapBlogRow(), blogId);
    }
}
