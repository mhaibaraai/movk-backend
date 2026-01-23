package com.movk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sys_role_menu")
@IdClass(RoleMenu.Id.class)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleMenu {

    @jakarta.persistence.Id
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @jakarta.persistence.Id
    @Column(name = "menu_id", nullable = false)
    private UUID menuId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private UUID roleId;
        private UUID menuId;
    }
}
