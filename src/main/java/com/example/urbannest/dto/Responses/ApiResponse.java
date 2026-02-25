package com.example.urbannest.dto.Responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Standard API response for mutation operations")
@Getter
@Setter
public class ApiResponse {
    @Schema(description = "Whether the operation succeeded", example = "true")
    private boolean success;

    @Schema(description = "Human-readable result message", example = "Listing created successfully")
    private String message;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}