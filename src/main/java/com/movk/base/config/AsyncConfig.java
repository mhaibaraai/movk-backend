/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.base.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 异步任务线程池
     * 主要用于操作日志异步保存、邮件发送等非关键性异步任务
     */
    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(20);
        // 队列容量
        executor.setQueueCapacity(500);
        // 线程名前缀
        executor.setThreadNamePrefix("async-");
        // 空闲线程存活时间（秒）
        executor.setKeepAliveSeconds(60);
        // 拒绝策略：由调用线程处理（降级保证任务执行）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 关闭时等待任务完成的最大时间（秒）
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("异步线程池初始化完成 - corePoolSize: {}, maxPoolSize: {}, queueCapacity: {}",
            executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        return executor;
    }
}
