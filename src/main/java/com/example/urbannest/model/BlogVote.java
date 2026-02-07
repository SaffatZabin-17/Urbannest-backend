package com.example.urbannest.model;

import com.example.urbannest.model.composite.BlogVoteId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "blog_votes")
@Getter @Setter
public class BlogVote {

    @EmbeddedId
    private BlogVoteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blogId")
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @Column(name = "vote_value", nullable = false)
    private Integer voteValue;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}