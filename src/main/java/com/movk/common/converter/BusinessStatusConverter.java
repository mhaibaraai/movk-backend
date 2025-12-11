/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.common.converter;

import com.movk.common.enums.BusinessStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class BusinessStatusConverter implements AttributeConverter<BusinessStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(BusinessStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public BusinessStatus convertToEntityAttribute(Short dbData) {
        return BusinessStatus.fromCode(dbData);
    }
}
