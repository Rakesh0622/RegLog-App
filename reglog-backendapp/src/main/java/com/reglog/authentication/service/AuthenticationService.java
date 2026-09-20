package com.reglog.authentication.service;

import com.reglog.authentication.entity.JwtToken;
import com.reglog.authentication.repository.JwtTokenRepository;
import com.reglog.dto.LoginRequest;
import com.reglog.dto.UserResponse;
import com.reglog.exception.InvalidCredentialsException;
import com.reglog.security.CookieUtil;
import com.reglog.security.JwtService;
import com.reglog.user.entity.User;
import com.reglog.user.repository.UserRepository;
import com.reglog.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtTokenRepository jwtTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CookieUtil cookieUtil;
    private final UserService userService;

    public AuthenticationService(UserRepository userRepository,
                                 JwtTokenRepository jwtTokenRepository,
                                 JwtService jwtService,
                                 PasswordEncoder passwordEncoder,
                                 CookieUtil cookieUtil,
                                 UserService userService) {
        this.userRepository = userRepository;
        this.jwtTokenRepository = jwtTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.cookieUtil = cookieUtil;
        this.userService = userService;
    }

    @Transactional
    public UserResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByName(request.getName().trim())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        Instant now = Instant.now();
        long maxAge = Duration.ofMillis(jwtService.getExpirationMs()).toSeconds();

        jwtTokenRepository.save(new JwtToken(
                user,
                token,
                LocalDateTime.ofInstant(now, ZoneOffset.UTC),
                LocalDateTime.ofInstant(now.plusMillis(jwtService.getExpirationMs()), ZoneOffset.UTC)));

        cookieUtil.write(cookieUtil.createJwtCookie(token, maxAge), response);
        return UserService.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(String username) {
        return userService.getUserByName(username);
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = cookieUtil.readJwt(request);
        if (token != null) {
            jwtTokenRepository.deleteByToken(token);
        }
        cookieUtil.write(cookieUtil.clearJwtCookie(), response);
    }
}