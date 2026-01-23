/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

import com.movk.dto.notice.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * 通知公告服务接口
 */
public interface NoticeService {

    /**
     * 创建通知公告
     */
    UUID createNotice(NoticeCreateReq req);

    /**
     * 更新通知公告
     */
    void updateNotice(UUID id, NoticeUpdateReq req);

    /**
     * 删除通知公告（逻辑删除）
     */
    void deleteNotice(UUID noticeId);

    /**
     * 根据ID查询通知公告
     */
    NoticeResp getNoticeById(UUID noticeId);

    /**
     * 分页查询通知公告列表
     */
    Page<NoticeResp> getNoticePage(Pageable pageable);

    /**
     * 查询已发布的通知公告列表（前端展示）
     */
    List<NoticeResp> getPublishedNotices();
}
