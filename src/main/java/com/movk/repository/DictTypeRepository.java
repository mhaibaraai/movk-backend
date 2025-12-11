/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.repository;

import com.movk.common.enums.EnableStatus;
import com.movk.entity.DictType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 字典类型 Repository
 */
@Repository
public interface DictTypeRepository extends JpaRepository<DictType, UUID> {

    /**
     * 根据字典类型查询
     */
    Optional<DictType> findByDictTypeAndDeletedFalse(String dictType);

    /**
     * 查询所有未删除的字典类型
     */
    List<DictType> findByDeletedFalseOrderByCreatedAtDesc();

    /**
     * 根据状态查询字典类型
     */
    List<DictType> findByStatusAndDeletedFalseOrderByCreatedAtDesc(EnableStatus status);

    /**
     * 检查字典类型是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(dt) > 0 FROM DictType dt " +
           "WHERE dt.dictType = :dictType " +
           "AND dt.id != :excludeId " +
           "AND dt.deleted = false")
    boolean existsByDictTypeAndIdNot(
        @Param("dictType") String dictType,
        @Param("excludeId") UUID excludeId
    );
}
