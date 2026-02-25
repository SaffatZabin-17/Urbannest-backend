package com.example.urbannest.controller;

import com.example.urbannest.dto.Requests.ListingCreateRequest;
import com.example.urbannest.dto.Requests.ListingUpdateRequest;
import com.example.urbannest.dto.Responses.ApiResponse;
import com.example.urbannest.dto.Responses.ListingResponse;
import com.example.urbannest.model.enums.PropertyType;
import com.example.urbannest.service.ListingService;
import com.example.urbannest.util.FirebaseUtil;
import com.google.firebase.auth.FirebaseToken;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @GetMapping
    public ResponseEntity<Page<ListingResponse>> getListings(
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer minBedrooms,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ListingResponse> page = listingService.getListings(
                propertyType, priceMin, priceMax, district, minBedrooms, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ListingResponse>> getMyListings(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        Page<ListingResponse> page = listingService.getMyListings(token, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/favorites")
    public ResponseEntity<Page<ListingResponse>> getMyFavorites(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        Page<ListingResponse> page = listingService.getMyFavorites(token, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/saved")
    public ResponseEntity<Page<ListingResponse>> getMySavedListings(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        Page<ListingResponse> page = listingService.getMySavedListings(token, pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse> addFavorite(@PathVariable("id") UUID listingId) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.addFavorite(token, listingId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse> removeFavorite(@PathVariable("id") UUID listingId) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.removeFavorite(token, listingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse> saveListing(@PathVariable("id") UUID listingId) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.saveListing(token, listingId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/save")
    public ResponseEntity<ApiResponse> unsaveListing(@PathVariable("id") UUID listingId) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.unsaveListing(token, listingId);
        return ResponseEntity.ok(response);
    }
}
