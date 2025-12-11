/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.datascope;

import com.movk.common.enums.DataScope;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;
import java.util.UUID;

/**
 * 数据权限 JPA Specification 助手类
 * 用于构建带有数据权限过滤的查询条件
 *
 * <p>使用示例：
 * <pre>
 * // 在 Service 中使用
 * public Page&lt;User&gt; findUsers(UserQueryReq req, Pageable pageable) {
 *     Specification&lt;User&gt; spec = buildSpec(req)
 *         .and(DataPermissionSpecification.withDataPermission("dept", "u"));
 *     return userRepository.findAll(spec, pageable);
 * }
 * </pre>
 */
@Slf4j
public class DataPermissionSpecification {

    private DataPermissionSpecification() {
        // 私有构造函数，防止实例化
    }

    /**
     * 构建数据权限 Specification（使用上下文）
     * 需要配合 @DataPermission 注解使用
     *
     * @param <T> 实体类型
     * @return Specification
     */
    public static <T> Specification<T> withDataPermission() {
        return (root, query, criteriaBuilder) -> {
            DataPermissionContext context = DataPermissionContextHolder.getContext();
            if (context == null) {
                log.debug("数据权限上下文为空，不应用数据权限过滤");
                return null;
            }
            return buildPredicate(root, criteriaBuilder, context);
        };
    }

    /**
     * 构建数据权限 Specification（直接指定参数）
     *
     * @param dataScope      数据权限范围
     * @param userId         当前用户ID
     * @param deptId         当前用户部门ID
     * @param deptIds        可访问的部门ID集合
     * @param deptIdField    部门ID字段名
     * @param creatorField   创建人字段名
     * @param <T>            实体类型
     * @return Specification
     */
    public static <T> Specification<T> withDataPermission(
            DataScope dataScope,
            UUID userId,
            UUID deptId,
            Set<UUID> deptIds,
            String deptIdField,
            String creatorField) {

        return (root, query, criteriaBuilder) -> {
            if (dataScope == null || dataScope == DataScope.ALL) {
                return null; // 全部数据，不添加过滤条件
            }

            switch (dataScope) {
                case DEPT:
                    // 仅本部门数据
                    if (deptId != null) {
                        return criteriaBuilder.equal(root.get(deptIdField), deptId);
                    }
                    return criteriaBuilder.disjunction(); // 无效条件，返回空结果

                case DEPT_AND_CHILD:
                case CUSTOM:
                    // 本部门及子部门 / 自定义部门
                    if (deptIds != null && !deptIds.isEmpty()) {
                        return root.get(deptIdField).in(deptIds);
                    }
                    return criteriaBuilder.disjunction();

                case SELF:
                    // 仅本人数据
                    if (userId != null) {
                        return criteriaBuilder.equal(root.get(creatorField), userId);
                    }
                    return criteriaBuilder.disjunction();

                default:
                    return null;
            }
        };
    }

    /**
     * 构建数据权限 Specification（使用关联表）
     * 适用于需要 JOIN 部门表的场景
     *
     * @param deptJoinAttribute 部门关联属性名
     * @param dataScope         数据权限范围
     * @param userId            当前用户ID
     * @param deptIds           可访问的部门ID集合
     * @param <T>               实体类型
     * @return Specification
     */
    public static <T> Specification<T> withDataPermissionJoin(
            String deptJoinAttribute,
            DataScope dataScope,
            UUID userId,
            Set<UUID> deptIds) {

        return (root, query, criteriaBuilder) -> {
            if (dataScope == null || dataScope == DataScope.ALL) {
                return null;
            }

            if (dataScope == DataScope.SELF) {
                if (userId != null) {
                    return criteriaBuilder.equal(root.get("creator"), userId);
                }
                return criteriaBuilder.disjunction();
            }

            // 需要 JOIN 部门表的场景
            if (deptIds != null && !deptIds.isEmpty()) {
                Join<Object, Object> deptJoin = root.join(deptJoinAttribute, JoinType.LEFT);
                return deptJoin.get("id").in(deptIds);
            }

            return criteriaBuilder.disjunction();
        };
    }

    /**
     * 从上下文构建 Predicate
     */
    private static <T> Predicate buildPredicate(Root<T> root, CriteriaBuilder cb, DataPermissionContext context) {
        DataScope dataScope = context.getDataScope();

        if (dataScope == null || dataScope == DataScope.ALL) {
            return null;
        }

        String deptIdColumn = context.getDeptIdColumn();
        String userIdColumn = context.getUserIdColumn();

        switch (dataScope) {
            case DEPT:
                // 仅本部门数据
                if (context.getDeptId() != null) {
                    return cb.equal(root.get(deptIdColumn), context.getDeptId());
                }
                return cb.disjunction();

            case DEPT_AND_CHILD:
            case CUSTOM:
                // 本部门及子部门 / 自定义部门
                Set<UUID> deptIds = context.getDataScopeDeptIds();
                if (deptIds != null && !deptIds.isEmpty()) {
                    return root.get(deptIdColumn).in(deptIds);
                }
                return cb.disjunction();

            case SELF:
                // 仅本人数据
                if (context.getUserId() != null) {
                    return cb.equal(root.get(userIdColumn), context.getUserId());
                }
                return cb.disjunction();

            default:
                return null;
        }
    }

    /**
     * 组合多个 Specification（AND 逻辑）
     */
    @SafeVarargs
    public static <T> Specification<T> and(Specification<T>... specs) {
        Specification<T> result = Specification.where(null);
        for (Specification<T> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }

    /**
     * 组合多个 Specification（OR 逻辑）
     */
    @SafeVarargs
    public static <T> Specification<T> or(Specification<T>... specs) {
        Specification<T> result = Specification.where(null);
        for (Specification<T> spec : specs) {
            if (spec != null) {
                result = result.or(spec);
            }
        }
        return result;
    }
}
