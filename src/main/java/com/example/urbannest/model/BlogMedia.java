package com.example.urbannest.model;

import com.example.urbannest.model.composite.BlogMediaId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "blog_media")
@Getter @Setter
public class BlogMedia {

    @EmbeddedId
    private BlogMediaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blogId")
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mediaId")
    @JoinColumn(name = "media_id")
    private MediaAsset mediaAsset;
}