package com.example.urbannest.model;

import com.example.urbannest.model.composite.SavedBlogId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "saved_blogs")
@Getter @Setter
public class SavedBlog {

    @EmbeddedId
    private SavedBlogId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blogId")
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}