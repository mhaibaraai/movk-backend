/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.repository;

import com.movk.common.enums.ConfigType;
import com.movk.entity.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 系统配置 Repository
 */
@Repository
public interface ConfigRepository extends JpaRepository<Config, UUID> {

    /**
     * 根据配置键查询配置
     */
    Optional<Config> findByConfigKeyAndDeletedFalse(String configKey);

    /**
     * 查询所有未删除的配置
     */
    List<Config> findByDeletedFalseOrderByCreatedAtDesc();

    /**
     * 根据配置类型查询配置
     */
    List<Config> findByConfigTypeAndDeletedFalseOrderByCreatedAtDesc(
        ConfigType configType
    );

    /**
     * 检查配置键是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(c) > 0 FROM Config c " +
           "WHERE c.configKey = :configKey " +
           "AND c.id != :excludeId " +
           "AND c.deleted = false")
    boolean existsByConfigKeyAndIdNot(
        @Param("configKey") String configKey,
        @Param("excludeId") UUID excludeId
    );
}
