package com.example.urbannest.dto.Requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private String name;
    private String phone;
    private String nid;
    private String profilePictureUrl;
}
