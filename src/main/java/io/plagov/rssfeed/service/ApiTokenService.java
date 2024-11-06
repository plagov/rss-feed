package io.plagov.rssfeed.service;

import io.plagov.rssfeed.dao.TokenDao;
import io.plagov.rssfeed.domain.ApiToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class ApiTokenService {

    private final TokenDao tokenDao;

    public ApiTokenService(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    public void generateNewToken(String description) {
        ApiToken token = new ApiToken(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                description
        );
        tokenDao.saveToken(token);
    }

    public boolean validateToken(String token) {
        return tokenDao.existsByToken(token);
    }

    public Iterable<ApiToken> getAllTokens() {
        return tokenDao.findAll();
    }
}
