package io.plagov.rssfeedtonotion.domain;

import java.time.LocalDateTime;

public record Post(
        int id,
        int blogId,
        String postName,
        String postUrl,
        LocalDateTime postDate
) {
}
