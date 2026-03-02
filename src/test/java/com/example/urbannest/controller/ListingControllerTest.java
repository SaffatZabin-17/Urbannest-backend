package com.example.urbannest.controller;

import com.example.urbannest.dto.Requests.ListingCreateRequest;
import com.example.urbannest.dto.Responses.ApiResponse;
import com.example.urbannest.dto.Responses.ListingResponse;
import com.example.urbannest.exception.ResourceAlreadyExistsException;
import com.example.urbannest.exception.ResourceNotFoundException;
import com.example.urbannest.model.enums.PropertyType;
import com.example.urbannest.security.FirebaseAuthFilter;
import com.example.urbannest.service.ListingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListingController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ListingService listingService;

    @MockitoBean
    private FirebaseAuthFilter firebaseAuthFilter;

    @BeforeEach
    void setUp() {
        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("test-uid");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockToken, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ========== GET /listings (public) ==========

    @Test
    void getListings_returns200WithPage() throws Exception {
        Page<ListingResponse> page = new PageImpl<>(List.of());
        when(listingService.getListings(any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getListings_withFilters_passes200() throws Exception {
        when(listingService.getListings(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/listings")
                        .param("propertyType", "apartment")
                        .param("priceMin", "100000")
                        .param("priceMax", "500000")
                        .param("district", "Dhaka")
                        .param("minBedrooms", "2"))
                .andExpect(status().isOk());
    }

    // ========== GET /listings/{id} (public) ==========

    @Test
    void getListingById_found_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        ListingResponse response = new ListingResponse();
        response.setListingId(id);
        response.setTitle("Test Listing");
        when(listingService.getListingById(id)).thenReturn(response);

        mockMvc.perform(get("/listings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Listing"));
    }

    @Test
    void getListingById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(listingService.getListingById(id))
                .thenThrow(new ResourceNotFoundException("Listing not found"));

        mockMvc.perform(get("/listings/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ========== POST /listings (authenticated) ==========

    @Test
    void createListing_validRequest_returns201() throws Exception {
        when(listingService.createListing(any(), any()))
                .thenReturn(new ApiResponse(true, "Listing created successfully"));

        mockMvc.perform(post("/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createListing_missingRequiredFields_returns400() throws Exception {
        ListingCreateRequest invalid = new ListingCreateRequest();

        mockMvc.perform(post("/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    // ========== DELETE /listings/{id} (authenticated) ==========

    @Test
    void deleteListing_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(listingService.deleteListing(any(), any()))
                .thenReturn(new ApiResponse(true, "Listing deleted successfully"));

        mockMvc.perform(delete("/listings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ========== POST /listings/{id}/favorite (authenticated) ==========

    @Test
    void addFavorite_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        when(listingService.addFavorite(any(), any()))
                .thenReturn(new ApiResponse(true, "Listing added to favorites"));

        mockMvc.perform(post("/listings/{id}/favorite", id))
                .andExpect(status().isCreated());
    }

    @Test
    void addFavorite_alreadyFavorited_returns409() throws Exception {
        UUID id = UUID.randomUUID();
        when(listingService.addFavorite(any(), any()))
                .thenThrow(new ResourceAlreadyExistsException("Already favorited"));

        mockMvc.perform(post("/listings/{id}/favorite", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ========== Helper ==========

    private ListingCreateRequest buildValidCreateRequest() {
        ListingCreateRequest request = new ListingCreateRequest();
        request.setTitle("Modern Apartment");
        request.setPropertyType(PropertyType.apartment);
        request.setPricing(new BigDecimal("500000"));
        request.setIsPublishing(true);

        ListingCreateRequest.Details details = new ListingCreateRequest.Details();
        details.setYearBuilt(2020);
        details.setListingCondition(com.example.urbannest.model.enums.ListingCondition.brand_new);
        details.setBedroomsCount(3);
        details.setBathroomsCount(2);
        details.setBalconiesCount(1);
        details.setLivingArea(1200);
        request.setDetails(details);

        ListingCreateRequest.Location location = new ListingCreateRequest.Location();
        location.setAddressLine("House 12, Road 5");
        location.setArea("Gulshan");
        location.setDistrict("Dhaka");
        location.setZipCode("1212");
        location.setLatitude(new BigDecimal("23.780000"));
        location.setLongitude(new BigDecimal("90.420000"));
        request.setLocation(location);

        return request;
    }
}
