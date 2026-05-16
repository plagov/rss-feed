package io.plagov.rssfeed.domain.response;

import java.time.LocalDateTime;

public record PostResponse(
        int id,
        int blogId,
        String blogName,
        String name,
        String url,
        boolean isRead,
        boolean isIgnored,
        String aiReason,
        LocalDateTime dateAdded
) {
}
