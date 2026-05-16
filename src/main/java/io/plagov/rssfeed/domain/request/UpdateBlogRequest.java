package io.plagov.rssfeed.domain.request;

public record UpdateBlogRequest(
    String name,
    String feedUrl,
    boolean isSubscribed,
    boolean useAiFiltering
) {
}
