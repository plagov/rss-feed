package io.plagov.rssfeed.dao;

import io.plagov.rssfeed.domain.Blog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlogDao {

    private final JdbcTemplate jdbcTemplate;

    public BlogDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Blog> getAllBlogs() {
        return jdbcTemplate.query("SELECT * FROM blogs", (rs, i) -> new Blog(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("feed_url")
        ));
    }
}
