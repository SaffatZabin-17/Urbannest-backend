package com.example.urbannest.repository;

import com.example.urbannest.model.Listing;
import com.example.urbannest.model.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID>, JpaSpecificationExecutor<Listing> {

    @EntityGraph(attributePaths = {"user", "listingDetails", "listingLocation",
            "listingCounters"})
    Page<Listing> findByUserAndDeletedAtIsNull(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "listingDetails", "listingLocation", "listingCounters"})
    @Override
    Page<Listing> findAll(@NonNull Specification<Listing> spec, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"user", "listingDetails", "listingLocation",
            "listingCounters"})
    Optional<Listing> findByListingId(UUID listingId);
}
