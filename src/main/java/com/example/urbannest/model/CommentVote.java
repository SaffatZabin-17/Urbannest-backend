package com.example.urbannest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "comment_votes")
@Getter @Setter
public class CommentVote {

    @EmbeddedId
    private CommentVoteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commentId")
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "vote_value", nullable = false)
    private Integer voteValue;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}