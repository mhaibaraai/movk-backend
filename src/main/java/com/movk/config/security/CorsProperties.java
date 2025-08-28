/*
 * @Author yixuanmiao
 * @Date 2025/08/28 21:11
 */

package com.movk.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ConfigurationProperties("app.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>();

}
