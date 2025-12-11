/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.common.converter;

import com.movk.common.enums.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class GenderConverter implements AttributeConverter<Gender, Short> {

    @Override
    public Short convertToDatabaseColumn(Gender attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public Gender convertToEntityAttribute(Short dbData) {
        return Gender.fromCode(dbData);
    }
}
