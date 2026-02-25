package com.example.urbannest.dto.Requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request body for generating a pre-signed S3 upload URL")
@Getter
@Setter
public class MediaUploadRequest {
    @Schema(description = "Original file name", example = "photo.jpg")
    @NotBlank(message = "File name is required")
    private String fileName;

    @Schema(description = "MIME type of the file", example = "image/jpeg")
    @NotBlank(message = "Content type is required")
    private String contentType;

    @Schema(description = "Upload category (used as S3 key prefix)", example = "listings")
    @NotBlank(message = "Category is required")
    private String category;
}