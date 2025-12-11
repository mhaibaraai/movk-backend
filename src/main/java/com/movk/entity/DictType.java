/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.entity;

import com.movk.common.converter.EnableStatusConverter;
import com.movk.common.enums.EnableStatus;
import com.movk.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

/**
 * 字典类型实体
 */
@Entity
@Table(name = "sys_dict_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class DictType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "dict_name", nullable = false, length = 100)
    private String dictName;

    @Column(name = "dict_type", nullable = false, unique = true, columnDefinition = "citext")
    private String dictType;

    @Convert(converter = EnableStatusConverter.class)
    @Column(nullable = false)
    private EnableStatus status;

    @Column(length = 500)
    private String remark;
}
