/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.repository;

import com.movk.common.enums.EnableStatus;
import com.movk.entity.DictData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 字典数据 Repository
 */
@Repository
public interface DictDataRepository extends JpaRepository<DictData, UUID> {

    /**
     * 根据字典类型查询字典数据
     */
    List<DictData> findByDictTypeAndDeletedFalseOrderByDictSortAsc(String dictType);

    /**
     * 根据字典类型和状态查询字典数据
     */
    List<DictData> findByDictTypeAndStatusAndDeletedFalseOrderByDictSortAsc(
        String dictType, EnableStatus status
    );

    /**
     * 查询所有未删除的字典数据
     */
    List<DictData> findByDeletedFalseOrderByDictTypeAscDictSortAsc();

    /**
     * 根据字典类型删除所有关联的字典数据
     */
    void deleteByDictType(String dictType);

    /**
     * 查询字典类型的默认值
     */
    List<DictData> findByDictTypeAndIsDefaultAndDeletedFalse(
        String dictType, Boolean isDefault
    );
}
