package com.example.urbannest.dto.Requests;

import com.example.urbannest.model.enums.FacingDirection;
import com.example.urbannest.model.enums.ListingCondition;
import com.example.urbannest.model.enums.PropertyStatus;
import com.example.urbannest.model.enums.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Request body for partially updating a listing. Only provided fields are updated.")
@Getter
@Setter
public class ListingUpdateRequest {

    private String title;
    private String description;
    private PropertyType propertyType;

    @Schema(description = "New asking price in BDT. Triggers price history tracking if changed.")
    private BigDecimal pricing;

    @Schema(description = "Change listing status (e.g. draft to published)")
    private PropertyStatus propertyStatus;

    @Valid
    private Details details;

    @Valid
    private Location location;

    @Schema(description = "New or additional media attachments")
    private List<@Valid MediaItem> mediaItems;

    @Schema(description = "Partial update for property details")
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

    @Schema(description = "Partial update for property location")
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

    @Schema(description = "Media attachment for update")
    @Getter
    @Setter
    public static class MediaItem {
        @Schema(description = "S3 object key", example = "listings/550e8400/photo.jpg")
        @NotBlank(message = "s3 location is required")
        private String s3Location;

        @Schema(description = "MIME type", example = "image/jpeg")
        @NotBlank(message = "Mime type is required")
        private String contentType;

        @Schema(description = "Display order (0-based)")
        @NotNull(message = "Sort order is required")
        private Integer sortOrder;
    }
}