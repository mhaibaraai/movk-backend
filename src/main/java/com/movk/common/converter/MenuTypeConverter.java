/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.common.converter;

import com.movk.common.enums.MenuType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter()
public class MenuTypeConverter implements AttributeConverter<MenuType, Short> {
    @Override
    public Short convertToDatabaseColumn(MenuType attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public MenuType convertToEntityAttribute(Short dbData) {
        return MenuType.fromCode(dbData);
    }
}
