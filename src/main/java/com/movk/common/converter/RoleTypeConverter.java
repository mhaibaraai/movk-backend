/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.common.converter;

import com.movk.common.enums.RoleType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RoleTypeConverter implements AttributeConverter<RoleType, Short> {

    @Override
    public Short convertToDatabaseColumn(RoleType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public RoleType convertToEntityAttribute(Short dbData) {
        return RoleType.fromCode(dbData);
    }
}
