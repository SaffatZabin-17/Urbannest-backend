package com.example.urbannest.controller;

import com.example.urbannest.dto.Requests.UserRegistrationRequest;
import com.example.urbannest.dto.Responses.UserResponse;
import com.example.urbannest.service.UserService;
import com.google.firebase.auth.FirebaseToken;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody(required = false) UserRegistrationRequest request) {

        FirebaseToken token = getFirebaseToken();
        UserResponse response = userService.registerUser(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        FirebaseToken token = getFirebaseToken();
        UserResponse response = userService.getAuthenticatedUser(token);
        return ResponseEntity.ok(response);
    }

    private FirebaseToken getFirebaseToken() {
        return (FirebaseToken) Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getPrincipal();
    }
}
