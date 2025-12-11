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
 * 岗位实体
 */
@Entity
@Table(name = "sys_post")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "post_code", nullable = false, unique = true, columnDefinition = "citext")
    private String postCode;

    @Column(name = "post_name", nullable = false, length = 50)
    private String postName;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum = 0;

    @Convert(converter = EnableStatusConverter.class)
    @Column(nullable = false)
    private EnableStatus status;

    @Column(length = 500)
    private String remark;
}
