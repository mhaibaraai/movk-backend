/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.common.converter;

import com.movk.common.enums.ConfigType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ConfigTypeConverter implements AttributeConverter<ConfigType, Short> {

    @Override
    public Short convertToDatabaseColumn(ConfigType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public ConfigType convertToEntityAttribute(Short dbData) {
        return ConfigType.fromCode(dbData);
    }
}
