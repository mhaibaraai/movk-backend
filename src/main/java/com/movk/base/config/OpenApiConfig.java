/*
 * @Author yixuanmiao
 * @Date 2025/08/31 16:35
 */

package com.movk.base.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private final JwtHeaderProperties jwtHeaderProperties;

    public OpenApiConfig(JwtHeaderProperties jwtHeaderProperties) {
        this.jwtHeaderProperties = jwtHeaderProperties;
    }

    @Bean
    public OpenAPI movkOpenAPI() {
        Info info = new Info()
                .title("Movk Backend API")
                .version("v1");

        // 定义自定义 JWT 安全方案
        final String securitySchemeName = "x-movk-auth";
        SecurityScheme auth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(jwtHeaderProperties.getName())
                .description("请输入JWT token，格式为: " + jwtHeaderProperties.getPrefix() + "<token>");

        return new OpenAPI()
                .info(info)
                .components(new Components().addSecuritySchemes(securitySchemeName, auth))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
