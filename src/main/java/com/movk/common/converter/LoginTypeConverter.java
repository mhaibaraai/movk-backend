/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.common.converter;

import com.movk.common.enums.LoginType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class LoginTypeConverter implements AttributeConverter<LoginType, Short> {

    @Override
    public Short convertToDatabaseColumn(LoginType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public LoginType convertToEntityAttribute(Short dbData) {
        return LoginType.fromCode(dbData);
    }
}
