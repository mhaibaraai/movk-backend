/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.entity;

import com.movk.common.converter.BusinessStatusConverter;
import com.movk.common.enums.BusinessStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 操作日志实体
 */
@Entity
@Table(name = "sys_operate_log")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class OperateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 50)
    private String username;

    @Column(length = 50)
    private String module;

    @Column(length = 50)
    private String operation;

    @Column(length = 200)
    private String method;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_url", length = 500)
    private String requestUrl;

    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    @Column(name = "user_ip", length = 50)
    private String userIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "operation_time")
    private Integer operationTime;

    @Convert(converter = BusinessStatusConverter.class)
    @Column(nullable = false)
    private BusinessStatus status;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
