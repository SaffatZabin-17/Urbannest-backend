package com.example.urbannest.repository;

import com.example.urbannest.model.FavoriteListing;
import com.example.urbannest.model.User;
import com.example.urbannest.model.composite.FavoriteListingId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteListingRepository extends JpaRepository<FavoriteListing, FavoriteListingId> {

    @EntityGraph(attributePaths = {
            "listing",
            "listing.user",
            "listing.listingDetails",
            "listing.listingLocation",
            "listing.listingCounters"
    })
    Page<FavoriteListing> findByUser(User user, Pageable pageable);
}
