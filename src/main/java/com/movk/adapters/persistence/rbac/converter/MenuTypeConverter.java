/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.adapters.persistence.rbac.converter;

import com.movk.domain.rbac.model.MenuType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
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