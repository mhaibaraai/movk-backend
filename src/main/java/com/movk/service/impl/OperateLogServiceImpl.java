/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.common.enums.BusinessStatus;
import com.movk.entity.OperateLog;
import com.movk.repository.OperateLogRepository;
import com.movk.service.OperateLogService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 操作日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperateLogServiceImpl implements OperateLogService {

    private final OperateLogRepository operateLogRepository;

    @Async("asyncExecutor")
    @Override
    @Transactional
    public void saveLogAsync(OperateLog operateLog) {
        try {
            operateLogRepository.save(operateLog);
            log.info("操作日志保存成功 - traceId: {}, userId: {}, username: {}, module: {}, operation: {}, url: {}",
                operateLog.getTraceId(), operateLog.getUserId(), operateLog.getUsername(),
                operateLog.getModule(), operateLog.getOperation(), operateLog.getRequestUrl());
        } catch (Exception e) {
            log.error("操作日志保存失败 - traceId: {}, error: {}",
                operateLog.getTraceId(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OperateLog> listOperateLogs(UUID userId, String module, String operation,
                                            BusinessStatus status, OffsetDateTime startTime,
                                            OffsetDateTime endTime, Pageable pageable) {
        Specification<OperateLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (StringUtils.hasText(module)) {
                predicates.add(cb.like(root.get("module"), "%" + module + "%"));
            }
            if (StringUtils.hasText(operation)) {
                predicates.add(cb.equal(root.get("operation"), operation));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return operateLogRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public OperateLog getById(Long id) {
        return operateLogRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperateLog> listByUserId(UUID userId) {
        return operateLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public int cleanLogs(int days) {
        OffsetDateTime beforeDate = OffsetDateTime.now().minusDays(days);
        long countBefore = operateLogRepository.count();
        operateLogRepository.deleteByCreatedAtBefore(beforeDate);
        long countAfter = operateLogRepository.count();
        int deleted = (int) (countBefore - countAfter);
        log.info("清理操作日志完成，共清理 {} 条记录（{}天前）", deleted, days);
        return deleted;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperateLog> exportLogs(UUID userId, String module, BusinessStatus status,
                                       OffsetDateTime startTime, OffsetDateTime endTime) {
        Specification<OperateLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (StringUtils.hasText(module)) {
                predicates.add(cb.like(root.get("module"), "%" + module + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return operateLogRepository.findAll(spec);
    }
}
