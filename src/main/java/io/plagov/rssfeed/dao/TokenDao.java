package io.plagov.rssfeed.dao;

import io.plagov.rssfeed.domain.ApiToken;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class TokenDao {

    private final JdbcClient jdbcClient;

    public TokenDao(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void saveToken(ApiToken tokenRequest) {
        String sql = "INSERT INTO tokens (token, created_at, description) VALUES (?, ?, ?)";
        jdbcClient
                .sql(sql)
                .params(tokenRequest.token(), tokenRequest.createdAt(), tokenRequest.description())
                .update();
    }

    public boolean existsByToken(String token) {
        String sql = "SELECT COUNT(*) FROM tokens WHERE token = ?";
        return jdbcClient.sql(sql).param(token).query(Integer.class).single() > 0;
    }

    public Iterable<ApiToken> findAll() {
        String sql = "SELECT * FROM tokens";
        return jdbcClient
                .sql(sql)
                .query(ApiToken.class)
                .list();
    }
}
