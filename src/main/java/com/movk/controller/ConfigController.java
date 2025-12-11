/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.dto.config.*;
import com.movk.security.annotation.Log;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.movk.common.enums.OperationType.*;

/**
 * 系统配置 Controller
 */
@RestController
@RequestMapping("/api/system/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    /**
     * 获取配置列表
     */
    @GetMapping("/list")
    @RequiresPermission("system:config:list")
    public R<List<ConfigResp>> getConfigList() {
        return R.success(configService.getAllConfigs());
    }

    /**
     * 获取配置详情
     */
    @GetMapping("/{id}")
    @RequiresPermission("system:config:query")
    public R<ConfigResp> getConfigById(@PathVariable UUID id) {
        return R.success(configService.getConfigById(id));
    }

    /**
     * 根据配置键获取配置值
     * 此接口无需权限，供前端获取公开配置
     */
    @GetMapping("/key/{configKey}")
    public R<String> getConfigByKey(@PathVariable String configKey) {
        return R.success(configService.getConfigValue(configKey));
    }

    /**
     * 新增配置
     */
    @PostMapping
    @RequiresPermission("system:config:add")
    @Log(module = "系统配置", operation = CREATE)
    public R<UUID> createConfig(@RequestBody ConfigCreateReq req) {
        return R.success(configService.createConfig(req));
    }

    /**
     * 修改配置
     */
    @PutMapping
    @RequiresPermission("system:config:edit")
    @Log(module = "系统配置", operation = UPDATE)
    public R<Void> updateConfig(@RequestBody ConfigUpdateReq req) {
        configService.updateConfig(req);
        return R.ok();
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("system:config:delete")
    @Log(module = "系统配置", operation = DELETE)
    public R<Void> deleteConfig(@PathVariable UUID id) {
        configService.deleteConfig(id);
        return R.ok();
    }

    /**
     * 刷新配置缓存
     */
    @DeleteMapping("/refresh-cache")
    @RequiresPermission("system:config:edit")
    @Log(module = "系统配置", operation = OTHER, description = "刷新缓存")
    public R<Void> refreshCache() {
        configService.refreshCache();
        return R.ok();
    }

    /**
     * 检查配置键是否存在
     */
    @GetMapping("/check-key/{configKey}")
    @RequiresPermission("system:config:query")
    public R<Boolean> checkConfigKey(@PathVariable String configKey) {
        return R.success(configService.existsByConfigKey(configKey));
    }
}
