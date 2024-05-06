package io.plagov.rssfeed.domain;

public record Blog(
    int id,
    String name,
    String feedUrl,
    boolean isSubscribed
) {
}
