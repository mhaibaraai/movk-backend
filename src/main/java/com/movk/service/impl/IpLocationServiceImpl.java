/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.service.IpLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * IP 地理位置解析服务实现
 * 
 * 当前为简化实现，仅识别内网 IP。
 * 如需完整 IP 归属地解析，可接入：
 * - ip2region（离线库）
 * - MaxMind GeoIP2（离线库）
 * - 高德/百度/腾讯地图 API（在线查询）
 */
@Slf4j
@Service
public class IpLocationServiceImpl implements IpLocationService {

    /**
     * 内网 IP 正则
     */
    private static final Pattern INTERNAL_IP_PATTERN = Pattern.compile(
        "^(127\\.)|(10\\.)|(172\\.(1[6-9]|2[0-9]|3[0-1])\\.)|(192\\.168\\.)"
    );

    /**
     * IPv6 本地回环地址
     */
    private static final String IPV6_LOCALHOST = "0:0:0:0:0:0:0:1";

    @Override
    public String getLocation(String ip) {
        if (ip == null || ip.isBlank()) {
            return "未知";
        }

        // 内网 IP
        if (isInternalIp(ip)) {
            return "内网IP";
        }

        // 外网 IP 暂返回未知，后续可接入 IP 库
        return "未知";
    }

    @Override
    public boolean isInternalIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }

        // localhost
        if ("localhost".equalsIgnoreCase(ip)) {
            return true;
        }

        // IPv6 本地回环
        if (IPV6_LOCALHOST.equals(ip) || "::1".equals(ip)) {
            return true;
        }

        // 内网 IP 段匹配
        return INTERNAL_IP_PATTERN.matcher(ip).find();
    }
}
