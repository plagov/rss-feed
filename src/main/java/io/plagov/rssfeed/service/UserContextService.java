package io.plagov.rssfeed.service;

import io.plagov.rssfeed.security.AuthenticatedUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserContextService {

    public UUID getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user is available");
        }

        var principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser user) {
            return user.getId();
        }
        if (principal instanceof Jwt jwt) {
            return UUID.fromString(jwt.getSubject());
        }
        throw new IllegalStateException("Unsupported authenticated principal");
    }
}
