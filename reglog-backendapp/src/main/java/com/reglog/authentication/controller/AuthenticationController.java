package com.reglog.authentication.controller;

import com.reglog.dto.ApiResponse;
import com.reglog.dto.LoginRequest;
import com.reglog.dto.UserResponse;
import com.reglog.authentication.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        UserResponse user = authenticationService.login(request, response);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", user));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(Principal principal) {
        UserResponse user = authenticationService.me(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok("Authenticated", user));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request,
                                                    HttpServletResponse response) {
        authenticationService.logout(request, response);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }
}