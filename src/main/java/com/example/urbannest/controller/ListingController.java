package com.example.urbannest.controller;

import com.example.urbannest.dto.Requests.ListingCreateRequest;
import com.example.urbannest.dto.Requests.ListingUpdateRequest;
import com.example.urbannest.dto.Responses.ApiResponse;
import com.example.urbannest.dto.Responses.ListingResponse;
import com.example.urbannest.model.enums.PropertyType;
import com.example.urbannest.service.ListingService;
import com.example.urbannest.util.FirebaseUtil;
import com.google.firebase.auth.FirebaseToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Listings", description = "Property listing CRUD, search, favorites, and saved listings")
@RestController
@RequestMapping("/listings")
public class ListingController {
    private final ListingService listingService;

    public ListingController(ListingService listingService){
        this.listingService = listingService;
    }

    @Operation(summary = "Create a listing", description = "Creates a new property listing as draft or published. Requires authentication.")
    @PostMapping
    public ResponseEntity<ApiResponse> createListing(
            @Valid @RequestBody ListingCreateRequest request
    ){
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.createListing(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get a listing by ID", description = "Returns full listing details including owner, property details, location, counters, and media.")
    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListingById(
            @Parameter(description = "Listing UUID") @PathVariable("id") UUID listingId){
        ListingResponse response = listingService.getListingById(listingId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a listing", description = "Partially updates a listing. Only the owner can update. Tracks price history on price changes.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updateListingById(
            @Parameter(description = "Listing UUID") @PathVariable("id") UUID listingId,
            @Valid @RequestBody ListingUpdateRequest request){
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.updateListing(token, listingId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a listing", description = "Soft-deletes a listing by setting status to archived. Only the owner can delete.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteListingById(
            @Parameter(description = "Listing UUID") @PathVariable("id") UUID listingId) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.deleteListing(token, listingId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search published listings", description = "Public endpoint. Returns paginated published listings with optional filters.")
    @GetMapping
    public ResponseEntity<Page<ListingResponse>> getListings(
            @Parameter(description = "Filter by property type") @RequestParam(required = false) PropertyType propertyType,
            @Parameter(description = "Minimum price (inclusive)") @RequestParam(required = false) BigDecimal priceMin,
            @Parameter(description = "Maximum price (inclusive)") @RequestParam(required = false) BigDecimal priceMax,
            @Parameter(description = "Filter by district name") @RequestParam(required = false) String district,
            @Parameter(description = "Minimum number of bedrooms") @RequestParam(required = false) Integer minBedrooms,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ListingResponse> page = listingService.getListings(
                propertyType, priceMin, priceMax, district, minBedrooms, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get my listings", description = "Returns the authenticated user's own listings (excluding deleted).")
    @GetMapping("/my")
    public ResponseEntity<Page<ListingResponse>> getMyListings(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        Page<ListingResponse> page = listingService.getMyListings(token, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get my favorite listings", description = "Returns listings the authenticated user has favorited.")
    @GetMapping("/favorites")
    public ResponseEntity<Page<ListingResponse>> getMyFavorites(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        Page<ListingResponse> page = listingService.getMyFavorites(token, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Get my saved listings", description = "Returns listings the authenticated user has saved for later.")
    @GetMapping("/saved")
    public ResponseEntity<Page<ListingResponse>> getMySavedListings(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        Page<ListingResponse> page = listingService.getMySavedListings(token, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Favorite a listing", description = "Adds a listing to the authenticated user's favorites. Returns 409 if already favorited.")
    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse> addFavorite(
            @Parameter(description = "Listing UUID") @PathVariable("id") UUID listingId) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.addFavorite(token, listingId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Unfavorite a listing", description = "Removes a listing from the authenticated user's favorites. Returns 404 if not favorited.")
    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse> removeFavorite(
            @Parameter(description = "Listing UUID") @PathVariable("id") UUID listingId) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.removeFavorite(token, listingId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Save a listing", description = "Saves a listing for later viewing. Returns 409 if already saved.")
    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse> saveListing(
            @Parameter(description = "Listing UUID") @PathVariable("id") UUID listingId) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.saveListing(token, listingId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Unsave a listing", description = "Removes a listing from the authenticated user's saved list. Returns 404 if not saved.")
    @DeleteMapping("/{id}/save")
    public ResponseEntity<ApiResponse> unsaveListing(
            @Parameter(description = "Listing UUID") @PathVariable("id") UUID listingId) {
        FirebaseToken token = FirebaseUtil.getFirebaseToken();
        ApiResponse response = listingService.unsaveListing(token, listingId);
        return ResponseEntity.ok(response);
    }
}