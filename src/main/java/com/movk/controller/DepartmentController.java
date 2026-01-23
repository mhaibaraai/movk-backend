/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.dto.dept.*;
import com.movk.security.annotation.Log;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.DepartmentService;
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
 * 部门管理 Controller
 */
@Tag(name = "部门管理", description = "部门相关接口")
@RestController
@RequestMapping("/api/system/depts")
@RequiredArgsConstructor
@Validated
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取部门树
     */
    @Operation(summary = "获取部门树", description = "获取树形结构的部门列表")
    @GetMapping("/tree")
    @RequiresPermission("system:dept:list")
    public R<List<DeptResp>> getDeptTree() {
        return R.success(departmentService.getDepartmentTree());
    }

    /**
     * 获取部门列表（扁平）
     */
    @Operation(summary = "获取部门列表", description = "获取扁平化的部门列表")
    @GetMapping
    @RequiresPermission("system:dept:list")
    public R<List<DeptResp>> getDeptList() {
        return R.success(departmentService.getAllDepartments());
    }

    /**
     * 获取部门详情
     */
    @Operation(summary = "获取部门详情", description = "根据部门 ID 查询部门详细信息")
    @GetMapping("/{id}")
    @RequiresPermission("system:dept:query")
    public R<DeptResp> getDeptById(@PathVariable UUID id) {
        return R.success(departmentService.getDepartmentById(id));
    }

    /**
     * 新增部门
     */
    @Operation(summary = "新增部门", description = "创建新部门")
    @PostMapping
    @RequiresPermission("system:dept:create")
    @Log(module = "部门管理", operation = CREATE)
    public R<UUID> createDept(@Valid @RequestBody DeptCreateReq req) {
        return R.success(departmentService.createDepartment(req));
    }

    /**
     * 修改部门
     */
    @Operation(summary = "修改部门", description = "修改部门信息")
    @PutMapping("/{id}")
    @RequiresPermission("system:dept:update")
    @Log(module = "部门管理", operation = UPDATE)
    public R<Void> updateDept(@PathVariable UUID id, @Valid @RequestBody DeptUpdateReq req) {
        departmentService.updateDepartment(id, req);
        return R.ok();
    }

    /**
     * 删除部门
     */
    @Operation(summary = "删除部门", description = "根据部门 ID 删除部门")
    @DeleteMapping("/{id}")
    @RequiresPermission("system:dept:delete")
    @Log(module = "部门管理", operation = DELETE)
    public R<Void> deleteDept(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
        return R.ok();
    }

    /**
     * 获取部门及子部门ID列表
     * 用于数据权限范围选择
     */
    @Operation(summary = "获取部门及子部门 ID", description = "获取指定部门及其所有子部门的 ID 列表")
    @GetMapping("/{id}/children-ids")
    @RequiresPermission("system:dept:list")
    public R<List<UUID>> getDeptAndChildIds(@PathVariable UUID id) {
        return R.success(departmentService.getDeptAndChildIds(id));
    }
}
