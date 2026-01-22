package com.movk.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * RefreshToken 实体
 * 用于双 Token 认证架构中的长期令牌存储
 */
@Entity
@Table(name = "sys_refresh_token")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "revoked_reason", length = 100)
    private String revokedReason;

    /**
     * 检查 Token 是否有效（未过期且未撤销）
     */
    public boolean isValid() {
        return !revoked && expiresAt.isAfter(OffsetDateTime.now());
    }

    /**
     * 撤销 Token
     */
    public void revoke(String reason) {
        this.revoked = true;
        this.revokedAt = OffsetDateTime.now();
        this.revokedReason = reason;
    }

    /**
     * 更新最后使用时间
     */
    public void touch() {
        this.lastUsedAt = OffsetDateTime.now();
    }
}
