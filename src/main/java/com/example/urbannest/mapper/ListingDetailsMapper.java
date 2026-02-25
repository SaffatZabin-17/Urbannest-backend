package com.example.urbannest.mapper;

import com.example.urbannest.dto.Requests.ListingCreateRequest;
import com.example.urbannest.dto.Requests.ListingUpdateRequest;
import com.example.urbannest.dto.Responses.ListingResponse;
import com.example.urbannest.model.ListingCounters;
import com.example.urbannest.model.ListingDetails;
import com.example.urbannest.model.ListingLocation;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ListingDetailsMapper {

    @Mapping(target = "listing", ignore = true)
    @Mapping(target = "listingId", ignore = true)
    ListingDetails toListingDetails(ListingCreateRequest.Details details);

    @Mapping(target = "listing", ignore = true)
    @Mapping(target = "listingId", ignore = true)
    ListingLocation toListingLocation(ListingCreateRequest.Location location);

    default ListingCounters toDefaultCounters() {
        ListingCounters counters = new ListingCounters();
        counters.setViewCount(0);
        counters.setFavoriteCount(0);
        counters.setSaveCount(0);
        return counters;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "listing", ignore = true)
    @Mapping(target = "listingId", ignore = true)
    void updateDetailsFromRequest(ListingUpdateRequest.Details source, @MappingTarget ListingDetails target);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "listing", ignore = true)
    @Mapping(target = "listingId", ignore = true)
    void updateLocationFromRequest(ListingUpdateRequest.Location source, @MappingTarget ListingLocation target);

    ListingResponse.Details toResponseDetails(ListingDetails details);

    ListingResponse.Location toResponseLocation(ListingLocation location);

    ListingResponse.Counters toResponseCounters(ListingCounters counters);
}
