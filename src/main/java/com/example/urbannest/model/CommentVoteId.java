package com.example.urbannest.model;

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
public class CommentVoteId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "comment_id")
    private UUID commentId;
}