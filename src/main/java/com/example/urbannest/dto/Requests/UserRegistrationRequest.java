package com.example.urbannest.dto.Requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request body for registering a new user")
@Getter
@Setter
public class UserRegistrationRequest {
    @Schema(example = "John Doe")
    @NotBlank(message = "User must have a name")
    private String name;

    @Schema(example = "john@example.com")
    @NotBlank
    @Email(message = "Invalid email format")
    private String email;

    @Schema(example = "+8801712345678")
    private String phone;

    @Schema(description = "National ID number (will be encrypted)")
    @NotBlank(message = "NID cannot be blank")
    private String nid;
}