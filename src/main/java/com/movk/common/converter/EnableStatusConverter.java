/*
 * @Author yixuanmiao
 * @Date 2025/08/31 00:03
 */

package com.movk.common.converter;

import com.movk.common.enums.EnableStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter()
public class EnableStatusConverter implements AttributeConverter<EnableStatus, Short> {
    @Override
    public Short convertToDatabaseColumn(EnableStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public EnableStatus convertToEntityAttribute(Short dbData) {
        return EnableStatus.fromCode(dbData);
    }
}
