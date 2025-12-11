/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.base.config.CacheConfig;
import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.dto.dept.*;
import com.movk.entity.Department;
import com.movk.entity.User;
import com.movk.repository.DepartmentRepository;
import com.movk.repository.UserRepository;
import com.movk.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 */
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DEPT_TREE, allEntries = true)
    public UUID createDepartment(DeptCreateReq req) {
        if (req.deptCode() != null) {
            if (departmentRepository.findByDeptCodeAndDeletedFalse(req.deptCode()).isPresent()) {
                throw new BusinessException(RCode.BAD_REQUEST, "部门编码已存在");
            }
        }

        String ancestors = buildAncestors(req.parentId());

        Department department = Department.builder()
            .parentId(req.parentId())
            .ancestors(ancestors)
            .deptName(req.deptName())
            .deptCode(req.deptCode())
            .orderNum(req.orderNum())
            .leaderUserId(req.leaderUserId())
            .phone(req.phone())
            .email(req.email())
            .status(req.status())
            .build();

        department = departmentRepository.save(department);
        return department.getId();
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DEPT_TREE, allEntries = true)
    public void updateDepartment(DeptUpdateReq req) {
        Department department = departmentRepository.findById(req.id())
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "部门不存在"));

        if (req.deptCode() != null && !req.deptCode().equals(department.getDeptCode())) {
            if (departmentRepository.existsByDeptCodeAndIdNot(req.deptCode(), req.id())) {
                throw new BusinessException(RCode.BAD_REQUEST, "部门编码已存在");
            }
        }

        String ancestors = buildAncestors(req.parentId());

        department.setParentId(req.parentId());
        department.setAncestors(ancestors);
        department.setDeptName(req.deptName());
        department.setDeptCode(req.deptCode());
        department.setOrderNum(req.orderNum());
        department.setLeaderUserId(req.leaderUserId());
        department.setPhone(req.phone());
        department.setEmail(req.email());
        department.setStatus(req.status());

        departmentRepository.save(department);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DEPT_TREE, allEntries = true)
    public void deleteDepartment(UUID deptId) {
        Department department = departmentRepository.findById(deptId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "部门不存在"));

        List<Department> children = departmentRepository.findByParentIdAndDeletedFalseOrderByOrderNumAsc(deptId);
        if (!children.isEmpty()) {
            throw new BusinessException(RCode.BAD_REQUEST, "存在子部门，无法删除");
        }

        department.setDeleted(true);
        department.setDeletedAt(OffsetDateTime.now());
        departmentRepository.save(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DeptResp getDepartmentById(UUID deptId) {
        Department department = departmentRepository.findById(deptId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "部门不存在"));

        return toDeptResp(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeptResp> getAllDepartments() {
        List<Department> departments = departmentRepository.findByDeletedFalseOrderByOrderNumAsc();
        return departments.stream()
            .map(this::toDeptResp)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.DEPT_TREE, key = "'all'")
    public List<DeptResp> getDepartmentTree() {
        List<Department> allDepartments = departmentRepository.findByDeletedFalseOrderByOrderNumAsc();
        return buildDepartmentTree(allDepartments, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getDeptAndChildIds(UUID deptId) {
        return departmentRepository.findDeptAndChildIds(deptId);
    }

    private DeptResp toDeptResp(Department dept) {
        String leaderUserName = null;
        if (dept.getLeaderUserId() != null) {
            leaderUserName = userRepository.findById(dept.getLeaderUserId())
                .map(User::getUsername)
                .orElse(null);
        }

        return new DeptResp(
            dept.getId(),
            dept.getParentId(),
            dept.getDeptName(),
            dept.getDeptCode(),
            dept.getOrderNum(),
            dept.getLeaderUserId(),
            leaderUserName,
            dept.getPhone(),
            dept.getEmail(),
            dept.getStatus(),
            dept.getCreatedAt(),
            dept.getUpdatedAt(),
            null
        );
    }

    private List<DeptResp> buildDepartmentTree(List<Department> allDepartments, UUID parentId) {
        return allDepartments.stream()
            .filter(dept -> Objects.equals(dept.getParentId(), parentId))
            .map(dept -> {
                List<DeptResp> children = buildDepartmentTree(allDepartments, dept.getId());
                String leaderUserName = null;
                if (dept.getLeaderUserId() != null) {
                    leaderUserName = userRepository.findById(dept.getLeaderUserId())
                        .map(User::getUsername)
                        .orElse(null);
                }
                return new DeptResp(
                    dept.getId(),
                    dept.getParentId(),
                    dept.getDeptName(),
                    dept.getDeptCode(),
                    dept.getOrderNum(),
                    dept.getLeaderUserId(),
                    leaderUserName,
                    dept.getPhone(),
                    dept.getEmail(),
                    dept.getStatus(),
                    dept.getCreatedAt(),
                    dept.getUpdatedAt(),
                    children.isEmpty() ? null : children
                );
            })
            .collect(Collectors.toList());
    }

    private String buildAncestors(UUID parentId) {
        if (parentId == null) {
            return ",";
        }

        Department parent = departmentRepository.findById(parentId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "父部门不存在"));

        return parent.getAncestors() + parentId + ",";
    }
}
