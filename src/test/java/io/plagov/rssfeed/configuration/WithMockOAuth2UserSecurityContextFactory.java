package io.plagov.rssfeed.configuration;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WithMockOAuth2UserSecurityContextFactory implements WithSecurityContextFactory<WithMockOAuth2User> {
    @Override
    public SecurityContext createSecurityContext(WithMockOAuth2User annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", annotation.email());
        attributes.put("name", annotation.name());

        OAuth2User principal = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );

        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                principal,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                "github"
        );

        context.setAuthentication(auth);
        return context;
    }
}
