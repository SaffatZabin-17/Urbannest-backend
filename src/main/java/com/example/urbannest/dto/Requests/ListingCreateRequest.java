package com.example.urbannest.dto.Requests;

import com.example.urbannest.model.enums.FacingDirection;
import com.example.urbannest.model.enums.ListingCondition;
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
public class ListingCreateRequest {

    @NotBlank(message = "Property listing title is required")
    private String title;

    private String description;

    @NotNull(message = "Property type must be defined")
    private PropertyType propertyType;

    @NotNull(message = "Property must have an asking price")
    private BigDecimal pricing;

    @NotNull(message = "Property details are required")
    @Valid
    private Details details;

    @NotNull(message = "Property location is required")
    @Valid
    private Location location;

    private List<@Valid MediaItem> medias;

    @NotNull(message = "Must choose between draft or publishing")
    private Boolean isPublishing;

    @Getter
    @Setter
    public static class Details {
        @NotNull(message = "Year built is required")
        private Integer yearBuilt;

        @NotNull(message = "Listing condition is required")
        private ListingCondition listingCondition;

        private FacingDirection facingDirection;

        @NotNull(message = "Bedrooms count is required")
        private Integer bedroomsCount;

        @NotNull(message = "Bathrooms count is required")
        private Integer bathroomsCount;

        @NotNull(message = "Balconies count is required")
        private Integer balconiesCount;

        private Integer floorLevel;
        private Boolean furnished;
        private Integer parkingArea;
        private Boolean petFriendly;
        private Integer lotArea;

        @NotNull(message = "Living area is required")
        private Integer livingArea;
    }

    @Getter
    @Setter
    public static class Location {
        @NotBlank(message = "Address line is required")
        private String addressLine;

        @NotBlank(message = "Area is required")
        private String area;

        @NotBlank(message = "District is required")
        private String district;

        @NotBlank(message = "Zip code is required")
        private String zipCode;

        @NotNull(message = "Latitude is required")
        private BigDecimal latitude;

        @NotNull(message = "Longitude is required")
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

        @NotNull
        private Long byteSize;

        private String caption;
    }
}