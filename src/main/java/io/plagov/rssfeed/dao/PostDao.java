package io.plagov.rssfeed.dao;

import io.plagov.rssfeed.domain.request.PostRequest;
import io.plagov.rssfeed.domain.response.PostResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class PostDao {

    private final JdbcClient jdbcClient;

    public PostDao(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<PostResponse> getLatestPostForBlog(String blogName) {
        var sql = """
                SELECT p.*
                FROM posts p
                JOIN blogs b ON p.blog_id = b.id
                WHERE b.name = :blogName
                ORDER BY p.id DESC
                LIMIT 1;""";

        return jdbcClient.sql(sql).param("blogName", blogName).query(mapToPost()).optional();
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
        jdbcClient
                .sql(sql)
                .params(post.blogId(), post.name(), post.url(), post.dateAdded())
                .update();
    }

    public List<PostResponse> getUnreadPosts() {
        var sql = "SELECT * FROM posts WHERE is_read = FALSE";
        return jdbcClient.sql(sql).query(mapToPost()).list();
    }

    public void markPostAsRead(int postId, Timestamp dateRead) {
        var sql = "UPDATE posts SET is_read = TRUE, date_read = ? WHERE id = ?";
        jdbcClient
                .sql(sql)
                .params(dateRead, postId)
                .update();
    }

    public void deleteReadPostsOlderThanDays(int days) {
        var query = "DELETE from posts WHERE is_read IS TRUE AND date_read < NOW() - make_interval(days => ?)";
        jdbcClient
                .sql(query)
                .param(days)
                .update();
    }
}
