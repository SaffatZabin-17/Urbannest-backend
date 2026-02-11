package com.example.urbannest.controller;

import com.example.urbannest.dto.Requests.MediaUploadRequest;
import com.example.urbannest.dto.Responses.MediaUploadResponse;
import com.example.urbannest.service.S3Service;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/s3")
public class S3Controller {
    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

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

    @GetMapping("/download-url")
    public ResponseEntity<String> getDownloadUrl(@RequestParam String key) {
        String downloadUrl = s3Service.generateDownloadUrl(key);
        return ResponseEntity.ok(downloadUrl);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteObject(@RequestParam String key) {
        s3Service.deleteObject(key);
        return ResponseEntity.noContent().build();
    }
}