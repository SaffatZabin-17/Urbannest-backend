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
import com.example.urbannest.specification.ListingSpecification;
import com.google.firebase.auth.FirebaseToken;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ListingService {
    private final ListingRepository listingRepository;
    private final ListingDetailsRepository listingDetailsRepository;
    private final ListingLocationRepository listingLocationRepository;
    private final ListingCountersRepository listingCountersRepository;
    private final ListingMediaRepository listingMediaRepository;
    private final ListingPriceHistoryRepository listingPriceHistoryRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final FavoriteListingRepository favoriteListingRepository;
    private final SavedListingRepository savedListingRepository;
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
                          FavoriteListingRepository favoriteListingRepository,
                          SavedListingRepository savedListingRepository,
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
        this.favoriteListingRepository = favoriteListingRepository;
        this.savedListingRepository = savedListingRepository;
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
        Listing listing = listingRepository.findByListingId(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing with id " + listingId + " not found"));
        return buildListingResponse(listing);
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
        Listing listing = resolveListing(listingId);
        verifyOwnership(listing, token);
        listing.setPropertyStatus(PropertyStatus.archived);
        listing.setDeletedAt(OffsetDateTime.now());
        listingRepository.save(listing);
        return new ApiResponse(true, "Listing deleted successfully");
    }

    public Page<ListingResponse> getListings(PropertyType propertyType,
                                             BigDecimal priceMin,
                                             BigDecimal priceMax,
                                             String district,
                                             Integer minBedrooms,
                                             Pageable pageable) {
        Specification<Listing> spec = ListingSpecification.withFilters(
                propertyType, priceMin, priceMax, district, minBedrooms);
        Page<Listing> page = listingRepository.findAll(spec, pageable);
        return buildListingResponsePage(page);
    }

    public Page<ListingResponse> getMyListings(FirebaseToken token, Pageable pageable) {
        User user = resolveUser(token);
        Page<Listing> page = listingRepository.findByUserAndDeletedAtIsNull(user, pageable);
        return buildListingResponsePage(page);
    }

    @Transactional
    public ApiResponse addFavorite(FirebaseToken token, UUID listingId) {
        User user = resolveUser(token);
        Listing listing = resolveListing(listingId);

        FavoriteListingId id = new FavoriteListingId();
        id.setUserId(user.getUserId());
        id.setListingId(listingId);

        if (favoriteListingRepository.existsById(id)) {
            throw new ResourceAlreadyExistsException("Listing is already in favorites");
        }

        FavoriteListing favorite = new FavoriteListing();
        favorite.setId(id);
        favorite.setUser(user);
        favorite.setListing(listing);
        favorite.setCreatedAt(OffsetDateTime.now());
        favoriteListingRepository.save(favorite);

        ListingCounters counters = listingCountersRepository.findById(listingId).orElse(null);
        if (counters != null) {
            counters.setFavoriteCount(counters.getFavoriteCount() + 1);
            listingCountersRepository.save(counters);
        }

        return new ApiResponse(true, "Listing added to favorites");
    }

    @Transactional
    public ApiResponse removeFavorite(FirebaseToken token, UUID listingId) {
        User user = resolveUser(token);

        FavoriteListingId id = new FavoriteListingId();
        id.setUserId(user.getUserId());
        id.setListingId(listingId);

        if (!favoriteListingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Listing is not in favorites");
        }

        favoriteListingRepository.deleteById(id);

        ListingCounters counters = listingCountersRepository.findById(listingId).orElse(null);
        if (counters != null) {
            counters.setFavoriteCount(Math.max(0, counters.getFavoriteCount() - 1));
            listingCountersRepository.save(counters);
        }

        return new ApiResponse(true, "Listing removed from favorites");
    }

    public Page<ListingResponse> getMyFavorites(FirebaseToken token, Pageable pageable) {
        User user = resolveUser(token);
        Page<FavoriteListing> favoritePage = favoriteListingRepository.findByUser(user, pageable);

        List<UUID> listingIds = favoritePage.getContent().stream()
                .map(fav -> fav.getListing().getListingId())
                .toList();

        Map<UUID, List<ListingMedia>> mediaByListing = batchFetchMedia(listingIds);

        return favoritePage.map(fav -> buildListingResponseFromLoaded(fav.getListing(), mediaByListing));
    }

    @Transactional
    public ApiResponse saveListing(FirebaseToken token, UUID listingId) {
        User user = resolveUser(token);
        Listing listing = resolveListing(listingId);

        SavedListingId id = new SavedListingId();
        id.setUserId(user.getUserId());
        id.setListingId(listingId);

        if (savedListingRepository.existsById(id)) {
            throw new ResourceAlreadyExistsException("Listing is already saved");
        }

        SavedListing saved = new SavedListing();
        saved.setId(id);
        saved.setUser(user);
        saved.setListing(listing);
        saved.setCreatedAt(OffsetDateTime.now());
        savedListingRepository.save(saved);

        ListingCounters counters = listingCountersRepository.findById(listingId).orElse(null);
        if (counters != null) {
            counters.setSaveCount(counters.getSaveCount() + 1);
            listingCountersRepository.save(counters);
        }

        return new ApiResponse(true, "Listing saved successfully");
    }

    @Transactional
    public ApiResponse unsaveListing(FirebaseToken token, UUID listingId) {
        User user = resolveUser(token);

        SavedListingId id = new SavedListingId();
        id.setUserId(user.getUserId());
        id.setListingId(listingId);

        if (!savedListingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Listing is not saved");
        }

        savedListingRepository.deleteById(id);

        ListingCounters counters = listingCountersRepository.findById(listingId).orElse(null);
        if (counters != null) {
            counters.setSaveCount(Math.max(0, counters.getSaveCount() - 1));
            listingCountersRepository.save(counters);
        }

        return new ApiResponse(true, "Listing unsaved successfully");
    }

    public Page<ListingResponse> getMySavedListings(FirebaseToken token, Pageable pageable) {
        User user = resolveUser(token);
        Page<SavedListing> savedPage = savedListingRepository.findByUser(user, pageable);

        List<UUID> listingIds = savedPage.getContent().stream()
                .map(saved -> saved.getListing().getListingId())
                .toList();

        Map<UUID, List<ListingMedia>> mediaByListing = batchFetchMedia(listingIds);

        return savedPage.map(saved -> buildListingResponseFromLoaded(saved.getListing(), mediaByListing));
    }

    // ======================== HELPERS ========================

    private ListingResponse buildListingResponse(Listing listing) {
        List<ListingMedia> mediaList = listingMediaRepository.findByListingListingIdOrderBySortOrderAsc(listing.getListingId());
        return mapListingToResponse(listing, mediaList);
    }

    private Page<ListingResponse> buildListingResponsePage(Page<Listing> page) {
        List<UUID> listingIds = page.getContent().stream()
                .map(Listing::getListingId)
                .toList();

        Map<UUID, List<ListingMedia>> mediaByListing = batchFetchMedia(listingIds);

        return page.map(listing -> buildListingResponseFromLoaded(listing, mediaByListing));
    }

    private ListingResponse buildListingResponseFromLoaded(Listing listing, Map<UUID, List<ListingMedia>> mediaByListing) {
        List<ListingMedia> mediaList = mediaByListing.getOrDefault(listing.getListingId(), List.of());
        return mapListingToResponse(listing, mediaList);
    }

    private ListingResponse mapListingToResponse(Listing listing, List<ListingMedia> mediaList) {
        ListingResponse response = listingMapper.toListingResponse(
                listing,
                listing.getListingDetails(),
                listing.getListingLocation(),
                listing.getListingCounters(),
                mediaList,
                listingDetailsMapper,
                mediaAssetMapper);

        response.getMedia().forEach(media ->
                media.setUrl(s3Service.generateDownloadUrl(media.getUrl())));

        return response;
    }

    private Map<UUID, List<ListingMedia>> batchFetchMedia(List<UUID> listingIds) {
        if (listingIds.isEmpty()) {
            return Map.of();
        }
        return listingMediaRepository.findByListingListingIdInOrderBySortOrderAsc(listingIds)
                .stream()
                .collect(Collectors.groupingBy(m -> m.getListing().getListingId()));
    }

    private User resolveUser(FirebaseToken token) {
        return userRepository.findByFirebaseId(token.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Listing resolveListing(UUID listingId) {
        return listingRepository.findById(listingId).orElseThrow(() -> new ResourceNotFoundException("Listing with id " + listingId + " not found"));
    }

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
