/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.movk.common.converter.GenderConverter;
import com.movk.common.converter.UserStatusConverter;
import com.movk.common.enums.Gender;
import com.movk.common.enums.UserStatus;
import com.movk.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sys_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 50, columnDefinition = "citext")
    private String username;

    @Column(columnDefinition = "citext")
    private String email;

    @Column(length = 30)
    private String phone;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Convert(converter = UserStatusConverter.class)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "login_date")
    private OffsetDateTime loginDate;

    @Column(length = 50)
    private String nickname;

    @Convert(converter = GenderConverter.class)
    @Column(nullable = false)
    private Gender gender = Gender.UNKNOWN;

    @Column(length = 500)
    private String avatar;

    @Column(name = "dept_id")
    private UUID deptId;

    @Column(name = "login_ip", length = 50)
    private String loginIp;

    @Column(length = 500)
    private String remark;
}
