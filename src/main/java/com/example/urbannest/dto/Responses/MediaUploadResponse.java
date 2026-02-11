package com.example.urbannest.dto.Responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaUploadResponse {
    private String uploadUrl;
    private String key;
}