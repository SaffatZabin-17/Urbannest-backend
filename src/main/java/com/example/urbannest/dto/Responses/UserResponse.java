package com.example.urbannest.dto.Responses;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserResponse {
    private UUID userId;
    private String name;
    private String email;
    private String phone;
    private String nid;
    private String roleName;
    private String profilePictureUrl;
    private OffsetDateTime createdAt;
}
