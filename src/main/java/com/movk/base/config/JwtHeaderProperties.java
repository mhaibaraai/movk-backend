/*
 * @Author yixuanmiao
 * @Date 2025/08/31 03:04
 */

package com.movk.base.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "jwt.header")
public class JwtHeaderProperties {

    private String name = "X-Movk-Auth";
    private String prefix = "Bearer ";
}
