package com.example.urbannest.service;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.urbannest.dto.Requests.UserRegistrationRequest;
import com.example.urbannest.dto.Requests.UserUpdateRequest;
import com.example.urbannest.dto.Responses.ApiResponse;
import com.example.urbannest.dto.Responses.UserResponse;
import com.example.urbannest.exception.ResourceAlreadyExistsException;
import com.example.urbannest.exception.ResourceNotFoundException;
import com.example.urbannest.model.User;
import com.example.urbannest.repository.UserRepository;
import com.example.urbannest.util.EncryptionUtil;
import com.example.urbannest.util.HashUtil;
import com.google.firebase.auth.FirebaseToken;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Value("${encryption.nid-key}")
    private String nidEncryptionKey;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ApiResponse registerUser(FirebaseToken token, UserRegistrationRequest request) {
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
            user.setNidHash("NOT_SET_" + firebaseUid);
        } else {
            String nidHash = HashUtil.generateHash(request.getNid());
            boolean phoneTaken = request.getPhone() != null && userRepository.existsByPhone(request.getPhone());
            boolean nidTaken = userRepository.existsByNidHash(nidHash);

            if (phoneTaken && nidTaken) {
                throw new ResourceAlreadyExistsException("Phone number and NID need to be unique");
            } else if (phoneTaken) {
                throw new ResourceAlreadyExistsException("Phone number is already registered");
            } else if (nidTaken) {
                throw new ResourceAlreadyExistsException("NID is already registered");
            }

            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setNidHash(nidHash);
            user.setNidEncrypted(EncryptionUtil.encrypt(request.getNid(), nidEncryptionKey));
        }

        userRepository.save(user);
        return new ApiResponse(true, "User registered successfully");
    }

    public UserResponse getAuthenticatedUser(FirebaseToken token) {
        String firebaseUid = token.getUid();

        User user = userRepository.findByFirebaseId(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return constructResponse(user);
    }

    public void updateProfile(FirebaseToken token, UserUpdateRequest request) {
        String firebaseUid = token.getUid();
        User user = userRepository.findByFirebaseId(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if(request.getNid() != null) {
            user.setNidHash(HashUtil.generateHash(request.getNid()));
            user.setNidEncrypted(EncryptionUtil.encrypt(request.getNid(), nidEncryptionKey));
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
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
        if (user.getNidEncrypted() != null) {
            response.setNid(EncryptionUtil.decrypt(user.getNidEncrypted(), nidEncryptionKey));
        }
        return response;
    }
}
