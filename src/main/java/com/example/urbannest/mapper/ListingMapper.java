package com.example.urbannest.mapper;

import com.example.urbannest.dto.Requests.ListingCreateRequest;
import com.example.urbannest.dto.Requests.ListingUpdateRequest;
import com.example.urbannest.dto.Responses.ListingResponse;
import com.example.urbannest.model.*;
import com.example.urbannest.model.enums.PropertyStatus;
import org.mapstruct.*;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper(componentModel = "spring",
        uses = {ListingDetailsMapper.class, MediaAssetMapper.class},
        imports = {OffsetDateTime.class, PropertyStatus.class})
public interface ListingMapper {

    @Mapping(target = "listingId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "propertyStatus", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "listingDetails", ignore = true)
    @Mapping(target = "listingLocation", ignore = true)
    @Mapping(target = "listingCounters", ignore = true)
    @Mapping(target = "createdAt", expression = "java(OffsetDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(OffsetDateTime.now())")
    Listing toListing(ListingCreateRequest request);

    @AfterMapping
    default void setPublishingStatus(ListingCreateRequest request, @MappingTarget Listing listing) {
        if (Boolean.TRUE.equals(request.getIsPublishing())) {
            listing.setPropertyStatus(PropertyStatus.published);
            listing.setPublishedAt(OffsetDateTime.now());
        } else {
            listing.setPropertyStatus(PropertyStatus.draft);
        }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "listingId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "listingDetails", ignore = true)
    @Mapping(target = "listingLocation", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "pricing", ignore = true)
    @Mapping(target = "listingCounters", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(OffsetDateTime.now())")
    void updateListingFromRequest(ListingUpdateRequest request, @MappingTarget Listing listing);

    @AfterMapping
    default void setPublishedAtOnUpdate(ListingUpdateRequest request, @MappingTarget Listing listing) {
        if (request.getPropertyStatus() == PropertyStatus.published && listing.getPublishedAt() == null) {
            listing.setPublishedAt(OffsetDateTime.now());
        }
    }

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "counters", ignore = true)
    @Mapping(target = "media", ignore = true)
    ListingResponse toListingResponseFromListing(Listing listing);

    ListingResponse.Owner toOwner(User user);

    default ListingResponse toListingResponse(Listing listing,
                                               ListingDetails details,
                                               ListingLocation location,
                                               ListingCounters counters,
                                               List<ListingMedia> medias,
                                               ListingDetailsMapper detailsMapper,
                                               MediaAssetMapper mediaAssetMapper) {
        ListingResponse response = toListingResponseFromListing(listing);
        response.setOwner(toOwner(listing.getUser()));

        if (details != null) {
            response.setDetails(detailsMapper.toResponseDetails(details));
        }
        if (location != null) {
            response.setLocation(detailsMapper.toResponseLocation(location));
        }
        if (counters != null) {
            response.setCounters(detailsMapper.toResponseCounters(counters));
        }

        response.setMedia(mediaAssetMapper.toResponseMediaList(medias));

        return response;
    }
}
