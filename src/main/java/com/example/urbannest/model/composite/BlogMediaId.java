package com.example.urbannest.model.composite;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter
@EqualsAndHashCode
public class BlogMediaId implements Serializable {

    @Column(name = "blog_id")
    private UUID blogId;

    @Column(name = "media_id")
    private UUID mediaId;
}