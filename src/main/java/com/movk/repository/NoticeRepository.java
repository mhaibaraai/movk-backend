/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.repository;

import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.NoticeType;
import com.movk.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 通知公告 Repository
 */
@Repository
public interface NoticeRepository extends JpaRepository<Notice, UUID> {

    /**
     * 查询所有未删除的通知，按创建时间降序
     */
    List<Notice> findByDeletedFalseOrderByCreatedAtDesc();

    /**
     * 根据通知类型查询通知
     */
    List<Notice> findByNoticeTypeAndDeletedFalseOrderByCreatedAtDesc(
        NoticeType noticeType
    );

    /**
     * 根据状态查询通知
     */
    List<Notice> findByStatusAndDeletedFalseOrderByCreatedAtDesc(
        EnableStatus status
    );

    /**
     * 根据通知类型和状态查询通知
     */
    List<Notice> findByNoticeTypeAndStatusAndDeletedFalseOrderByCreatedAtDesc(
        NoticeType noticeType, EnableStatus status
    );
}
