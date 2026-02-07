package com.example.urbannest.model;

import com.example.urbannest.model.composite.ListingMediaId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "listing_media")
@Getter @Setter
public class ListingMedia {

    @EmbeddedId
    private ListingMediaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("listingId")
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mediaId")
    @JoinColumn(name = "media_id")
    private MediaAsset mediaAsset;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}