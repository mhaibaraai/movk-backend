/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.base.config.CacheConfig;
import com.movk.common.enums.ConfigType;
import com.movk.dto.config.*;
import com.movk.entity.Config;
import com.movk.repository.ConfigRepository;
import com.movk.service.ConfigService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository configRepository;

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CONFIG, allEntries = true)
    public UUID createConfig(ConfigCreateReq req) {
        // 检查配置键是否已存在
        if (existsByConfigKey(req.configKey())) {
            throw new IllegalArgumentException("配置键已存在: " + req.configKey());
        }

        Config config = Config.builder()
            .configName(req.configName())
            .configKey(req.configKey())
            .configValue(req.configValue())
            .configType(req.configType() != null ? req.configType() : ConfigType.CUSTOM)
            .remark(req.remark())
            .build();

        configRepository.save(config);
        log.info("创建系统配置成功: {}", config.getConfigKey());
        return config.getId();
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CONFIG, allEntries = true)
    public void updateConfig(ConfigUpdateReq req) {
        Config config = configRepository.findById(req.id())
            .filter(c -> !c.getDeleted())
            .orElseThrow(() -> new EntityNotFoundException("系统配置不存在"));

        // 检查新的配置键是否与其他配置冲突
        String oldKey = config.getConfigKey();
        if (!oldKey.equals(req.configKey()) && existsByConfigKey(req.configKey())) {
            throw new IllegalArgumentException("配置键已存在: " + req.configKey());
        }

        config.setConfigName(req.configName());
        config.setConfigKey(req.configKey());
        config.setConfigValue(req.configValue());
        config.setConfigType(req.configType());
        config.setRemark(req.remark());

        configRepository.save(config);
        log.info("更新系统配置成功: {}", config.getConfigKey());
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CONFIG, allEntries = true)
    public void deleteConfig(UUID configId) {
        Config config = configRepository.findById(configId)
            .filter(c -> !c.getDeleted())
            .orElseThrow(() -> new EntityNotFoundException("系统配置不存在"));

        // 系统内置配置不允许删除
        if (config.getConfigType() == ConfigType.BUILTIN) {
            throw new IllegalStateException("系统内置配置不允许删除");
        }

        config.setDeleted(true);
        config.setDeletedAt(OffsetDateTime.now());
        configRepository.save(config);

        log.info("删除系统配置成功: {}", config.getConfigKey());
    }

    @Override
    public ConfigResp getConfigById(UUID configId) {
        return configRepository.findById(configId)
            .filter(c -> !c.getDeleted())
            .map(this::toResp)
            .orElseThrow(() -> new EntityNotFoundException("系统配置不存在"));
    }

    @Override
    public List<ConfigResp> getAllConfigs() {
        return configRepository.findByDeletedFalseOrderByCreatedAtDesc()
            .stream()
            .map(this::toResp)
            .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = CacheConfig.CONFIG, key = "#configKey")
    public String getConfigValue(String configKey) {
        return configRepository.findByConfigKeyAndDeletedFalse(configKey)
            .map(Config::getConfigValue)
            .orElse(null);
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean existsByConfigKey(String configKey) {
        return configRepository.findByConfigKeyAndDeletedFalse(configKey).isPresent();
    }

    @Override
    @CacheEvict(value = CacheConfig.CONFIG, allEntries = true)
    public void refreshCache() {
        log.info("系统配置缓存已刷新");
    }

    // ========== 转换方法 ==========

    private ConfigResp toResp(Config entity) {
        return new ConfigResp(
            entity.getId(),
            entity.getConfigName(),
            entity.getConfigKey(),
            entity.getConfigValue(),
            entity.getConfigType(),
            entity.getRemark(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
