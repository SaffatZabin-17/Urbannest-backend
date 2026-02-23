package com.example.urbannest.repository;

import com.example.urbannest.model.FavoriteListing;
import com.example.urbannest.model.composite.FavoriteListingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteListingRepository extends JpaRepository<FavoriteListing, FavoriteListingId> {
}
