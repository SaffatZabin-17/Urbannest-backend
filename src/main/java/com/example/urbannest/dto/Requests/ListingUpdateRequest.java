package com.example.urbannest.dto.Requests;

import com.example.urbannest.model.enums.FacingDirection;
import com.example.urbannest.model.enums.ListingCondition;
import com.example.urbannest.model.enums.PropertyStatus;
import com.example.urbannest.model.enums.PropertyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ListingUpdateRequest {

    private String title;
    private String description;
    private PropertyType propertyType;
    private BigDecimal pricing;
    private PropertyStatus propertyStatus;

    @Valid
    private Details details;

    @Valid
    private Location location;

    private List<@Valid MediaItem> mediaItems;

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
    public static class MediaItem {
        @NotBlank(message = "s3 location is required")
        private String s3Location;
        @NotBlank(message = "Mime type is required")
        private String contentType;
        @NotNull(message = "Sort order is required")
        private Integer sortOrder;
    }
}