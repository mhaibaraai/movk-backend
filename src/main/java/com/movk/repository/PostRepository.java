/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.repository;

import com.movk.common.enums.EnableStatus;
import com.movk.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 岗位 Repository
 */
@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    /**
     * 查询所有未删除的岗位，按排序号排序
     */
    List<Post> findByDeletedFalseOrderByOrderNumAsc();

    /**
     * 根据岗位编码查询岗位
     */
    Optional<Post> findByPostCodeAndDeletedFalse(String postCode);

    /**
     * 根据状态查询岗位
     */
    List<Post> findByStatusAndDeletedFalseOrderByOrderNumAsc(EnableStatus status);

    /**
     * 检查岗位编码是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(p) > 0 FROM Post p " +
           "WHERE p.postCode = :postCode " +
           "AND p.id != :excludeId " +
           "AND p.deleted = false")
    boolean existsByPostCodeAndIdNot(
        @Param("postCode") String postCode,
        @Param("excludeId") UUID excludeId
    );

    /**
     * 统计指定ID列表中存在的岗位数量
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.id IN :ids AND p.deleted = false")
    long countByIdIn(@Param("ids") List<UUID> ids);
}
