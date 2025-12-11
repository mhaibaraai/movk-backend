/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.entity;

import com.movk.common.converter.BusinessStatusConverter;
import com.movk.common.converter.LoginTypeConverter;
import com.movk.common.enums.BusinessStatus;
import com.movk.common.enums.LoginType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 登录日志实体
 */
@Entity
@Table(name = "sys_login_log")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Convert(converter = LoginTypeConverter.class)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    @Column(name = "login_ip", length = 50)
    private String loginIp;

    @Column(name = "login_location", length = 100)
    private String loginLocation;

    @Column(length = 50)
    private String browser;

    @Column(length = 50)
    private String os;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Convert(converter = BusinessStatusConverter.class)
    @Column(nullable = false)
    private BusinessStatus status;

    @Column(length = 500)
    private String message;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
