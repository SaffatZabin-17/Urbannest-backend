package com.example.urbannest.controller;

import com.example.urbannest.dto.Requests.UserRegistrationRequest;
import com.example.urbannest.dto.Requests.UserUpdateRequest;
import com.example.urbannest.dto.Responses.ApiResponse;
import com.example.urbannest.dto.Responses.UserResponse;
import com.example.urbannest.service.UserService;
import com.google.firebase.auth.FirebaseToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.urbannest.util.FirebaseUtil;

@Tag(name = "Users", description = "User registration and profile management")
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register a new user", description = "Creates a user account linked to the authenticated Firebase UID.")
    @PostMapping
    public ResponseEntity<ApiResponse> createUser(
            @Valid @RequestBody(required = false) UserRegistrationRequest request) {

        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = userService.registerUser(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get current user profile", description = "Returns the authenticated user's profile information.")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        UserResponse response = userService.getAuthenticatedUser(token);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update current user profile", description = "Partially updates the authenticated user's profile. Only provided fields are updated.")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse> updateCurrentUser(
            @RequestBody UserUpdateRequest request
    ){
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        userService.updateProfile(token, request);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully"));
    }
}