package com.example.urbannest.controller;

import com.example.urbannest.dto.Requests.MediaUploadRequest;
import com.example.urbannest.dto.Responses.MediaUploadResponse;
import com.example.urbannest.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "S3 Media", description = "Pre-signed URL generation for S3 uploads and downloads")
@RestController
@RequestMapping("/s3")
public class S3Controller {
    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @Operation(summary = "Get a pre-signed upload URL", description = "Generates a pre-signed S3 URL for uploading a file. Returns the URL and the S3 object key.")
    @PostMapping("/upload-request")
    public ResponseEntity<MediaUploadResponse> getUploadUrl(
            @Valid @RequestBody MediaUploadRequest request) {

        String key = request.getCategory() + "/" + UUID.randomUUID() + "/" + request.getFileName();
        String uploadUrl = s3Service.generateUploadUrl(key, request.getContentType());

        MediaUploadResponse response = new MediaUploadResponse();
        response.setUploadUrl(uploadUrl);
        response.setKey(key);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get a pre-signed download URL", description = "Generates a pre-signed S3 URL for downloading a file by its key.")
    @GetMapping("/download-url")
    public ResponseEntity<String> getDownloadUrl(
            @Parameter(description = "S3 object key") @RequestParam String key) {
        String downloadUrl = s3Service.generateDownloadUrl(key);
        return ResponseEntity.ok(downloadUrl);
    }

    @Operation(summary = "Delete an S3 object", description = "Deletes a file from S3 by its key.")
    @DeleteMapping
    public ResponseEntity<Void> deleteObject(
            @Parameter(description = "S3 object key") @RequestParam String key) {
        s3Service.deleteObject(key);
        return ResponseEntity.noContent().build();
    }
}