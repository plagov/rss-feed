package io.plagov.rssfeed.dao;

import io.plagov.rssfeed.domain.Post;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PostDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public PostDao(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcTemplate = jdbcTemplate;
    }

    public Post getLatestPostForBlog(String blogName) {
        var sql = """
                SELECT p.*
                FROM posts p
                JOIN blogs b ON p.blog_id = b.id
                WHERE b.name = :blogName
                ORDER BY p.id DESC
                LIMIT 1;""";

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, Map.of("blogName", blogName), mapToPost());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static RowMapper<Post> mapToPost() {
        return (rs, rowNum) ->
                new Post(rs.getInt("blog_id"),
                        rs.getString("post_name"),
                        rs.getString("post_url"),
                        rs.getBoolean("is_read"),
                        rs.getObject("date_added", LocalDateTime.class)
                );
    }

    public void savePost(Post post) {
        String sql = "INSERT INTO posts (blog_id, post_name, post_url, is_read) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, post.blogId(), post.name(), post.url(), post.isRead());
    }
}
