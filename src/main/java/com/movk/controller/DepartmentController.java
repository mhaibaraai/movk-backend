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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.movk.common.enums.OperationType.*;

/**
 * 部门管理 Controller
 */
@RestController
@RequestMapping("/api/system/dept")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取部门树
     */
    @GetMapping("/tree")
    @RequiresPermission("system:dept:list")
    public R<List<DeptResp>> getDeptTree() {
        return R.success(departmentService.getDepartmentTree());
    }

    /**
     * 获取部门列表（扁平）
     */
    @GetMapping("/list")
    @RequiresPermission("system:dept:list")
    public R<List<DeptResp>> getDeptList() {
        return R.success(departmentService.getAllDepartments());
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/{id}")
    @RequiresPermission("system:dept:query")
    public R<DeptResp> getDeptById(@PathVariable UUID id) {
        return R.success(departmentService.getDepartmentById(id));
    }

    /**
     * 新增部门
     */
    @PostMapping
    @RequiresPermission("system:dept:add")
    @Log(module = "部门管理", operation = CREATE)
    public R<UUID> createDept(@RequestBody DeptCreateReq req) {
        return R.success(departmentService.createDepartment(req));
    }

    /**
     * 修改部门
     */
    @PutMapping
    @RequiresPermission("system:dept:edit")
    @Log(module = "部门管理", operation = UPDATE)
    public R<Void> updateDept(@RequestBody DeptUpdateReq req) {
        departmentService.updateDepartment(req);
        return R.ok();
    }

    /**
     * 删除部门
     */
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
    @GetMapping("/{id}/children-ids")
    @RequiresPermission("system:dept:list")
    public R<List<UUID>> getDeptAndChildIds(@PathVariable UUID id) {
        return R.success(departmentService.getDeptAndChildIds(id));
    }
}
