/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.dto.dict.*;
import com.movk.security.annotation.Log;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.DictService;
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
 * 字典管理 Controller
 */
@Tag(name = "字典管理", description = "字典类型和字典数据相关接口")
@RestController
@RequestMapping("/api/system/dicts")
@RequiredArgsConstructor
@Validated
public class DictController {

    private final DictService dictService;

    // ========== 字典类型操作 ==========

    /**
     * 获取字典类型列表
     */
    @Operation(summary = "获取字典类型列表", description = "获取所有字典类型列表")
    @GetMapping("/types")
    @RequiresPermission("system:dict:list")
    public R<List<DictTypeResp>> getDictTypeList() {
        return R.success(dictService.getAllDictTypes());
    }

    /**
     * 获取字典类型详情
     */
    @Operation(summary = "获取字典类型详情", description = "根据字典类型 ID 查询详细信息")
    @GetMapping("/types/{id}")
    @RequiresPermission("system:dict:query")
    public R<DictTypeResp> getDictTypeById(@PathVariable UUID id) {
        return R.success(dictService.getDictTypeById(id));
    }

    /**
     * 新增字典类型
     */
    @Operation(summary = "新增字典类型", description = "创建新的字典类型")
    @PostMapping("/types")
    @RequiresPermission("system:dict:add")
    @Log(module = "字典管理", operation = CREATE)
    public R<UUID> createDictType(@Valid @RequestBody DictTypeCreateReq req) {
        return R.success(dictService.createDictType(req));
    }

    /**
     * 修改字典类型
     */
    @Operation(summary = "修改字典类型", description = "修改字典类型信息")
    @PutMapping("/types")
    @RequiresPermission("system:dict:edit")
    @Log(module = "字典管理", operation = UPDATE)
    public R<Void> updateDictType(@Valid @RequestBody DictTypeUpdateReq req) {
        dictService.updateDictType(req);
        return R.ok();
    }

    /**
     * 删除字典类型
     */
    @Operation(summary = "删除字典类型", description = "根据字典类型 ID 删除字典类型")
    @DeleteMapping("/types/{id}")
    @RequiresPermission("system:dict:delete")
    @Log(module = "字典管理", operation = DELETE)
    public R<Void> deleteDictType(@PathVariable UUID id) {
        dictService.deleteDictType(id);
        return R.ok();
    }

    // ========== 字典数据操作 ==========

    /**
     * 根据字典类型获取字典数据
     */
    @Operation(summary = "获取字典数据", description = "根据字典类型获取对应的字典数据列表")
    @GetMapping("/data")
    public R<List<DictDataResp>> getDictDataByType(@RequestParam String dictType) {
        return R.success(dictService.getDictDataByType(dictType));
    }

    /**
     * 获取字典数据详情
     */
    @Operation(summary = "获取字典数据详情", description = "根据字典数据 ID 查询详细信息")
    @GetMapping("/data/{id}")
    @RequiresPermission("system:dict:query")
    public R<DictDataResp> getDictDataById(@PathVariable UUID id) {
        return R.success(dictService.getDictDataById(id));
    }

    /**
     * 新增字典数据
     */
    @Operation(summary = "新增字典数据", description = "为指定字典类型创建新的字典数据")
    @PostMapping("/data")
    @RequiresPermission("system:dict:add")
    @Log(module = "字典管理", operation = CREATE)
    public R<UUID> createDictData(@Valid @RequestBody DictDataCreateReq req) {
        return R.success(dictService.createDictData(req));
    }

    /**
     * 修改字典数据
     */
    @Operation(summary = "修改字典数据", description = "修改字典数据信息")
    @PutMapping("/data")
    @RequiresPermission("system:dict:edit")
    @Log(module = "字典管理", operation = UPDATE)
    public R<Void> updateDictData(@Valid @RequestBody DictDataUpdateReq req) {
        dictService.updateDictData(req);
        return R.ok();
    }

    /**
     * 删除字典数据
     */
    @Operation(summary = "删除字典数据", description = "根据字典数据 ID 删除字典数据")
    @DeleteMapping("/data/{id}")
    @RequiresPermission("system:dict:delete")
    @Log(module = "字典管理", operation = DELETE)
    public R<Void> deleteDictData(@PathVariable UUID id) {
        dictService.deleteDictData(id);
        return R.ok();
    }

    // ========== 缓存操作 ==========

    /**
     * 刷新字典缓存
     */
    @Operation(summary = "刷新字典缓存", description = "清除并重新加载字典缓存")
    @DeleteMapping("/cache")
    @RequiresPermission("system:dict:edit")
    @Log(module = "字典管理", operation = OTHER, description = "刷新缓存")
    public R<Void> refreshCache() {
        dictService.refreshCache();
        return R.ok();
    }

    /**
     * 检查字典类型是否存在
     */
    @Operation(summary = "检查字典类型", description = "检查指定字典类型是否已存在")
    @GetMapping("/types/exists")
    @RequiresPermission("system:dict:query")
    public R<Boolean> checkDictType(@RequestParam String dictType) {
        return R.success(dictService.existsByDictType(dictType));
    }
}
