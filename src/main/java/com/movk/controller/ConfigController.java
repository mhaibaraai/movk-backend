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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.movk.common.enums.OperationType.*;

/**
 * 系统配置 Controller
 */
@Tag(name = "系统配置管理", description = "系统配置相关接口")
@RestController
@RequestMapping("/api/system/configs")
@RequiredArgsConstructor
@Validated
public class ConfigController {

    private final ConfigService configService;

    /**
     * 获取配置列表
     */
    @Operation(summary = "获取配置列表", description = "获取所有系统配置列表")
    @GetMapping
    @RequiresPermission("system:config:list")
    public R<List<ConfigResp>> getConfigList() {
        return R.success(configService.getAllConfigs());
    }

    /**
     * 获取配置详情
     */
    @Operation(summary = "获取配置详情", description = "根据配置 ID 查询配置详细信息")
    @GetMapping("/{id}")
    @RequiresPermission("system:config:query")
    public R<ConfigResp> getConfigById(@PathVariable UUID id) {
        return R.success(configService.getConfigById(id));
    }

    /**
     * 根据配置键获取配置值
     * 此接口无需权限，供前端获取公开配置
     */
    @Operation(summary = "根据配置键获取值", description = "根据配置键获取对应的配置值，无需权限")
    @GetMapping("/key/{configKey}")
    public R<String> getConfigByKey(@PathVariable String configKey) {
        return R.success(configService.getConfigValue(configKey));
    }

    /**
     * 新增配置
     */
    @Operation(summary = "新增配置", description = "创建新的系统配置")
    @PostMapping
    @RequiresPermission("system:config:create")
    @Log(module = "系统配置", operation = CREATE)
    public R<UUID> createConfig(@Valid @RequestBody ConfigCreateReq req) {
        return R.success(configService.createConfig(req));
    }

    /**
     * 修改配置
     */
    @Operation(summary = "修改配置", description = "修改系统配置信息")
    @PutMapping("/{id}")
    @RequiresPermission("system:config:update")
    @Log(module = "系统配置", operation = UPDATE)
    public R<Void> updateConfig(@PathVariable UUID id, @Valid @RequestBody ConfigUpdateReq req) {
        configService.updateConfig(id, req);
        return R.ok();
    }

    /**
     * 删除配置
     */
    @Operation(summary = "删除配置", description = "根据配置 ID 删除系统配置")
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
    @Operation(summary = "刷新配置缓存", description = "清除并重新加载配置缓存")
    @PostMapping("/refresh-cache")
    @RequiresPermission("system:config:update")
    @Log(module = "系统配置", operation = OTHER, description = "刷新缓存")
    public R<Void> refreshCache() {
        configService.refreshCache();
        return R.ok();
    }
}
