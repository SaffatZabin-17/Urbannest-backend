package com.example.urbannest.repository;

import com.example.urbannest.model.ListingCounters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ListingCountersRepository extends JpaRepository<ListingCounters, UUID> {
}
