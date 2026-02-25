package com.example.urbannest.dto.Responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "User profile information")
@Getter
@Setter
public class UserResponse {
    private UUID userId;

    @Schema(example = "John Doe")
    private String name;

    @Schema(example = "john@example.com")
    private String email;

    @Schema(example = "+8801712345678")
    private String phone;

    @Schema(description = "Decrypted National ID")
    private String nid;

    @Schema(description = "User role", example = "user")
    private String roleName;

    @Schema(description = "S3 pre-signed URL for profile picture")
    private String profilePictureUrl;

    private OffsetDateTime createdAt;
}