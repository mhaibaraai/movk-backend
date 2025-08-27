/*
 * @Author yixuanmiao
 * @Date 2025/08/27 21:42
 */

package com.movk.adapters.persistence.rbac.converter;

import com.movk.domain.rbac.model.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class UserStatusConverter implements AttributeConverter<UserStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(UserStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public UserStatus convertToEntityAttribute(Short dbData) {
        return UserStatus.fromCode(dbData);
    }
}
