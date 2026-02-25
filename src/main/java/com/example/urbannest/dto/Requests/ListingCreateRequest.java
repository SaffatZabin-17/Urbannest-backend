package com.example.urbannest.dto.Requests;

import com.example.urbannest.model.enums.FacingDirection;
import com.example.urbannest.model.enums.ListingCondition;
import com.example.urbannest.model.enums.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Request body for creating a new property listing")
@Getter
@Setter
public class ListingCreateRequest {

    @Schema(description = "Listing title", example = "Modern 3BR Apartment in Gulshan")
    @NotBlank(message = "Property listing title is required")
    private String title;

    @Schema(description = "Detailed description of the property")
    private String description;

    @NotNull(message = "Property type must be defined")
    private PropertyType propertyType;

    @Schema(description = "Asking price in BDT", example = "4500000.00")
    @NotNull(message = "Property must have an asking price")
    private BigDecimal pricing;

    @NotNull(message = "Property details are required")
    @Valid
    private Details details;

    @NotNull(message = "Property location is required")
    @Valid
    private Location location;

    @Schema(description = "List of media attachments (images/videos)")
    private List<@Valid MediaItem> medias;

    @Schema(description = "If true, listing is published immediately; if false, saved as draft")
    @NotNull(message = "Must choose between draft or publishing")
    private Boolean isPublishing;

    @Schema(description = "Property detail specifications")
    @Getter
    @Setter
    public static class Details {
        @Schema(example = "2020")
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
        @Schema(description = "Number of parking spaces")
        private Integer parkingArea;
        private Boolean petFriendly;
        @Schema(description = "Total lot area in sqft")
        private Integer lotArea;

        @Schema(description = "Living area in sqft")
        @NotNull(message = "Living area is required")
        private Integer livingArea;
    }

    @Schema(description = "Property geographic location")
    @Getter
    @Setter
    public static class Location {
        @Schema(example = "House 12, Road 5, Block F")
        @NotBlank(message = "Address line is required")
        private String addressLine;

        @Schema(example = "Gulshan 2")
        @NotBlank(message = "Area is required")
        private String area;

        @Schema(example = "Dhaka")
        @NotBlank(message = "District is required")
        private String district;

        @Schema(example = "1212")
        @NotBlank(message = "Zip code is required")
        private String zipCode;

        @Schema(example = "23.780000")
        @NotNull(message = "Latitude is required")
        private BigDecimal latitude;

        @Schema(example = "90.420000")
        @NotNull(message = "Longitude is required")
        private BigDecimal longitude;
    }

    @Schema(description = "Media attachment for the listing")
    @Getter
    @Setter
    public static class MediaItem {
        @Schema(description = "S3 object key returned from the upload endpoint", example = "listings/550e8400/photo.jpg")
        @NotBlank(message = "s3 location is required")
        private String s3Location;

        @Schema(description = "MIME type", example = "image/jpeg")
        @NotBlank(message = "Mime type is required")
        private String contentType;

        @Schema(description = "Display order (0-based)", example = "0")
        @NotNull(message = "Sort order is required")
        private Integer sortOrder;

        @Schema(description = "File size in bytes")
        @NotNull
        private Long byteSize;

        @Schema(description = "Optional caption for the media")
        private String caption;
    }
}