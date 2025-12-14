/*
 * @Author yixuanmiao
 * @Date 2025/12/14
 */

package com.movk.repository;

import com.movk.entity.SysFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 系统文件 Repository
 */
@Repository
public interface SysFileRepository extends JpaRepository<SysFile, UUID> {

    /**
     * 根据 ID 查找未删除的文件
     */
    Optional<SysFile> findByIdAndDeletedFalse(UUID id);

    /**
     * 根据 MD5 查找未删除的文件
     */
    Optional<SysFile> findByMd5AndDeletedFalse(String md5);

    /**
     * 根据存储名称查找文件
     */
    Optional<SysFile> findByStorageNameAndDeletedFalse(String storageName);

    /**
     * 分页查询文件列表
     */
    @Query("""
            SELECT f FROM SysFile f
            WHERE f.deleted = false
            AND (:originalName IS NULL OR f.originalName LIKE %:originalName%)
            AND (:category IS NULL OR f.category = :category)
            AND (:contentType IS NULL OR f.contentType LIKE %:contentType%)
            """)
    Page<SysFile> findByConditions(
            @Param("originalName") String originalName,
            @Param("category") String category,
            @Param("contentType") String contentType,
            Pageable pageable
    );

    /**
     * 根据 ID 列表批量查询
     */
    List<SysFile> findByIdInAndDeletedFalse(List<UUID> ids);

    /**
     * 根据分类查询文件列表
     */
    List<SysFile> findByCategoryAndDeletedFalse(String category);
}
