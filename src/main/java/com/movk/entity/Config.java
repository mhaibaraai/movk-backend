/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.entity;

import com.movk.common.converter.ConfigTypeConverter;
import com.movk.common.enums.ConfigType;
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
 * 系统配置实体
 */
@Entity
@Table(name = "sys_config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Config extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "config_name", nullable = false, length = 100)
    private String configName;

    @Column(name = "config_key", nullable = false, unique = true, columnDefinition = "citext")
    private String configKey;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    @Convert(converter = ConfigTypeConverter.class)
    @Column(name = "config_type", nullable = false)
    private ConfigType configType;

    @Column(length = 500)
    private String remark;
}
