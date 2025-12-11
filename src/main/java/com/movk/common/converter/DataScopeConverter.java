/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.common.converter;

import com.movk.common.enums.DataScope;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DataScopeConverter implements AttributeConverter<DataScope, Short> {

    @Override
    public Short convertToDatabaseColumn(DataScope attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public DataScope convertToEntityAttribute(Short dbData) {
        return DataScope.fromCode(dbData);
    }
}
