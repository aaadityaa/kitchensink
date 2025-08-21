package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.dto.AuthRequest;
import com.mongodb.kitchensink.dto.UserRequest;
import com.mongodb.kitchensink.exception.UserCreationException;
import com.mongodb.kitchensink.service.AuthService;
import com.mongodb.kitchensink.service.UserInfoService;
import com.mongodb.kitchensink.service.UserValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final UserInfoService service;
    private final UserValidation validation;
    private final AuthService authService;

    public AuthController(UserInfoService service, UserValidation validation, AuthService authService) {
        this.service = service;
        this.validation = validation;
        this.authService = authService;
    }

    @PostMapping("/register-user")
    @Operation(summary = "Register a new user", description = "Creates a new user account with encoded password")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest userRequest) {
        if(validation.isExistingUser(userRequest))
            throw new UserCreationException("Email already in use!");
        service.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }

    @PostMapping(value="/login")
    public ResponseEntity<Map<String,String>> login(@Valid @RequestBody AuthRequest authRequest) {
        String token = authService.login(authRequest);
        return ResponseEntity.ok(Map.of("token", token, "type", "Bearer"));
    }
}

