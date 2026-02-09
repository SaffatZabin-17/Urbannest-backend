package com.example.urbannest.dto.Requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationRequest {
    @NotBlank(message = "User must have a name")
    private String name;

    @NotBlank
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    @NotBlank(message = "NID cannot be blank")
    private String nid;
}
