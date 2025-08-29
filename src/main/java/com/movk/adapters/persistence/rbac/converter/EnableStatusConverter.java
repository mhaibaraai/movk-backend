/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.adapters.persistence.rbac.converter;

import com.movk.domain.rbac.model.EnableStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
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