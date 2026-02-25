package com.example.urbannest.dto.Responses;

import com.example.urbannest.model.enums.FacingDirection;
import com.example.urbannest.model.enums.ListingCondition;
import com.example.urbannest.model.enums.PropertyStatus;
import com.example.urbannest.model.enums.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Full listing response with owner, property details, location, counters, and media")
@Getter
@Setter
public class ListingResponse {

    @Schema(description = "Unique listing identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID listingId;

    @Schema(description = "Listing title", example = "Modern 3BR Apartment in Gulshan")
    private String title;

    @Schema(description = "Detailed description of the property")
    private String description;

    private PropertyType propertyType;
    private PropertyStatus propertyStatus;

    @Schema(description = "Asking price in BDT", example = "4500000.00")
    private BigDecimal pricing;

    private Owner owner;
    private Details details;
    private Location location;
    private Counters counters;
    private List<Media> media;

    private OffsetDateTime createdAt;
    private OffsetDateTime publishedAt;
    private OffsetDateTime updatedAt;

    @Schema(description = "Listing owner information")
    @Getter
    @Setter
    public static class Owner {
        private UUID userId;
        private String name;
        @Schema(description = "S3 pre-signed URL for profile picture")
        private String profilePictureUrl;
    }

    @Schema(description = "Property details and specifications")
    @Getter
    @Setter
    public static class Details {
        @Schema(example = "2020")
        private Integer yearBuilt;
        private ListingCondition listingCondition;
        private FacingDirection facingDirection;
        private Integer bedroomsCount;
        private Integer bathroomsCount;
        private Integer balconiesCount;
        private Integer floorLevel;
        private Boolean furnished;
        @Schema(description = "Number of parking spaces")
        private Integer parkingArea;
        private Boolean petFriendly;
        @Schema(description = "Total lot area in sqft")
        private Integer lotArea;
        @Schema(description = "Living area in sqft")
        private Integer livingArea;
    }

    @Schema(description = "Property geographic location")
    @Getter
    @Setter
    public static class Location {
        @Schema(example = "House 12, Road 5, Block F")
        private String addressLine;
        @Schema(example = "Gulshan 2")
        private String area;
        @Schema(example = "Dhaka")
        private String district;
        @Schema(example = "1212")
        private String zipCode;
        @Schema(example = "23.780000")
        private BigDecimal latitude;
        @Schema(example = "90.420000")
        private BigDecimal longitude;
    }

    @Schema(description = "Listing engagement counters")
    @Getter
    @Setter
    public static class Counters {
        private Integer viewCount;
        private Integer favoriteCount;
        private Integer saveCount;
    }

    @Schema(description = "Listing media attachment")
    @Getter
    @Setter
    public static class Media {
        private UUID mediaId;
        @Schema(description = "S3 pre-signed download URL")
        private String url;
        @Schema(description = "MIME type", example = "image/jpeg")
        private String contentType;
        @Schema(description = "Display order (0-based)")
        private Integer sortOrder;
    }
}