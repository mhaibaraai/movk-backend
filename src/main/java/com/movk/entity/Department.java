/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.entity;

import com.movk.common.converter.EnableStatusConverter;
import com.movk.common.enums.EnableStatus;
import com.movk.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * 部门实体
 */
@Entity
@Table(name = "sys_dept")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(length = 500)
    private String ancestors;

    @Column(name = "dept_name", nullable = false, length = 50)
    private String deptName;

    @Column(name = "dept_code", unique = true, columnDefinition = "citext")
    private String deptCode;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum = 0;

    @Column(name = "leader_user_id")
    private UUID leaderUserId;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Convert(converter = EnableStatusConverter.class)
    @Column(nullable = false)
    private EnableStatus status;
}
