package com.example.urbannest.mapper;

import com.example.urbannest.dto.Requests.ListingCreateRequest;
import com.example.urbannest.dto.Requests.ListingUpdateRequest;
import com.example.urbannest.dto.Responses.ListingResponse;
import com.example.urbannest.model.Listing;
import com.example.urbannest.model.ListingMedia;
import com.example.urbannest.model.MediaAsset;
import com.example.urbannest.model.composite.ListingMediaId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", imports = OffsetDateTime.class)
public interface MediaAssetMapper {

    @Mapping(target = "mediaId", ignore = true)
    @Mapping(target = "ownerUser", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "createdAt", expression = "java(OffsetDateTime.now())")
    MediaAsset toMediaAsset(ListingCreateRequest.MediaItem item);

    @Mapping(target = "mediaId", ignore = true)
    @Mapping(target = "ownerUser", ignore = true)
    @Mapping(target = "caption", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "byteSize", constant = "0L")
    @Mapping(target = "createdAt", expression = "java(OffsetDateTime.now())")
    MediaAsset toMediaAssetFromUpdate(ListingUpdateRequest.MediaItem item);

    List<MediaAsset> toMediaAssetList(List<ListingCreateRequest.MediaItem> items);

    List<MediaAsset> toMediaAssetListFromUpdate(List<ListingUpdateRequest.MediaItem> items);

    @Mapping(target = "mediaId", source = "mediaAsset.mediaId")
    @Mapping(target = "url", source = "mediaAsset.s3Location")
    @Mapping(target = "contentType", source = "mediaAsset.contentType")
    @Mapping(target = "sortOrder", source = "sortOrder")
    ListingResponse.Media toResponseMedia(ListingMedia listingMedia);

    List<ListingResponse.Media> toResponseMediaList(List<ListingMedia> mediaList);

    default ListingMedia toListingMedia(Listing listing, MediaAsset asset, Integer sortOrder) {
        ListingMedia listingMedia = new ListingMedia();
        ListingMediaId mediaId = new ListingMediaId();
        mediaId.setListingId(listing.getListingId());
        mediaId.setMediaId(asset.getMediaId());
        listingMedia.setId(mediaId);
        listingMedia.setListing(listing);
        listingMedia.setMediaAsset(asset);
        listingMedia.setSortOrder(sortOrder);
        return listingMedia;
    }

    default <T> List<ListingMedia> toListingMediaList(List<MediaAsset> assets, List<T> items, Listing listing,
                                                       java.util.function.Function<T, Integer> sortOrderExtractor) {
        List<ListingMedia> result = new ArrayList<>();
        for (int i = 0; i < assets.size(); i++) {
            result.add(toListingMedia(listing, assets.get(i), sortOrderExtractor.apply(items.get(i))));
        }
        return result;
    }
}
