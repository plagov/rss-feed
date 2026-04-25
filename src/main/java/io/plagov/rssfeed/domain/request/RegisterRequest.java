package io.plagov.rssfeed.domain.request;

public record RegisterRequest(
        String username,
        String password,
        String email
) {
}
