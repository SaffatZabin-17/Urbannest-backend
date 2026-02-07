package com.example.urbannest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "listing_locations")
@Getter @Setter
public class ListingLocation {

    @Id
    @Column(name = "listing_id")
    private UUID listingId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @Column(name = "address_line", nullable = false)
    private String addressLine;

    @Column(name = "area", nullable = false)
    private String area;

    @Column(name = "district", nullable = false)
    private String district;

    @Column(name = "zip_code", nullable = false, length = 10)
    private String zipCode;

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;
}