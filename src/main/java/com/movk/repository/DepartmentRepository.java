/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.repository;

import com.movk.common.enums.EnableStatus;
import com.movk.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 部门 Repository
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    /**
     * 查询所有未删除的部门，按排序号排序
     */
    List<Department> findByDeletedFalseOrderByOrderNumAsc();

    /**
     * 根据父部门ID查询子部门
     */
    List<Department> findByParentIdAndDeletedFalseOrderByOrderNumAsc(UUID parentId);

    /**
     * 根据部门编码查询部门
     */
    Optional<Department> findByDeptCodeAndDeletedFalse(String deptCode);

    /**
     * 根据状态查询部门
     */
    List<Department> findByStatusAndDeletedFalseOrderByOrderNumAsc(EnableStatus status);

    /**
     * 查询部门及其所有子部门ID
     */
    @Query("SELECT d.id FROM Department d " +
           "WHERE d.deleted = false " +
           "AND (d.id = :deptId OR d.ancestors LIKE CONCAT('%,', :deptId, ',%'))")
    List<UUID> findDeptAndChildIds(@Param("deptId") UUID deptId);

    /**
     * 检查部门编码是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(d) > 0 FROM Department d " +
           "WHERE d.deptCode = :deptCode " +
           "AND d.id != :excludeId " +
           "AND d.deleted = false")
    boolean existsByDeptCodeAndIdNot(
        @Param("deptCode") String deptCode,
        @Param("excludeId") UUID excludeId
    );
}
