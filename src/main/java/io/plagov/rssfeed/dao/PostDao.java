package io.plagov.rssfeed.dao;

import io.plagov.rssfeed.domain.request.PostRequest;
import io.plagov.rssfeed.domain.response.PostResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class PostDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    public PostDao(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcTemplate = jdbcTemplate;
    }

    public PostResponse getLatestPostForBlog(String blogName) {
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

    private static RowMapper<PostResponse> mapToPost() {
        return (rs, rowNum) ->
                new PostResponse(rs.getInt("id"),
                        rs.getInt("blog_id"),
                        rs.getString("post_name"),
                        rs.getString("post_url"),
                        rs.getBoolean("is_read"),
                        rs.getObject("date_added", LocalDateTime.class)
                );
    }

    public void savePost(PostRequest post) {
        String sql = "INSERT INTO posts (blog_id, post_name, post_url, date_added) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, post.blogId(), post.name(), post.url(), post.dateAdded());
    }

    public List<PostResponse> getAllUnreadPosts() {
        var sql = "SELECT * FROM posts WHERE is_read = FALSE";
        return jdbcTemplate.query(sql, mapToPost());
    }
}
