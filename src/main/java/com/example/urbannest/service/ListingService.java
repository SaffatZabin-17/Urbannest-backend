package com.example.urbannest.service;

import com.example.urbannest.dto.Requests.ListingCreateRequest;
import com.example.urbannest.dto.Requests.ListingUpdateRequest;
import com.example.urbannest.dto.Responses.ApiResponse;
import com.example.urbannest.dto.Responses.ListingResponse;
import com.example.urbannest.exception.ResourceNotFoundException;
import com.example.urbannest.exception.UnauthorizedAccessException;
import com.example.urbannest.mapper.ListingDetailsMapper;
import com.example.urbannest.mapper.ListingMapper;
import com.example.urbannest.mapper.MediaAssetMapper;
import com.example.urbannest.model.*;
import com.example.urbannest.model.enums.PropertyStatus;
import com.example.urbannest.repository.ListingCountersRepository;
import com.example.urbannest.repository.ListingDetailsRepository;
import com.example.urbannest.repository.ListingLocationRepository;
import com.example.urbannest.repository.ListingMediaRepository;
import com.example.urbannest.repository.ListingPriceHistoryRepository;
import com.example.urbannest.repository.ListingRepository;
import com.example.urbannest.repository.MediaAssetRepository;
import com.example.urbannest.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ListingService {
    private final ListingRepository listingRepository;
    private final ListingDetailsRepository listingDetailsRepository;
    private final ListingLocationRepository listingLocationRepository;
    private final ListingCountersRepository listingCountersRepository;
    private final ListingMediaRepository listingMediaRepository;
    private final ListingPriceHistoryRepository listingPriceHistoryRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final ListingMapper listingMapper;
    private final ListingDetailsMapper listingDetailsMapper;
    private final MediaAssetMapper mediaAssetMapper;

    public ListingService(ListingRepository listingRepository,
                          ListingDetailsRepository listingDetailsRepository,
                          ListingLocationRepository listingLocationRepository,
                          ListingCountersRepository listingCountersRepository,
                          ListingMediaRepository listingMediaRepository,
                          ListingPriceHistoryRepository listingPriceHistoryRepository,
                          MediaAssetRepository mediaAssetRepository,
                          UserRepository userRepository,
                          S3Service s3Service,
                          ListingMapper listingMapper,
                          ListingDetailsMapper listingDetailsMapper,
                          MediaAssetMapper mediaAssetMapper) {
        this.listingRepository = listingRepository;
        this.listingDetailsRepository = listingDetailsRepository;
        this.listingLocationRepository = listingLocationRepository;
        this.listingCountersRepository = listingCountersRepository;
        this.listingMediaRepository = listingMediaRepository;
        this.listingPriceHistoryRepository = listingPriceHistoryRepository;
        this.mediaAssetRepository = mediaAssetRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
        this.listingMapper = listingMapper;
        this.listingDetailsMapper = listingDetailsMapper;
        this.mediaAssetMapper = mediaAssetMapper;
    }

    @Transactional
    public ApiResponse createListing(FirebaseToken token, ListingCreateRequest request) {
        User user = resolveUser(token);

        Listing listing = listingMapper.toListing(request);
        listing.setUser(user);
        listingRepository.save(listing);

        ListingDetails details = listingDetailsMapper.toListingDetails(request.getDetails());
        details.setListing(listing);
        listingDetailsRepository.save(details);

        ListingLocation location = listingDetailsMapper.toListingLocation(request.getLocation());
        location.setListing(listing);
        listingLocationRepository.save(location);

        ListingCounters counters = listingDetailsMapper.toDefaultCounters();
        counters.setListing(listing);
        listingCountersRepository.save(counters);

        if (request.getMedias() != null && !request.getMedias().isEmpty()) {
            List<MediaAsset> assets = mediaAssetMapper.toMediaAssetList(request.getMedias());
            for (MediaAsset mediaAsset : assets) {
                mediaAsset.setOwnerUser(user);
            }
            mediaAssetRepository.saveAll(assets);

            List<ListingMedia> listingMediaList = mediaAssetMapper.toListingMediaList(
                    assets, request.getMedias(), listing, ListingCreateRequest.MediaItem::getSortOrder);
            listingMediaRepository.saveAll(listingMediaList);
        }

        return new ApiResponse(true, "Listing created successfully");
    }

    public ListingResponse getListingById(UUID listingId) {
        Listing listing = resolveListing(listingId);

        ListingDetails details = listingDetailsRepository.findById(listingId).orElse(null);
        ListingLocation location = listingLocationRepository.findById(listingId).orElse(null);
        ListingCounters counters = listingCountersRepository.findById(listingId).orElse(null);
        List<ListingMedia> mediaList = listingMediaRepository.findByListingListingIdOrderBySortOrderAsc(listingId);

        ListingResponse response = listingMapper.toListingResponse(
                listing, details, location, counters, mediaList, listingDetailsMapper, mediaAssetMapper);

        response.getMedia().forEach(media -> {
            media.setUrl(s3Service.generateDownloadUrl(media.getUrl()));
        });

        return response;
    }


    @Transactional
    public ApiResponse updateListing(FirebaseToken token, UUID listingId, ListingUpdateRequest request) {
        User user = resolveUser(token);
        Listing listing = resolveListing(listingId);
        verifyOwnership(listing, token);

        listingMapper.updateListingFromRequest(request, listing);

        ListingPriceHistory priceHistory = createPriceHistoryIfChanged(listing, request);
        if (priceHistory != null) {
            listingPriceHistoryRepository.save(priceHistory);
        }

        listingRepository.save(listing);

        if (request.getDetails() != null) {
            ListingDetails details = listingDetailsRepository.findById(listingId).orElse(null);
            if (details != null) {
                listingDetailsMapper.updateDetailsFromRequest(request.getDetails(), details);
                listingDetailsRepository.save(details);
            }
        }

        if (request.getLocation() != null) {
            ListingLocation location = listingLocationRepository.findById(listingId).orElse(null);
            if (location != null) {
                listingDetailsMapper.updateLocationFromRequest(request.getLocation(), location);
                listingLocationRepository.save(location);
            }
        }

        if (request.getMediaItems() != null && !request.getMediaItems().isEmpty()) {
            List<MediaAsset> assets = mediaAssetMapper.toMediaAssetListFromUpdate(request.getMediaItems());
            for (MediaAsset asset : assets) {
                asset.setOwnerUser(user);
            }
            mediaAssetRepository.saveAll(assets);

            List<ListingUpdateRequest.MediaItem> items = request.getMediaItems();
            for (int i = 0; i < assets.size(); i++) {
                ListingMedia listingMedia = mediaAssetMapper.toListingMedia(listing, assets.get(i), items.get(i).getSortOrder());
                listingMediaRepository.save(listingMedia);
            }
        }

        return new ApiResponse(true, "Listing updated successfully");
    }

    @Transactional
    public ApiResponse deleteListing(FirebaseToken token, UUID listingId) {
        Listing listing = listingRepository.findById(listingId).orElse(null);
        assert listing != null;
        verifyOwnership(listing, token);
        listing.setPropertyStatus(PropertyStatus.archived);
        listing.setDeletedAt(OffsetDateTime.now());
        listingRepository.save(listing);
        return new ApiResponse(true, "Listing deleted successfully");
    }

    @Transactional
    public ApiResponse publishListing(FirebaseToken token, UUID listingId) {
        Listing listing = listingRepository.findById(listingId).orElse(null);
        assert listing != null;
        verifyOwnership(listing, token);
        listing.setPropertyStatus(PropertyStatus.published);
        listingRepository.save(listing);
        return new ApiResponse(true, "Listing published successfully");
    }

    public ApiResponse markAsSold(FirebaseToken token, UUID listingId) {
        Listing listing = listingRepository.findById(listingId).orElse(null);
        assert listing != null;
        verifyOwnership(listing, token);
        listing.setPropertyStatus(PropertyStatus.sold);
        listingRepository.save(listing);
        return new ApiResponse(true, "Marked as sold successfully");
    }

    /** Public listing search with filters + pagination */
    public Page<Listing> getListings(Pageable pageable) {
        // TODO: add filter params (propertyType, priceMin, priceMax, district, bedrooms, etc.)
        return null;
    }

    /** Get all listings owned by the authenticated user */
    public Page<Listing> getMyListings(FirebaseToken token, Pageable pageable) {
        // TODO: implement
        return null;
    }

    // ======================== FAVORITES ========================

    /** Add a listing to user's favorites */
    public ApiResponse addFavorite(FirebaseToken token, UUID listingId) {
        // TODO: implement — increment listing favorite count
        return null;
    }

    /** Remove a listing from user's favorites */
    public ApiResponse removeFavorite(FirebaseToken token, UUID listingId) {
        // TODO: implement — decrement listing favorite count
        return null;
    }

    /** Get all favorite listings for the authenticated user */
    public Page<Listing> getMyFavorites(FirebaseToken token, Pageable pageable) {
        // TODO: implement
        return null;
    }

    // ======================== SAVED ========================

    /** Save a listing for later */
    public ApiResponse saveListing(FirebaseToken token, UUID listingId) {
        // TODO: implement — increment listing save count
        return null;
    }

    /** Remove a saved listing */
    public ApiResponse unsaveListing(FirebaseToken token, UUID listingId) {
        // TODO: implement — decrement listing save count
        return null;
    }

    /** Get all saved listings for the authenticated user */
    public Page<Listing> getMySavedListings(FirebaseToken token, Pageable pageable) {
        // TODO: implement
        return null;
    }

    private User resolveUser(FirebaseToken token) {
        return userRepository.findByFirebaseId(token.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Listing resolveListing(UUID listingId) {
        return listingRepository.findById(listingId).orElseThrow(() -> new ResourceNotFoundException("Listing with id " + listingId + " not found"));
    }

    /** Verify the authenticated user owns the listing, throw 403 if not */
    private void verifyOwnership(Listing listing, FirebaseToken token) throws UnauthorizedAccessException {
        if(!Objects.equals(listing.getUser().getFirebaseId(), token.getUid())) {
            throw new UnauthorizedAccessException("You are not allowed to perform this operation");
        }
    }

    private ListingPriceHistory createPriceHistoryIfChanged(Listing listing, ListingUpdateRequest request) {
        if (request.getPricing() != null && !request.getPricing().equals(listing.getPricing())) {
            ListingPriceHistory priceHistory = new ListingPriceHistory();
            priceHistory.setListing(listing);
            priceHistory.setOldPrice(listing.getPricing());
            priceHistory.setNewPrice(request.getPricing());
            priceHistory.setChangedAt(OffsetDateTime.now());
            listing.setPricing(request.getPricing());
            return priceHistory;
        }
        return null;
    }
}
