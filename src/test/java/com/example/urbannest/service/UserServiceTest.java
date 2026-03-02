package com.example.urbannest.service;

import com.example.urbannest.dto.Requests.UserRegistrationRequest;
import com.example.urbannest.dto.Requests.UserUpdateRequest;
import com.example.urbannest.dto.Responses.ApiResponse;
import com.example.urbannest.dto.Responses.UserResponse;
import com.example.urbannest.exception.ResourceAlreadyExistsException;
import com.example.urbannest.exception.ResourceNotFoundException;
import com.example.urbannest.mapper.UserMapper;
import com.example.urbannest.model.User;
import com.example.urbannest.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String TEST_ENCRYPTION_KEY = "QUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUE=";
    private static final String FIREBASE_UID = "test-firebase-uid";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private FirebaseToken mockToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "nidEncryptionKey", TEST_ENCRYPTION_KEY);
        mockToken = mock(FirebaseToken.class);
        lenient().when(mockToken.getUid()).thenReturn(FIREBASE_UID);
    }

    @Test
    void registerUser_normalRegistration_succeeds() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.empty());
        when(userRepository.existsByPhone("+8801712345678")).thenReturn(false);
        when(userRepository.existsByNidHash(anyString())).thenReturn(false);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPhone("+8801712345678");
        request.setNid("1234567890");

        ApiResponse response = userService.registerUser(mockToken, request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("User registered successfully");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_googleLogin_setsTokenFieldsAndNidPlaceholder() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.empty());
        when(mockToken.getName()).thenReturn("Google User");
        when(mockToken.getEmail()).thenReturn("google@example.com");
        when(mockToken.getPicture()).thenReturn("https://photo.url");

        ApiResponse response = userService.registerUser(mockToken, null);

        assertThat(response.isSuccess()).isTrue();
        verify(userRepository).save(argThat(user ->
                "Google User".equals(user.getName()) &&
                "google@example.com".equals(user.getEmail()) &&
                ("NOT_SET_" + FIREBASE_UID).equals(
                        ReflectionTestUtils.getField(user, "nidHash"))
        ));
    }

    @Test
    void registerUser_userAlreadyExists_throwsConflict() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.registerUser(mockToken, new UserRegistrationRequest()))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("User already registered");
    }

    @Test
    void registerUser_phoneTaken_throwsConflict() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.empty());
        when(userRepository.existsByPhone("+8801712345678")).thenReturn(true);
        when(userRepository.existsByNidHash(anyString())).thenReturn(false);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("Test");
        request.setEmail("test@example.com");
        request.setPhone("+8801712345678");
        request.setNid("1234567890");

        assertThatThrownBy(() -> userService.registerUser(mockToken, request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Phone number is already registered");
    }

    @Test
    void registerUser_nidTaken_throwsConflict() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.empty());
        when(userRepository.existsByNidHash(anyString())).thenReturn(true);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("Test");
        request.setEmail("test@example.com");
        request.setNid("1234567890");
        // phone is null → existsByPhone is never called

        assertThatThrownBy(() -> userService.registerUser(mockToken, request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("NID is already registered");
    }

    @Test
    void registerUser_phoneAndNidBothTaken_throwsCombinedMessage() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.empty());
        when(userRepository.existsByPhone("+8801712345678")).thenReturn(true);
        when(userRepository.existsByNidHash(anyString())).thenReturn(true);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("Test");
        request.setEmail("test@example.com");
        request.setPhone("+8801712345678");
        request.setNid("1234567890");

        assertThatThrownBy(() -> userService.registerUser(mockToken, request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Phone number and NID need to be unique");
    }

    @Test
    void getAuthenticatedUser_found_returnsResponse() {
        User user = new User();
        UserResponse expectedResponse = new UserResponse();
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user, TEST_ENCRYPTION_KEY)).thenReturn(expectedResponse);

        UserResponse result = userService.getAuthenticatedUser(mockToken);

        assertThat(result).isSameAs(expectedResponse);
    }

    @Test
    void getAuthenticatedUser_notFound_throws404() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getAuthenticatedUser(mockToken))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void updateProfile_updatesOnlyProvidedFields() {
        User user = new User();
        user.setName("Old Name");
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(user));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("New Name");
        // phone, nid, profilePictureUrl are null → not updated

        userService.updateProfile(mockToken, request);

        verify(userRepository).save(argThat(u ->
                "New Name".equals(u.getName()) && u.getUpdatedAt() != null
        ));
    }

    @Test
    void updateProfile_userNotFound_throws404() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(mockToken, new UserUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
