package com.example.urbannest.repository;

import com.example.urbannest.model.SavedListing;
import com.example.urbannest.model.User;
import com.example.urbannest.model.composite.SavedListingId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedListingRepository extends JpaRepository<SavedListing, SavedListingId> {

    @EntityGraph(attributePaths = {
            "listing",
            "listing.user",
            "listing.listingDetails",
            "listing.listingLocation",
            "listing.listingCounters"
    })
    Page<SavedListing> findByUser(User user, Pageable pageable);
}
