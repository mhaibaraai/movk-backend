/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.base.config;

import com.movk.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduleConfig {

    private final OnlineUserService onlineUserService;

    /**
     * 清理过期的在线用户记录
     * 每 5 分钟执行一次
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanExpiredOnlineUsers() {
        try {
            onlineUserService.cleanExpiredUsers();
            log.debug("定时清理过期在线用户记录完成");
        } catch (Exception e) {
            log.error("定时清理过期在线用户记录失败: {}", e.getMessage(), e);
        }
    }
}
