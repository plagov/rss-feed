package io.plagov.rssfeed.service;

import io.plagov.rssfeed.dao.UserDao;
import io.plagov.rssfeed.domain.UserAccount;
import io.plagov.rssfeed.domain.request.LoginRequest;
import io.plagov.rssfeed.domain.request.RegisterRequest;
import io.plagov.rssfeed.domain.response.LoginResponse;
import io.plagov.rssfeed.domain.response.MeResponse;
import io.plagov.rssfeed.domain.response.UserResponse;
import io.plagov.rssfeed.security.AuthenticatedUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserContextService userContextService;

    public AuthService(UserDao userDao,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserContextService userContextService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userContextService = userContextService;
    }

    public UserResponse registerUser(RegisterRequest request) {
        if (userDao.existsByUsername(request.username())) {
            throw new IllegalStateException("Username already exists");
        }

        var user = new UserAccount(
                UUID.randomUUID(),
                request.username(),
                passwordEncoder.encode(request.password()),
                request.email(),
                LocalDateTime.now()
        );
        userDao.save(user);
        return new UserResponse(user.id(), user.username(), user.email());
    }

    public LoginResponse login(LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            var principal = (AuthenticatedUser) authentication.getPrincipal();
            var token = jwtService.generateToken(principal.getId(), principal.getUsername());
            return new LoginResponse(principal.getId(), principal.getUsername(), token);
        } catch (BadCredentialsException exception) {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    public MeResponse getCurrentUser() {
        var userId = userContextService.getCurrentUserId();
        return userDao.findById(userId)
                .map(user -> new MeResponse(user.id(), user.username()))
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
    }
}
