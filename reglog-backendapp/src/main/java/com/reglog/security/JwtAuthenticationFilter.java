package com.reglog.security;

import com.reglog.authentication.repository.JwtTokenRepository;
import com.reglog.user.entity.User;
import com.reglog.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final JwtTokenRepository jwtTokenRepository;
    private final CookieUtil cookieUtil;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserRepository userRepository,
                                   JwtTokenRepository jwtTokenRepository,
                                   CookieUtil cookieUtil) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.jwtTokenRepository = jwtTokenRepository;
        this.cookieUtil = cookieUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = cookieUtil.readJwt(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtService.isValid(token)
                    && jwtTokenRepository.findByToken(token).isPresent()) {

                String username = jwtService.extractUsername(token);
                userRepository.findByName(username).ifPresent(user -> {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user.getName(),
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        }

        filterChain.doFilter(request, response);
    }
}