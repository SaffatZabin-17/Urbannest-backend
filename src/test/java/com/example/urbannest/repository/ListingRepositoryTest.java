package com.example.urbannest.repository;

import com.example.urbannest.model.Listing;
import com.example.urbannest.model.ListingCounters;
import com.example.urbannest.model.ListingDetails;
import com.example.urbannest.model.ListingLocation;
import com.example.urbannest.model.User;
import com.example.urbannest.model.enums.ListingCondition;
import com.example.urbannest.model.enums.PropertyStatus;
import com.example.urbannest.model.enums.PropertyType;
import com.example.urbannest.specification.ListingSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ListingRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setFirebaseId("test-uid-repo");
        testUser.setName("Test User");
        testUser.setEmail("repo-test@example.com");
        testUser.setNidHash("unique-hash-repo");
        testUser.setRoleName("USER");
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());
        entityManager.persistAndFlush(testUser);
    }

    @Test
    void findAll_withSpecification_onlyReturnsPublishedAndNotDeleted() {
        persistListing(PropertyStatus.published, null);
        persistListing(PropertyStatus.draft, null);
        persistListing(PropertyStatus.published, OffsetDateTime.now()); // soft-deleted

        Specification<Listing> spec = ListingSpecification.withFilters(null, null, null, null, null);
        Page<Listing> page = listingRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getPropertyStatus()).isEqualTo(PropertyStatus.published);
        assertThat(page.getContent().get(0).getDeletedAt()).isNull();
    }

    @Test
    void findAll_withPriceRange_filtersCorrectly() {
        persistListingWithPrice(PropertyStatus.published, new BigDecimal("300000"));
        persistListingWithPrice(PropertyStatus.published, new BigDecimal("500000"));
        persistListingWithPrice(PropertyStatus.published, new BigDecimal("800000"));

        Specification<Listing> spec = ListingSpecification.withFilters(
                null, new BigDecimal("400000"), new BigDecimal("600000"), null, null);
        Page<Listing> page = listingRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getPricing()).isEqualByComparingTo(new BigDecimal("500000"));
    }

    @Test
    void findAll_withPropertyTypeFilter_filtersCorrectly() {
        persistListingWithType(PropertyType.apartment);
        persistListingWithType(PropertyType.house);

        Specification<Listing> spec = ListingSpecification.withFilters(
                PropertyType.apartment, null, null, null, null);
        Page<Listing> page = listingRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getPropertyType()).isEqualTo(PropertyType.apartment);
    }

    @Test
    void findByListingId_returnsWithEagerLoadedRelations() {
        Listing listing = persistFullListing();

        Optional<Listing> found = listingRepository.findByListingId(listing.getListingId());

        assertThat(found).isPresent();
        // EntityGraph ensures these are loaded without additional queries
        assertThat(found.get().getUser()).isNotNull();
        assertThat(found.get().getListingDetails()).isNotNull();
        assertThat(found.get().getListingLocation()).isNotNull();
        assertThat(found.get().getListingCounters()).isNotNull();
    }

    // ========== Helpers ==========

    private Listing persistListing(PropertyStatus status, OffsetDateTime deletedAt) {
        Listing listing = new Listing();
        listing.setUser(testUser);
        listing.setPropertyType(PropertyType.apartment);
        listing.setPropertyStatus(status);
        listing.setTitle("Test Listing");
        listing.setPricing(new BigDecimal("500000"));
        listing.setCreatedAt(OffsetDateTime.now());
        listing.setUpdatedAt(OffsetDateTime.now());
        listing.setDeletedAt(deletedAt);
        entityManager.persistAndFlush(listing);
        return listing;
    }

    private void persistListingWithPrice(PropertyStatus status, BigDecimal price) {
        Listing listing = new Listing();
        listing.setUser(testUser);
        listing.setPropertyType(PropertyType.apartment);
        listing.setPropertyStatus(status);
        listing.setTitle("Listing " + price);
        listing.setPricing(price);
        listing.setCreatedAt(OffsetDateTime.now());
        listing.setUpdatedAt(OffsetDateTime.now());
        entityManager.persistAndFlush(listing);
    }

    private void persistListingWithType(PropertyType type) {
        Listing listing = new Listing();
        listing.setUser(testUser);
        listing.setPropertyType(type);
        listing.setPropertyStatus(PropertyStatus.published);
        listing.setTitle("Listing " + type);
        listing.setPricing(new BigDecimal("500000"));
        listing.setCreatedAt(OffsetDateTime.now());
        listing.setUpdatedAt(OffsetDateTime.now());
        entityManager.persistAndFlush(listing);
    }

    private Listing persistFullListing() {
        Listing listing = persistListing(PropertyStatus.published, null);

        ListingDetails details = new ListingDetails();
        details.setListing(listing);
        details.setYearBuilt(2020);
        details.setListingCondition(ListingCondition.brand_new);
        details.setBedroomsCount(3);
        details.setBathroomsCount(2);
        details.setBalconiesCount(1);
        details.setLivingArea(1200);
        entityManager.persistAndFlush(details);

        ListingLocation location = new ListingLocation();
        location.setListing(listing);
        location.setAddressLine("House 12");
        location.setArea("Gulshan");
        location.setDistrict("Dhaka");
        location.setZipCode("1212");
        location.setLatitude(new BigDecimal("23.780000"));
        location.setLongitude(new BigDecimal("90.420000"));
        entityManager.persistAndFlush(location);

        ListingCounters counters = new ListingCounters();
        counters.setListing(listing);
        counters.setViewCount(0);
        counters.setFavoriteCount(0);
        counters.setSaveCount(0);
        entityManager.persistAndFlush(counters);

        entityManager.clear(); // clear persistence context to force reload via EntityGraph
        return listing;
    }
}
