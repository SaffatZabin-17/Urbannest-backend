package com.example.urbannest.specification;

import com.example.urbannest.model.Listing;
import com.example.urbannest.model.ListingDetails;
import com.example.urbannest.model.ListingLocation;
import com.example.urbannest.model.enums.PropertyStatus;
import com.example.urbannest.model.enums.PropertyType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ListingSpecification {

    public static Specification<Listing> withFilters(PropertyType propertyType,
                                                     BigDecimal priceMin,
                                                     BigDecimal priceMax,
                                                     String district,
                                                     Integer minBedrooms) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("propertyStatus"), PropertyStatus.published));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (propertyType != null) {
                predicates.add(cb.equal(root.get("propertyType"), propertyType));
            }
            if (priceMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricing"), priceMin));
            }
            if (priceMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricing"), priceMax));
            }
            if (district != null) {
                Join<Listing, ListingLocation> locationJoin = root.join("listingLocation", JoinType.INNER);
                predicates.add(cb.equal(locationJoin.get("district"), district));
            }
            if (minBedrooms != null) {
                Join<Listing, ListingDetails> detailsJoin = root.join("listingDetails", JoinType.INNER);
                predicates.add(cb.greaterThanOrEqualTo(detailsJoin.get("bedroomsCount"), minBedrooms));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
