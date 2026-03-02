package com.example.urbannest.service;

import com.example.urbannest.dto.Requests.ListingCreateRequest;
import com.example.urbannest.dto.Requests.ListingUpdateRequest;
import com.example.urbannest.dto.Responses.ApiResponse;
import com.example.urbannest.dto.Responses.ListingResponse;
import com.example.urbannest.exception.ResourceAlreadyExistsException;
import com.example.urbannest.exception.ResourceNotFoundException;
import com.example.urbannest.exception.UnauthorizedAccessException;
import com.example.urbannest.mapper.ListingDetailsMapper;
import com.example.urbannest.mapper.ListingMapper;
import com.example.urbannest.mapper.MediaAssetMapper;
import com.example.urbannest.model.*;
import com.example.urbannest.model.composite.FavoriteListingId;
import com.example.urbannest.model.composite.SavedListingId;
import com.example.urbannest.model.enums.PropertyStatus;
import com.example.urbannest.model.enums.PropertyType;
import com.example.urbannest.repository.*;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    private static final String FIREBASE_UID = "test-firebase-uid";

    @Mock private ListingRepository listingRepository;
    @Mock private ListingDetailsRepository listingDetailsRepository;
    @Mock private ListingLocationRepository listingLocationRepository;
    @Mock private ListingCountersRepository listingCountersRepository;
    @Mock private ListingMediaRepository listingMediaRepository;
    @Mock private ListingPriceHistoryRepository listingPriceHistoryRepository;
    @Mock private MediaAssetRepository mediaAssetRepository;
    @Mock private FavoriteListingRepository favoriteListingRepository;
    @Mock private SavedListingRepository savedListingRepository;
    @Mock private UserRepository userRepository;
    @Mock private S3Service s3Service;
    @Mock private ListingMapper listingMapper;
    @Mock private ListingDetailsMapper listingDetailsMapper;
    @Mock private MediaAssetMapper mediaAssetMapper;

    @InjectMocks
    private ListingService listingService;

    private FirebaseToken mockToken;
    private User testUser;
    private UUID testListingId;

    @BeforeEach
    void setUp() {
        mockToken = mock(FirebaseToken.class);
        lenient().when(mockToken.getUid()).thenReturn(FIREBASE_UID);

        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setFirebaseId(FIREBASE_UID);

        testListingId = UUID.randomUUID();
    }

    // ========== createListing ==========

    @Test
    void createListing_withoutMedia_savesAllEntities() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(testUser));

        ListingCreateRequest request = buildCreateRequest(false);
        Listing listing = new Listing();
        listing.setListingId(testListingId);

        when(listingMapper.toListing(request)).thenReturn(listing);
        when(listingDetailsMapper.toListingDetails(request.getDetails())).thenReturn(new ListingDetails());
        when(listingDetailsMapper.toListingLocation(request.getLocation())).thenReturn(new ListingLocation());
        when(listingDetailsMapper.toDefaultCounters()).thenReturn(new ListingCounters());

        ApiResponse response = listingService.createListing(mockToken, request);

        assertThat(response.isSuccess()).isTrue();
        verify(listingRepository).save(listing);
        verify(listingDetailsRepository).save(any(ListingDetails.class));
        verify(listingLocationRepository).save(any(ListingLocation.class));
        verify(listingCountersRepository).save(any(ListingCounters.class));
        verify(mediaAssetRepository, never()).saveAll(anyList());
    }

    @Test
    void createListing_userNotFound_throws404() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.createListing(mockToken, buildCreateRequest(false)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    // ========== getListingById ==========

    @Test
    void getListingById_found_returnsResponseWithS3Urls() {
        Listing listing = buildListingWithRelations();
        when(listingRepository.findByListingId(testListingId)).thenReturn(Optional.of(listing));
        when(listingMediaRepository.findByListingListingIdOrderBySortOrderAsc(testListingId))
                .thenReturn(List.of());

        ListingResponse.Media media = new ListingResponse.Media();
        media.setUrl("listings/photo.jpg");
        ListingResponse expectedResponse = new ListingResponse();
        expectedResponse.setMedia(List.of(media));

        when(listingMapper.toListingResponse(any(), any(), any(), any(), anyList(), any(), any()))
                .thenReturn(expectedResponse);
        when(s3Service.generateDownloadUrl("listings/photo.jpg"))
                .thenReturn("https://s3.presigned/listings/photo.jpg");

        ListingResponse result = listingService.getListingById(testListingId);

        assertThat(result.getMedia().get(0).getUrl()).isEqualTo("https://s3.presigned/listings/photo.jpg");
    }

    @Test
    void getListingById_notFound_throws404() {
        when(listingRepository.findByListingId(testListingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.getListingById(testListingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== updateListing ==========

    @Test
    void updateListing_asOwner_succeeds() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(testUser));
        Listing listing = buildOwnedListing();
        when(listingRepository.findById(testListingId)).thenReturn(Optional.of(listing));

        ListingUpdateRequest request = new ListingUpdateRequest();
        request.setTitle("Updated Title");

        ApiResponse response = listingService.updateListing(mockToken, testListingId, request);

        assertThat(response.isSuccess()).isTrue();
        verify(listingRepository).save(listing);
    }

    @Test
    void updateListing_notOwner_throws403() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(testUser));

        Listing listing = new Listing();
        listing.setListingId(testListingId);
        User otherUser = new User();
        otherUser.setFirebaseId("other-uid");
        listing.setUser(otherUser);

        when(listingRepository.findById(testListingId)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.updateListing(mockToken, testListingId, new ListingUpdateRequest()))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void updateListing_priceChanged_createsPriceHistory() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(testUser));
        Listing listing = buildOwnedListing();
        listing.setPricing(new BigDecimal("500000"));
        when(listingRepository.findById(testListingId)).thenReturn(Optional.of(listing));

        ListingUpdateRequest request = new ListingUpdateRequest();
        request.setPricing(new BigDecimal("600000"));

        listingService.updateListing(mockToken, testListingId, request);

        verify(listingPriceHistoryRepository).save(argThat(history ->
                history.getOldPrice().equals(new BigDecimal("500000")) &&
                history.getNewPrice().equals(new BigDecimal("600000"))
        ));
    }

    // ========== deleteListing ==========

    @Test
    void deleteListing_setsArchivedStatusAndDeletedAt() {
        Listing listing = buildOwnedListing();
        when(listingRepository.findById(testListingId)).thenReturn(Optional.of(listing));

        ApiResponse response = listingService.deleteListing(mockToken, testListingId);

        assertThat(response.isSuccess()).isTrue();
        verify(listingRepository).save(argThat(l ->
                l.getPropertyStatus() == PropertyStatus.archived && l.getDeletedAt() != null
        ));
    }

    @Test
    void deleteListing_notOwner_throws403() {
        Listing listing = new Listing();
        listing.setListingId(testListingId);
        User otherUser = new User();
        otherUser.setFirebaseId("other-uid");
        listing.setUser(otherUser);
        when(listingRepository.findById(testListingId)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> listingService.deleteListing(mockToken, testListingId))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    // ========== getListings ==========

    @Test
    @SuppressWarnings("unchecked")
    void getListings_delegatesToSpecificationQuery() {
        Page<Listing> emptyPage = new PageImpl<>(List.of());
        when(listingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<ListingResponse> result = listingService.getListings(
                PropertyType.apartment, null, null, null, null, PageRequest.of(0, 20));

        assertThat(result.getContent()).isEmpty();
    }

    // ========== addFavorite / removeFavorite ==========

    @Test
    void addFavorite_incrementsCounter() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(testUser));
        Listing listing = buildOwnedListing();
        when(listingRepository.findById(testListingId)).thenReturn(Optional.of(listing));
        when(favoriteListingRepository.existsById(any(FavoriteListingId.class))).thenReturn(false);

        ListingCounters counters = new ListingCounters();
        counters.setFavoriteCount(5);
        when(listingCountersRepository.findById(testListingId)).thenReturn(Optional.of(counters));

        ApiResponse response = listingService.addFavorite(mockToken, testListingId);

        assertThat(response.isSuccess()).isTrue();
        verify(favoriteListingRepository).save(any(FavoriteListing.class));
        verify(listingCountersRepository).save(argThat(c -> c.getFavoriteCount() == 6));
    }

    @Test
    void addFavorite_alreadyFavorited_throws409() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(testUser));
        when(listingRepository.findById(testListingId)).thenReturn(Optional.of(buildOwnedListing()));
        when(favoriteListingRepository.existsById(any(FavoriteListingId.class))).thenReturn(true);

        assertThatThrownBy(() -> listingService.addFavorite(mockToken, testListingId))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void removeFavorite_decrementsCounterMinZero() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(testUser));
        when(favoriteListingRepository.existsById(any(FavoriteListingId.class))).thenReturn(true);

        ListingCounters counters = new ListingCounters();
        counters.setFavoriteCount(0);
        when(listingCountersRepository.findById(testListingId)).thenReturn(Optional.of(counters));

        listingService.removeFavorite(mockToken, testListingId);

        verify(favoriteListingRepository).deleteById(any(FavoriteListingId.class));
        verify(listingCountersRepository).save(argThat(c -> c.getFavoriteCount() == 0));
    }

    @Test
    void removeFavorite_notFavorited_throws404() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(testUser));
        when(favoriteListingRepository.existsById(any(FavoriteListingId.class))).thenReturn(false);

        assertThatThrownBy(() -> listingService.removeFavorite(mockToken, testListingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== saveListing / unsaveListing ==========

    @Test
    void saveListing_incrementsCounter() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(testUser));
        when(listingRepository.findById(testListingId)).thenReturn(Optional.of(buildOwnedListing()));
        when(savedListingRepository.existsById(any(SavedListingId.class))).thenReturn(false);

        ListingCounters counters = new ListingCounters();
        counters.setSaveCount(3);
        when(listingCountersRepository.findById(testListingId)).thenReturn(Optional.of(counters));

        ApiResponse response = listingService.saveListing(mockToken, testListingId);

        assertThat(response.isSuccess()).isTrue();
        verify(listingCountersRepository).save(argThat(c -> c.getSaveCount() == 4));
    }

    @Test
    void unsaveListing_notSaved_throws404() {
        when(userRepository.findByFirebaseId(FIREBASE_UID)).thenReturn(Optional.of(testUser));
        when(savedListingRepository.existsById(any(SavedListingId.class))).thenReturn(false);

        assertThatThrownBy(() -> listingService.unsaveListing(mockToken, testListingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== Helpers ==========

    private ListingCreateRequest buildCreateRequest(boolean withMedia) {
        ListingCreateRequest request = new ListingCreateRequest();
        request.setTitle("Test Listing");
        request.setPropertyType(PropertyType.apartment);
        request.setPricing(new BigDecimal("500000"));
        request.setIsPublishing(true);

        ListingCreateRequest.Details details = new ListingCreateRequest.Details();
        details.setYearBuilt(2020);
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

        if (withMedia) {
            ListingCreateRequest.MediaItem media = new ListingCreateRequest.MediaItem();
            media.setS3Location("listings/photo.jpg");
            media.setContentType("image/jpeg");
            media.setSortOrder(0);
            media.setByteSize(1024L);
            request.setMedias(List.of(media));
        }

        return request;
    }

    private Listing buildOwnedListing() {
        Listing listing = new Listing();
        listing.setListingId(testListingId);
        listing.setUser(testUser);
        listing.setPropertyStatus(PropertyStatus.published);
        return listing;
    }

    private Listing buildListingWithRelations() {
        Listing listing = buildOwnedListing();
        ReflectionTestUtils.setField(listing, "listingDetails", new ListingDetails());
        ReflectionTestUtils.setField(listing, "listingLocation", new ListingLocation());
        ReflectionTestUtils.setField(listing, "listingCounters", new ListingCounters());
        return listing;
    }
}
