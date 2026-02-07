package com.example.urbannest.model;

import com.example.urbannest.model.enums.FacingDirection;
import com.example.urbannest.model.enums.ListingCondition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "listing_details")
@Getter @Setter
public class ListingDetails {

    @Id
    @Column(name = "listing_id")
    private UUID listingId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @Column(name = "year_built", nullable = false)
    private Integer yearBuilt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "listing_condition", nullable = false)
    private ListingCondition listingCondition;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "facing_direction")
    private FacingDirection facingDirection;

    @Column(name = "bedrooms_count", nullable = false)
    private Integer bedroomsCount;

    @Column(name = "bathrooms_count", nullable = false)
    private Integer bathroomsCount;

    @Column(name = "balconies_count", nullable = false)
    private Integer balconiesCount;

    @Column(name = "floor_level")
    private Integer floorLevel;

    @Column(name = "furnished")
    private Boolean furnished;

    @Column(name = "parking_area")
    private Integer parkingArea;

    @Column(name = "pet_friendly")
    private Boolean petFriendly;

    @Column(name = "lot_area")
    private Integer lotArea;

    @Column(name = "living_area", nullable = false)
    private Integer livingArea;
}