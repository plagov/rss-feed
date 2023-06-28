package io.plagov.rssfeedtonotion.domain;

import java.time.LocalDateTime;

public record Post(
        String blogName,
        String postName,
        LocalDateTime postDate
) {
}
