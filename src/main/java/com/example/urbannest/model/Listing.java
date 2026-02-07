package com.example.urbannest.model;

import com.example.urbannest.model.enums.PropertyStatus;
import com.example.urbannest.model.enums.PropertyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "listing_id", nullable = false)
    @Getter
    @Setter
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Getter
    @Setter
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "property_type", nullable = false)
    @Getter
    @Setter
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "property_status", nullable = false)
    @Getter
    @Setter
    private PropertyStatus propertyStatus;

    @Column(name = "title", nullable = false)
    @Getter
    @Setter
    private String title;

    @Column(name = "description")
    @Getter
    @Setter
    private String description;

    @Column(name = "pricing", nullable = false, precision = 15, scale = 2)
    @Getter
    @Setter
    private BigDecimal pricing;

    @Column(name = "created_at", nullable = false)
    @Getter
    @Setter
    private OffsetDateTime createdAt;

    @Column (name = "published_at")
    @Getter
    @Setter
    private OffsetDateTime publishedAt;

    @Column(name = "updated_at", nullable = false)
    @Getter
    @Setter
    private OffsetDateTime updatedAt;

    @Column (name = "deleted_at")
    @Getter
    @Setter
    private OffsetDateTime deletedAt;

}
