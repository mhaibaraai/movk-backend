/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.common.enums.EnableStatus;
import com.movk.dto.notice.*;
import com.movk.entity.Notice;
import com.movk.repository.NoticeRepository;
import com.movk.service.NoticeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 通知公告服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    @Override
    @Transactional
    public UUID createNotice(NoticeCreateReq req) {
        Notice notice = Notice.builder()
            .noticeTitle(req.noticeTitle())
            .noticeType(req.noticeType())
            .noticeContent(req.noticeContent())
            .status(req.status() != null ? req.status() : EnableStatus.ENABLED)
            .build();

        noticeRepository.save(notice);
        log.info("创建通知公告成功: {}", notice.getNoticeTitle());
        return notice.getId();
    }

    @Override
    @Transactional
    public void updateNotice(UUID id, NoticeUpdateReq req) {
        Notice notice = noticeRepository.findById(id)
            .filter(n -> !n.getDeleted())
            .orElseThrow(() -> new EntityNotFoundException("通知公告不存在"));

        notice.setNoticeTitle(req.noticeTitle());
        notice.setNoticeType(req.noticeType());
        notice.setNoticeContent(req.noticeContent());
        notice.setStatus(req.status());

        noticeRepository.save(notice);
        log.info("更新通知公告成功: {}", notice.getNoticeTitle());
    }

    @Override
    @Transactional
    public void deleteNotice(UUID noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
            .filter(n -> !n.getDeleted())
            .orElseThrow(() -> new EntityNotFoundException("通知公告不存在"));

        notice.setDeleted(true);
        notice.setDeletedAt(OffsetDateTime.now());
        noticeRepository.save(notice);

        log.info("删除通知公告成功: {}", notice.getNoticeTitle());
    }

    @Override
    public NoticeResp getNoticeById(UUID noticeId) {
        return noticeRepository.findById(noticeId)
            .filter(n -> !n.getDeleted())
            .map(this::toResp)
            .orElseThrow(() -> new EntityNotFoundException("通知公告不存在"));
    }

    @Override
    public Page<NoticeResp> getNoticePage(Pageable pageable) {
        Page<Notice> noticePage = noticeRepository.findByDeletedFalse(pageable);
        return noticePage.map(this::toResp);
    }

    @Override
    public List<NoticeResp> getPublishedNotices() {
        return noticeRepository.findByStatusAndDeletedFalseOrderByCreatedAtDesc(EnableStatus.ENABLED)
            .stream()
            .map(this::toResp)
            .collect(Collectors.toList());
    }

    // ========== 转换方法 ==========

    private NoticeResp toResp(Notice entity) {
        return new NoticeResp(
            entity.getId(),
            entity.getNoticeTitle(),
            entity.getNoticeType(),
            entity.getNoticeContent(),
            entity.getStatus(),
            entity.getCreator(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
