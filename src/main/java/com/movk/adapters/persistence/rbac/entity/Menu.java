/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.adapters.persistence.rbac.entity;

import com.movk.adapters.persistence.rbac.converter.EnableStatusConverter;
import com.movk.adapters.persistence.rbac.converter.MenuTypeConverter;
import com.movk.domain.rbac.model.EnableStatus;
import com.movk.domain.rbac.model.MenuType;
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
@Table(name = "sys_menu")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "parent_id")
    private UUID parentId;

    @Convert(converter = MenuTypeConverter.class)
    @Column(nullable = false)
    private MenuType type;

    @Column(nullable = false, columnDefinition = "citext")
    private String name;

    @Column
    private String path;

    @Column
    private String component;

    @Column
    private String icon;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum;

    @Column(nullable = false)
    private Boolean visible;

    @Column(nullable = false)
    private Boolean cacheable;

    @Column(name = "external_link", nullable = false)
    private Boolean externalLink;

    @Column(name = "permission_code", columnDefinition = "citext")
    private String permissionCode;

    @Convert(converter = EnableStatusConverter.class)
    @Column(nullable = false)
    private EnableStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
