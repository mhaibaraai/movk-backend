/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 在线用户实体
 */
@Entity
@Table(name = "sys_online_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class OnlineUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false, unique = true, length = 100)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "dept_id")
    private UUID deptId;

    @Column(name = "dept_name", length = 50)
    private String deptName;

    @Column(name = "login_ip", length = 50)
    private String loginIp;

    @Column(name = "login_location", length = 100)
    private String loginLocation;

    @Column(length = 50)
    private String browser;

    @Column(length = 50)
    private String os;

    @Column(name = "login_time", nullable = false)
    private OffsetDateTime loginTime;

    @Column(name = "expire_time", nullable = false)
    private OffsetDateTime expireTime;
}
