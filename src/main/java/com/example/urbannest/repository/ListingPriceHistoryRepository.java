package com.example.urbannest.repository;

import com.example.urbannest.model.ListingPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ListingPriceHistoryRepository extends JpaRepository<ListingPriceHistory, UUID> {
}
