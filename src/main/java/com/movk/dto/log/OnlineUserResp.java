package com.movk.dto.log;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 在线用户/会话响应
 */
@Data
@Builder
public class OnlineUserResp {

    /**
     * 会话 ID（RefreshToken ID）
     */
    private UUID id;

    /**
     * 用户 ID
     */
    private UUID userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 客户端 IP
     */
    private String clientIp;

    /**
     * 签发时间
     */
    private OffsetDateTime issuedAt;

    /**
     * 过期时间
     */
    private OffsetDateTime expiresAt;

    /**
     * 最后活跃时间
     */
    private OffsetDateTime lastUsedAt;
}
