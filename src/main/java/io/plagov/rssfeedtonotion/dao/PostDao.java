package io.plagov.rssfeedtonotion.dao;

import io.plagov.rssfeedtonotion.domain.Post;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PostDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PostDao(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Post getLatestPostForBlog(String blogName) {
        var sql = """
                SELECT p.*
                FROM posts p
                JOIN blogs b ON p.blog_id = b.id
                WHERE b.name = :blogName
                ORDER BY p.post_date DESC
                LIMIT 1;""";

        return jdbcTemplate.queryForObject(sql, Map.of("blogName", blogName), (rs, rowNum) ->
                new Post(rs.getInt("id"),
                        rs.getInt("blog_id"),
                        rs.getString("post_name"),
                        rs.getString("post_url"),
                        rs.getObject("post_date", LocalDateTime.class)
                )
        );
    }
}
