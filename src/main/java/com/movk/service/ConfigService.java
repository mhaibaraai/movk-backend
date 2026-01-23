/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

import com.movk.dto.config.*;

import java.util.List;
import java.util.UUID;

/**
 * 系统配置服务接口
 */
public interface ConfigService {

    /**
     * 创建系统配置
     */
    UUID createConfig(ConfigCreateReq req);

    /**
     * 更新系统配置
     */
    void updateConfig(UUID id, ConfigUpdateReq req);

    /**
     * 删除系统配置（逻辑删除）
     */
    void deleteConfig(UUID configId);

    /**
     * 根据ID查询系统配置
     */
    ConfigResp getConfigById(UUID configId);

    /**
     * 查询所有系统配置列表
     */
    List<ConfigResp> getAllConfigs();

    /**
     * 根据配置键查询配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据配置键查询配置值，支持默认值
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKey(String configKey);

    /**
     * 刷新配置缓存
     */
    void refreshCache();
}
