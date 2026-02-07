package com.example.urbannest.model.composite;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter
@EqualsAndHashCode
public class ListingMediaId implements Serializable {

    @Column(name = "listing_id")
    private UUID listingId;

    @Column(name = "media_id")
    private UUID mediaId;
}