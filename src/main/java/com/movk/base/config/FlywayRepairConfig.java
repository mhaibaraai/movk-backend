/*
 * @Author yixuanmiao
 * @Date 2025/12/10
 */

package com.movk.base.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway 修复配置
 * 在迁移前自动修复失败的迁移记录
 */
@Configuration
public class FlywayRepairConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayRepairConfig.class);

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                // 修复失败的迁移记录
                log.info("执行 Flyway repair，清理失败的迁移记录...");
                flyway.repair();
                log.info("Flyway repair 完成");

                // 执行迁移
                flyway.migrate();
            } catch (Exception e) {
                log.error("Flyway 迁移失败", e);
                throw e;
            }
        };
    }
}
