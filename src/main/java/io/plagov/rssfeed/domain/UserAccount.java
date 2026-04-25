package io.plagov.rssfeed.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserAccount(
        UUID id,
        String username,
        String passwordHash,
        String email,
        LocalDateTime createdAt
) {
}
