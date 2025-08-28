/*
 * @Author yixuanmiao
 * @Date 2025/08/28 10:35
 */

package com.movk.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("security.password")
public class SecurityPasswordProperties {
    // 支持的加密算法，当前仅支持 bcrypt
    private String encoder = "bcrypt";
    // bcrypt 强度，默认10
    private Integer strength = 10;
    // 全局 pepper，用于增强密码安全性
    private String pepper;
}
