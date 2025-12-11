/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

/**
 * IP 地理位置解析服务接口
 */
public interface IpLocationService {

    /**
     * 根据 IP 地址获取地理位置
     *
     * @param ip IP 地址
     * @return 地理位置描述（如：中国 北京市）
     */
    String getLocation(String ip);

    /**
     * 判断是否为内网 IP
     */
    boolean isInternalIp(String ip);
}
