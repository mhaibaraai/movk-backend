/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.base.config.CacheConfig;
import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.dto.role.*;
import com.movk.entity.Department;
import com.movk.entity.Menu;
import com.movk.entity.Role;
import com.movk.entity.RoleMenu;
import com.movk.entity.id.RoleMenuId;
import com.movk.repository.DepartmentRepository;
import com.movk.repository.MenuRepository;
import com.movk.repository.RoleMenuRepository;
import com.movk.repository.RoleRepository;
import com.movk.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final MenuRepository menuRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public UUID createRole(RoleCreateReq req) {
        if (roleRepository.findByCodeIn(List.of(req.code())).stream().findAny().isPresent()) {
            throw new BusinessException(RCode.BAD_REQUEST, "角色编码已存在");
        }

        String dataScopeDeptIdsStr = null;
        if (req.dataScopeDeptIds() != null && !req.dataScopeDeptIds().isEmpty()) {
            dataScopeDeptIdsStr = req.dataScopeDeptIds().stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));
        }

        Role role = Role.builder()
            .code(req.code())
            .name(req.name())
            .roleSort(req.roleSort())
            .dataScope(req.dataScope())
            .dataScopeDeptIds(dataScopeDeptIdsStr)
            .status(req.status())
            .roleType(req.roleType())
            .remark(req.remark())
            .build();

        role = roleRepository.save(role);

        if (req.menuIds() != null && !req.menuIds().isEmpty()) {
            assignMenus(role.getId(), req.menuIds());
        }

        return role.getId();
    }

    @Override
    @Transactional
    public void updateRole(RoleUpdateReq req) {
        Role role = roleRepository.findById(req.id())
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "角色不存在"));

        String dataScopeDeptIdsStr = null;
        if (req.dataScopeDeptIds() != null && !req.dataScopeDeptIds().isEmpty()) {
            dataScopeDeptIdsStr = req.dataScopeDeptIds().stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));
        }

        role.setName(req.name());
        role.setRoleSort(req.roleSort());
        role.setDataScope(req.dataScope());
        role.setDataScopeDeptIds(dataScopeDeptIdsStr);
        role.setStatus(req.status());
        role.setRoleType(req.roleType());
        role.setRemark(req.remark());

        roleRepository.save(role);

        if (req.menuIds() != null) {
            assignMenus(role.getId(), req.menuIds());
        }
    }

    @Override
    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "角色不存在"));

        role.setDeleted(true);
        role.setDeletedAt(OffsetDateTime.now());
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteRoles(Iterable<UUID> roleIds) {
        for (UUID roleId : roleIds) {
            deleteRole(roleId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResp getRoleById(UUID roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "角色不存在"));

        List<UUID> dataScopeDeptIds = parseDataScopeDeptIds(role.getDataScopeDeptIds());

        return new RoleResp(
            role.getId(),
            role.getCode(),
            role.getName(),
            role.getRoleSort(),
            role.getDataScope(),
            dataScopeDeptIds,
            role.getStatus(),
            role.getRoleType(),
            role.getRemark(),
            role.getCreatedAt(),
            role.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetailResp getRoleDetail(UUID roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "角色不存在"));

        List<UUID> dataScopeDeptIds = parseDataScopeDeptIds(role.getDataScopeDeptIds());
        List<String> dataScopeDeptNames = new ArrayList<>();

        if (!dataScopeDeptIds.isEmpty()) {
            List<Department> depts = departmentRepository.findAllById(dataScopeDeptIds);
            dataScopeDeptNames = depts.stream()
                .map(Department::getDeptName)
                .collect(Collectors.toList());
        }

        List<UUID> menuIds = roleMenuRepository.findMenuIdsByRoleId(roleId);

        return new RoleDetailResp(
            role.getId(),
            role.getCode(),
            role.getName(),
            role.getRoleSort(),
            role.getDataScope(),
            dataScopeDeptIds,
            dataScopeDeptNames,
            role.getStatus(),
            role.getRoleType(),
            menuIds,
            role.getRemark(),
            role.getCreatedAt(),
            role.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResp> getRolePage(RoleQueryReq queryReq, Pageable pageable) {
        Specification<Role> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            if (queryReq.code() != null && !queryReq.code().isBlank()) {
                predicates.add(cb.like(root.get("code"), "%" + queryReq.code() + "%"));
            }

            if (queryReq.name() != null && !queryReq.name().isBlank()) {
                predicates.add(cb.like(root.get("name"), "%" + queryReq.name() + "%"));
            }

            if (queryReq.status() != null) {
                predicates.add(cb.equal(root.get("status"), queryReq.status()));
            }

            if (queryReq.roleType() != null) {
                predicates.add(cb.equal(root.get("roleType"), queryReq.roleType()));
            }

            if (queryReq.createdAtStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), queryReq.createdAtStart()));
            }

            if (queryReq.createdAtEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), queryReq.createdAtEnd()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Role> rolePage = roleRepository.findAll(spec, pageable);

        List<RoleResp> roleResps = rolePage.getContent().stream()
            .map(role -> {
                List<UUID> dataScopeDeptIds = parseDataScopeDeptIds(role.getDataScopeDeptIds());
                return new RoleResp(
                    role.getId(),
                    role.getCode(),
                    role.getName(),
                    role.getRoleSort(),
                    role.getDataScope(),
                    dataScopeDeptIds,
                    role.getStatus(),
                    role.getRoleType(),
                    role.getRemark(),
                    role.getCreatedAt(),
                    role.getUpdatedAt()
                );
            })
            .collect(Collectors.toList());

        return new PageImpl<>(roleResps, pageable, rolePage.getTotalElements());
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.USER_PERMISSIONS, allEntries = true),
        @CacheEvict(value = CacheConfig.USER_MENUS, allEntries = true)
    })
    public void assignMenus(UUID roleId, Iterable<UUID> menuIds) {
        if (!roleRepository.existsById(roleId)) {
            throw new BusinessException(RCode.NOT_FOUND, "角色不存在");
        }

        roleMenuRepository.deleteByRoleId(roleId);

        Role role = roleRepository.findById(roleId).orElseThrow();

        for (UUID menuId : menuIds) {
            Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "菜单不存在: " + menuId));

            RoleMenu roleMenu = RoleMenu.builder()
                .id(new RoleMenuId(roleId, menuId))
                .role(role)
                .menu(menu)
                .build();

            roleMenuRepository.save(roleMenu);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return roleRepository.findByCodeIn(List.of(code)).stream().findAny().isPresent();
    }

    private List<UUID> parseDataScopeDeptIds(String dataScopeDeptIdsStr) {
        if (dataScopeDeptIdsStr == null || dataScopeDeptIdsStr.isBlank()) {
            return List.of();
        }

        return java.util.Arrays.stream(dataScopeDeptIdsStr.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(UUID::fromString)
            .collect(Collectors.toList());
    }
}
