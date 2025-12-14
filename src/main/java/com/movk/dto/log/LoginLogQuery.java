/*
 * @Author yixuanmiao
 * @Date 2025/12/14
 */

package com.movk.dto.log;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

/**
 * 登录日志查询参数
 */
@Data
public class LoginLogQuery {

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 状态：1-成功 2-失败
     */
    private Short status;

    /**
     * 开始时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime startTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime endTime;
}
