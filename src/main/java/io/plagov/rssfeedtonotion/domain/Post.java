package io.plagov.rssfeedtonotion.domain;

import java.time.LocalDateTime;

public record Post(
        int blogId,
        String name,
        String url,
        boolean isRead,
        LocalDateTime dateAdded
) {
}
