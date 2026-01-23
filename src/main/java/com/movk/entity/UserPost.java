package com.movk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sys_user_post")
@IdClass(UserPost.Id.class)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPost {

    @jakarta.persistence.Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @jakarta.persistence.Id
    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private UUID userId;
        private UUID postId;
    }
}
