package io.plagov.rssfeed.domain.response;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email
) {
}
