package com.example.urbannest.service;

import com.example.urbannest.dto.Requests.UserRegistrationRequest;
import com.example.urbannest.dto.Responses.UserResponse;
import com.example.urbannest.exception.ResourceAlreadyExistsException;
import com.example.urbannest.exception.ResourceNotFoundException;
import com.example.urbannest.model.User;
import com.example.urbannest.repository.UserRepository;
import com.example.urbannest.util.HashUtil;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse registerUser(FirebaseToken token, UserRegistrationRequest request) {
        String firebaseUid = token.getUid();

        userRepository.findByFirebaseId(firebaseUid).ifPresent(user -> {
            throw new ResourceAlreadyExistsException("User already registered");
        });

        boolean isGoogleLogin = request == null;

        User user = new User();
        user.setFirebaseId(firebaseUid);
        user.setRoleName("USER");
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        if (isGoogleLogin) {
            user.setName(token.getName());
            user.setEmail(token.getEmail());
            user.setProfilePictureUrl(token.getPicture());
            user.setNidHash("NOT_SET");
        } else {
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setNidHash(HashUtil.generateHash(request.getNid()));
        }

        User savedUser = userRepository.save(user);
        return constructResponse(savedUser);
    }

    public UserResponse getAuthenticatedUser(FirebaseToken token) {
        String firebaseUid = token.getUid();

        User user = userRepository.findByFirebaseId(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return constructResponse(user);
    }

    private UserResponse constructResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRoleName(user.getRoleName());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
