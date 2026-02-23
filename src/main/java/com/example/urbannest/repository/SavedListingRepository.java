package com.example.urbannest.repository;

import com.example.urbannest.model.SavedListing;
import com.example.urbannest.model.composite.SavedListingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedListingRepository extends JpaRepository<SavedListing, SavedListingId> {
}
