package com.example.urbannest.model;

import com.example.urbannest.model.composite.FavoriteListingId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "favorite_listings")
@Getter @Setter
public class FavoriteListing {

    @EmbeddedId
    private FavoriteListingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("listingId")
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}