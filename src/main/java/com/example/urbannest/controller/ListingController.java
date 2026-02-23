package com.example.urbannest.controller;

import com.example.urbannest.dto.Requests.ListingCreateRequest;
import com.example.urbannest.dto.Requests.ListingUpdateRequest;
import com.example.urbannest.dto.Responses.ApiResponse;
import com.example.urbannest.dto.Responses.ListingResponse;
import com.example.urbannest.service.ListingService;
import com.example.urbannest.util.FirebaseUtil;
import com.google.firebase.auth.FirebaseToken;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/listings")
public class ListingController {
    private final ListingService listingService;

    public ListingController(ListingService listingService){
        this.listingService = listingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createListing(
            @Valid @RequestBody ListingCreateRequest request
    ){
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.createListing(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListingById(@PathVariable("id") UUID listingId){
        ListingResponse response = listingService.getListingById(listingId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateListingById(
            @PathVariable("id") UUID listingId, @Valid @RequestBody ListingUpdateRequest request){
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.updateListing(token, listingId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteListingById(@PathVariable("id") UUID listingId) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.deleteListing(token, listingId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> publishListing(@PathVariable("id") UUID listingId){
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.publishListing(token, listingId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> sellListing(@PathVariable("id") UUID listingId){
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.markAsSold(token, listingId);
        return ResponseEntity.ok(response);
    }
}
