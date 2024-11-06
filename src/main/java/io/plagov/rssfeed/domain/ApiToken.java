package io.plagov.rssfeed.domain;

import java.time.LocalDateTime;

public record ApiToken(
        String token,
        LocalDateTime createdAt,
        String description
) { }
