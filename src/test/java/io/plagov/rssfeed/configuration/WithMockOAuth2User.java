package io.plagov.rssfeed.configuration;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockOAuth2UserSecurityContextFactory.class)
public @interface WithMockOAuth2User {
    String email() default "test@example.com";
    String name() default "Test User";
}
