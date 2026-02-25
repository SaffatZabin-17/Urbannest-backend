package com.example.urbannest.repository;

import com.example.urbannest.model.ListingMedia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ListingMediaRepository extends JpaRepository<ListingMedia, UUID> {

    List<ListingMedia> findByListingListingIdOrderBySortOrderAsc(UUID listingId);

    @EntityGraph(attributePaths = {"mediaAsset"})
    List<ListingMedia> findByListingListingIdInOrderBySortOrderAsc(Collection<UUID> listingIds);
}