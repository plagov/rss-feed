package io.plagov.rssfeed.dao;

import io.plagov.rssfeed.domain.UserAccount;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserDao {

    private final JdbcClient jdbcClient;

    public UserDao(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public boolean existsAny() {
        return jdbcClient.sql("SELECT COUNT(*) FROM users")
                .query(Integer.class)
                .single() > 0;
    }

    public boolean existsByUsername(String username) {
        return jdbcClient.sql("SELECT COUNT(*) FROM users WHERE username = ?")
                .param(username)
                .query(Integer.class)
                .single() > 0;
    }

    public void save(UserAccount user) {
        jdbcClient.sql("""
                INSERT INTO users (id, username, password_hash, email, created_at)
                VALUES (?, ?, ?, ?, ?)
                """)
                .params(user.id(), user.username(), user.passwordHash(), user.email(), user.createdAt())
                .update();
    }

    public Optional<UserAccount> findByUsername(String username) {
        return jdbcClient.sql("""
                SELECT id, username, password_hash, email, created_at
                FROM users
                WHERE username = :username
                """)
                .param("username", username)
                .query((rs, rowNum) -> new UserAccount(
                        rs.getObject("id", UUID.class),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("email"),
                        rs.getObject("created_at", LocalDateTime.class)
                ))
                .optional();
    }

    public Optional<UserAccount> findById(UUID id) {
        return jdbcClient.sql("""
                SELECT id, username, password_hash, email, created_at
                FROM users
                WHERE id = :id
                """)
                .param("id", id)
                .query((rs, rowNum) -> new UserAccount(
                        rs.getObject("id", UUID.class),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("email"),
                        rs.getObject("created_at", LocalDateTime.class)
                ))
                .optional();
    }
}
