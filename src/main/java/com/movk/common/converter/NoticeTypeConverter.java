/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.common.converter;

import com.movk.common.enums.NoticeType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class NoticeTypeConverter implements AttributeConverter<NoticeType, Short> {

    @Override
    public Short convertToDatabaseColumn(NoticeType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public NoticeType convertToEntityAttribute(Short dbData) {
        return NoticeType.fromCode(dbData);
    }
}
