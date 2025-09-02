/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Configuration
public class JpaAuditingConfig {

    /**
     * 配置JPA审计时间提供器，提供OffsetDateTime类型
     * 解决@CreatedDate和@LastModifiedDate与OffsetDateTime类型不匹配的问题
     */
    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now(ZoneOffset.UTC));
    }
}
