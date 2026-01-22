package com.movk.base.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置
 * 具体的定时任务在各 Service 中通过 @Scheduled 注解定义
 * 如：TokenService.cleanupExpiredTokens() 每天凌晨 3 点清理过期 RefreshToken
 */
@Configuration
@EnableScheduling
public class ScheduleConfig {
}
