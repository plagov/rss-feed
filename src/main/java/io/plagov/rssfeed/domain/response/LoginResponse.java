package io.plagov.rssfeed.domain.response;

import java.util.UUID;

public record LoginResponse(
        UUID id,
        String username,
        String token
) {
}
