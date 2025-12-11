/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.base.filter.TraceIdFilter;
import com.movk.common.enums.BusinessStatus;
import com.movk.common.enums.LoginType;
import com.movk.entity.LoginLog;
import com.movk.repository.LoginLogRepository;
import com.movk.service.LoginLogService;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 登录日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogRepository loginLogRepository;

    @Async("asyncExecutor")
    @Override
    @Transactional
    public void recordLoginSuccess(String username, UUID userId, LoginType loginType, String message) {
        LoginLog loginLog = buildLoginLog(username, loginType, BusinessStatus.SUCCESS, message);
        loginLog.setUserId(userId);
        saveLog(loginLog);
    }

    @Async("asyncExecutor")
    @Override
    @Transactional
    public void recordLoginFailure(String username, LoginType loginType, String message) {
        LoginLog loginLog = buildLoginLog(username, loginType, BusinessStatus.FAILURE, message);
        saveLog(loginLog);
    }

    @Async("asyncExecutor")
    @Override
    @Transactional
    public void recordLogout(String username, UUID userId) {
        LoginLog loginLog = buildLoginLog(username, LoginType.LOGOUT, BusinessStatus.SUCCESS, "用户登出成功");
        loginLog.setUserId(userId);
        saveLog(loginLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoginLog> listLoginLogs(String username, String loginIp, BusinessStatus status,
                                        OffsetDateTime startTime, OffsetDateTime endTime, Pageable pageable) {
        Specification<LoginLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(username)) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            if (StringUtils.hasText(loginIp)) {
                predicates.add(cb.like(root.get("loginIp"), "%" + loginIp + "%"));
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

        return loginLogRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginLog getById(Long id) {
        return loginLogRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginLog getLatestLoginByUsername(String username) {
        return loginLogRepository.findLatestByUsernameAndStatus(username, BusinessStatus.SUCCESS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoginLog> exportLogs(String username, String loginIp, BusinessStatus status,
                                     OffsetDateTime startTime, OffsetDateTime endTime) {
        Specification<LoginLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(username)) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            if (StringUtils.hasText(loginIp)) {
                predicates.add(cb.like(root.get("loginIp"), "%" + loginIp + "%"));
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

        return loginLogRepository.findAll(spec);
    }

    @Override
    @Transactional
    public int cleanLogs(int days) {
        OffsetDateTime beforeDate = OffsetDateTime.now().minusDays(days);
        long countBefore = loginLogRepository.count();
        loginLogRepository.deleteByCreatedAtBefore(beforeDate);
        long countAfter = loginLogRepository.count();
        int deleted = (int) (countBefore - countAfter);
        log.info("清理登录日志完成，共清理 {} 条记录（{}天前）", deleted, days);
        return deleted;
    }

    /**
     * 构建登录日志
     */
    private LoginLog buildLoginLog(String username, LoginType loginType, BusinessStatus status, String message) {
        LoginLog loginLog = new LoginLog();
        loginLog.setTraceId(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY));
        loginLog.setUsername(username);
        loginLog.setLoginType(loginType);
        loginLog.setStatus(status);
        loginLog.setMessage(message);

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            loginLog.setLoginIp(getClientIp(request));
            loginLog.setUserAgent(request.getHeader("User-Agent"));

            // 解析 User-Agent
            UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
            loginLog.setBrowser(userAgent.getBrowser().getName());
            loginLog.setOs(userAgent.getOperatingSystem().getName());
        }

        return loginLog;
    }

    /**
     * 保存日志
     */
    private void saveLog(LoginLog loginLog) {
        try {
            loginLogRepository.save(loginLog);
            log.debug("登录日志保存成功 - username: {}, status: {}", loginLog.getUsername(), loginLog.getStatus());
        } catch (Exception e) {
            log.error("登录日志保存失败 - username: {}, error: {}", loginLog.getUsername(), e.getMessage(), e);
        }
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
