package io.plagov.rssfeed.domain.response;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String username
) {
}
