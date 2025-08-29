/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.adapters.persistence.rbac.entity.id;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RoleMenuId implements Serializable {
    private UUID roleId;
    private UUID menuId;
}