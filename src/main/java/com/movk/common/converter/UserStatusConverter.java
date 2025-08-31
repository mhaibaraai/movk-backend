/*
 * @Author yixuanmiao
 * @Date 2025/08/30 22:36
 */

package com.movk.common.converter;

import com.movk.common.enums.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter()
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
