/*
 * @Author yixuanmiao
 * @Date 2025/08/28 10:29
 */

package com.movk.adapters.persistence.rbac.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.movk.adapters.persistence.rbac.converter.UserStatusConverter;
import com.movk.domain.rbac.model.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sys_user")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 50, columnDefinition = "citext")
    private String username;

    @Column(columnDefinition = "citext")
    private String email;

    @Column(length = 30)
    private String phone;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Convert(converter = UserStatusConverter.class)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "github_id", length = 64)
    private String githubId;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "display_name", columnDefinition = "citext")
    private String displayName;
}
