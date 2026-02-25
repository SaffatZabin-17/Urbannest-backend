package com.example.urbannest.controller;

import com.example.urbannest.dto.Requests.UserRegistrationRequest;
import com.example.urbannest.dto.Requests.UserUpdateRequest;
import com.example.urbannest.dto.Responses.ApiResponse;
import com.example.urbannest.dto.Responses.UserResponse;
import com.example.urbannest.service.UserService;
import com.google.firebase.auth.FirebaseToken;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.urbannest.util.FirebaseUtil;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(
            @Valid @RequestBody(required = false) UserRegistrationRequest request) {

        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = userService.registerUser(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        UserResponse response = userService.getAuthenticatedUser(token);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse> updateCurrentUser(
            @RequestBody UserUpdateRequest request
    ){
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        userService.updateProfile(token, request);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully"));
    }
}
