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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.movk.common.enums.OperationType.*;

/**
 * 字典管理 Controller
 */
@RestController
@RequestMapping("/api/system/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // ========== 字典类型操作 ==========

    /**
     * 获取字典类型列表
     */
    @GetMapping("/type/list")
    @RequiresPermission("system:dict:list")
    public R<List<DictTypeResp>> getDictTypeList() {
        return R.success(dictService.getAllDictTypes());
    }

    /**
     * 获取字典类型详情
     */
    @GetMapping("/type/{id}")
    @RequiresPermission("system:dict:query")
    public R<DictTypeResp> getDictTypeById(@PathVariable UUID id) {
        return R.success(dictService.getDictTypeById(id));
    }

    /**
     * 新增字典类型
     */
    @PostMapping("/type")
    @RequiresPermission("system:dict:add")
    @Log(module = "字典管理", operation = CREATE)
    public R<UUID> createDictType(@RequestBody DictTypeCreateReq req) {
        return R.success(dictService.createDictType(req));
    }

    /**
     * 修改字典类型
     */
    @PutMapping("/type")
    @RequiresPermission("system:dict:edit")
    @Log(module = "字典管理", operation = UPDATE)
    public R<Void> updateDictType(@RequestBody DictTypeUpdateReq req) {
        dictService.updateDictType(req);
        return R.ok();
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping("/type/{id}")
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
    @GetMapping("/data/type/{dictType}")
    public R<List<DictDataResp>> getDictDataByType(@PathVariable String dictType) {
        return R.success(dictService.getDictDataByType(dictType));
    }

    /**
     * 获取字典数据详情
     */
    @GetMapping("/data/{id}")
    @RequiresPermission("system:dict:query")
    public R<DictDataResp> getDictDataById(@PathVariable UUID id) {
        return R.success(dictService.getDictDataById(id));
    }

    /**
     * 新增字典数据
     */
    @PostMapping("/data")
    @RequiresPermission("system:dict:add")
    @Log(module = "字典管理", operation = CREATE)
    public R<UUID> createDictData(@RequestBody DictDataCreateReq req) {
        return R.success(dictService.createDictData(req));
    }

    /**
     * 修改字典数据
     */
    @PutMapping("/data")
    @RequiresPermission("system:dict:edit")
    @Log(module = "字典管理", operation = UPDATE)
    public R<Void> updateDictData(@RequestBody DictDataUpdateReq req) {
        dictService.updateDictData(req);
        return R.ok();
    }

    /**
     * 删除字典数据
     */
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
    @DeleteMapping("/refresh-cache")
    @RequiresPermission("system:dict:edit")
    @Log(module = "字典管理", operation = OTHER, description = "刷新缓存")
    public R<Void> refreshCache() {
        dictService.refreshCache();
        return R.ok();
    }

    /**
     * 检查字典类型是否存在
     */
    @GetMapping("/type/check/{dictType}")
    @RequiresPermission("system:dict:query")
    public R<Boolean> checkDictType(@PathVariable String dictType) {
        return R.success(dictService.existsByDictType(dictType));
    }
}
