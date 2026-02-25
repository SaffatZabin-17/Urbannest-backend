package com.example.urbannest.dto.Responses;

import com.example.urbannest.model.enums.FacingDirection;
import com.example.urbannest.model.enums.ListingCondition;
import com.example.urbannest.model.enums.PropertyStatus;
import com.example.urbannest.model.enums.PropertyType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ListingResponse {

    private UUID listingId;
    private String title;
    private String description;
    private PropertyType propertyType;
    private PropertyStatus propertyStatus;
    private BigDecimal pricing;

    private Owner owner;
    private Details details;
    private Location location;
    private Counters counters;
    private List<Media> media;

    private OffsetDateTime createdAt;
    private OffsetDateTime publishedAt;
    private OffsetDateTime updatedAt;

    @Getter
    @Setter
    public static class Owner {
        private UUID userId;
        private String name;
        private String profilePictureUrl;
    }

    @Getter
    @Setter
    public static class Details {
        private Integer yearBuilt;
        private ListingCondition listingCondition;
        private FacingDirection facingDirection;
        private Integer bedroomsCount;
        private Integer bathroomsCount;
        private Integer balconiesCount;
        private Integer floorLevel;
        private Boolean furnished;
        private Integer parkingArea;
        private Boolean petFriendly;
        private Integer lotArea;
        private Integer livingArea;
    }

    @Getter
    @Setter
    public static class Location {
        private String addressLine;
        private String area;
        private String district;
        private String zipCode;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @Getter
    @Setter
    public static class Counters {
        private Integer viewCount;
        private Integer favoriteCount;
        private Integer saveCount;
    }

    @Getter
    @Setter
    public static class Media {
        private UUID mediaId;
        private String url;
        private String contentType;
        private Integer sortOrder;
    }
}