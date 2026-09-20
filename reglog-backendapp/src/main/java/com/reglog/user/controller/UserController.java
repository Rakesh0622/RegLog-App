package com.reglog.user.controller;

import com.reglog.dto.ApiResponse;
import com.reglog.dto.RegisterRequest;
import com.reglog.dto.UserResponse;
import com.reglog.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", user));
    }

    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByName(@PathVariable String name) {
        UserResponse user = userService.getUserByName(name);
        return ResponseEntity.ok(ApiResponse.ok("User found", user));
    }
}