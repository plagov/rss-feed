package io.plagov.rssfeed.domain.request;

public record LoginRequest(
        String username,
        String password
) {
}
