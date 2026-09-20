package com.reglog.user.service;

import com.reglog.dto.UserResponse;
import com.reglog.exception.EmailAlreadyExistsException;
import com.reglog.exception.UsernameAlreadyExistsException;
import com.reglog.exception.UserNotFoundException;
import com.reglog.user.entity.User;
import com.reglog.user.repository.UserRepository;
import com.reglog.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String name = normalize(request.getName());
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByName(name)) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User(
                name,
                passwordEncoder.encode(request.getPassword()),
                email,
                normalizePhone(request.getPhone()));

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByName(String name) {
        User user = userRepository.findByName(normalize(name))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public User getEntityByName(String name) {
        return userRepository.findByName(normalize(name))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone());
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private String normalizePhone(String value) {
        return value == null ? null : value.trim().replaceAll("[\\s-]", "");
    }
}