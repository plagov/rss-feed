package io.plagov.rssfeed.domain.response;

import java.time.LocalDateTime;

public record PostResponse(
        int id,
        int blogId,
        String name,
        String url,
        boolean isRead,
        LocalDateTime dateAdded
) {
}
