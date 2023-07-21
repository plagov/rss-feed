package io.plagov.rssfeed.domain.request;

import java.time.LocalDateTime;

public record PostRequest(
        int blogId,
        String name,
        String url,
        LocalDateTime dateAdded
) {
}
