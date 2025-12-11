/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.entity;

import com.movk.common.converter.EnableStatusConverter;
import com.movk.common.converter.MenuTypeConverter;
import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.MenuType;
import com.movk.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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

import java.util.UUID;

@Entity
@Table(name = "sys_menu")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "parent_id")
    private UUID parentId;

    @Convert(converter = MenuTypeConverter.class)
    @Column(name = "menu_type", nullable = false)
    private MenuType type;

    @Column(name = "menu_name", nullable = false, length = 50)
    private String name;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum;

    @Column(length = 200)
    private String path;

    @Column(length = 255)
    private String component;

    @Column(name = "query_params", length = 255)
    private String queryParams;

    @Column(name = "is_frame", nullable = false)
    private Boolean isFrame = false;

    @Column(name = "is_cache", nullable = false)
    private Boolean isCache = true;

    @Column(name = "permission_code", columnDefinition = "citext")
    private String permissionCode;

    @Column(nullable = false)
    private Boolean visible = true;

    @Convert(converter = EnableStatusConverter.class)
    @Column(nullable = false)
    private EnableStatus status;

    @Column(length = 100)
    private String icon;

    @Column(length = 500)
    private String remark;
}
