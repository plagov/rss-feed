package io.plagov.rssfeed.domain.request;

public record NewBlog(
        String name,
        String feedUrl) { }
