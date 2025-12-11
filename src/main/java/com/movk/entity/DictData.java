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
 * 字典数据实体
 */
@Entity
@Table(name = "sys_dict_data")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class DictData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "dict_type", nullable = false, columnDefinition = "citext")
    private String dictType;

    @Column(name = "dict_label", nullable = false, length = 100)
    private String dictLabel;

    @Column(name = "dict_value", nullable = false, length = 100)
    private String dictValue;

    @Column(name = "dict_sort", nullable = false)
    private Integer dictSort = 0;

    @Column(name = "css_class", length = 100)
    private String cssClass;

    @Column(name = "list_class", length = 100)
    private String listClass;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Convert(converter = EnableStatusConverter.class)
    @Column(nullable = false)
    private EnableStatus status;

    @Column(length = 500)
    private String remark;
}
