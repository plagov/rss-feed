package io.plagov.rssfeed.configuration;

import io.plagov.rssfeed.service.ApiTokenService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
public class ApiTokenConfig {

    private final ApiTokenService apiTokenService;

    public ApiTokenConfig(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    @Bean
    public Filter apiTokenFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String path = request.getRequestURI();

                if (path.startsWith("/api/")) {
                    String headerToken = request.getHeader("X-API-Token");
                    if (!apiTokenService.validateToken(headerToken)) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }

                filterChain.doFilter(request, response);
            }
        };
    }
}
