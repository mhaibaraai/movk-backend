/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.entity;

import com.movk.common.converter.DataScopeConverter;
import com.movk.common.converter.EnableStatusConverter;
import com.movk.common.converter.RoleTypeConverter;
import com.movk.common.enums.DataScope;
import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.RoleType;
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
@Table(name = "sys_role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "role_code", nullable = false, unique = true, columnDefinition = "citext")
    private String code;

    @Column(name = "role_name", nullable = false, length = 50)
    private String name;

    @Column(name = "role_sort", nullable = false)
    private Integer roleSort = 0;

    @Convert(converter = DataScopeConverter.class)
    @Column(name = "data_scope", nullable = false)
    private DataScope dataScope;

    @Column(name = "data_scope_dept_ids", columnDefinition = "TEXT")
    private String dataScopeDeptIds;

    @Convert(converter = EnableStatusConverter.class)
    @Column(nullable = false)
    private EnableStatus status;

    @Convert(converter = RoleTypeConverter.class)
    @Column(name = "role_type", nullable = false)
    private RoleType roleType;

    @Column(length = 500)
    private String remark;
}
