package com.example.urbannest.dto.Responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Response containing a pre-signed S3 upload URL and the generated object key")
@Getter
@Setter
public class MediaUploadResponse {
    @Schema(description = "Pre-signed S3 upload URL (PUT request)", example = "https://bucket.s3.amazonaws.com/...")
    private String uploadUrl;

    @Schema(description = "S3 object key to reference this file later", example = "listings/550e8400/photo.jpg")
    private String key;
}