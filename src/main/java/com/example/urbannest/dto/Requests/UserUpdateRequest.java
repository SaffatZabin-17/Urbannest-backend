package com.example.urbannest.dto.Requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request body for partially updating user profile. Only provided fields are updated.")
@Getter
@Setter
public class UserUpdateRequest {
    private String name;
    private String phone;

    @Schema(description = "National ID number (will be re-encrypted)")
    private String nid;

    @Schema(description = "S3 object key for profile picture")
    private String profilePictureUrl;
}