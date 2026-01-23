/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

import com.movk.dto.dept.*;

import java.util.List;
import java.util.UUID;

/**
 * 部门服务接口
 */
public interface DepartmentService {

    /**
     * 创建部门
     */
    UUID createDepartment(DeptCreateReq req);

    /**
     * 更新部门
     */
    void updateDepartment(UUID id, DeptUpdateReq req);

    /**
     * 删除部门（逻辑删除）
     */
    void deleteDepartment(UUID deptId);

    /**
     * 根据ID查询部门
     */
    DeptResp getDepartmentById(UUID deptId);

    /**
     * 查询所有部门列表
     */
    List<DeptResp> getAllDepartments();

    /**
     * 查询部门树
     */
    List<DeptResp> getDepartmentTree();

    /**
     * 查询部门及其所有子部门ID
     */
    List<UUID> getDeptAndChildIds(UUID deptId);
}
