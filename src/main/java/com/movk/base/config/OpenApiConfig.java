/*
 * @Author yixuanmiao
 * @Date 2025/09/01 14:14
 */

package com.movk.base.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 文档配置
 */
@Configuration
public class OpenApiConfig {

    private final JwtHeaderProperties jwtHeaderProperties;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public OpenApiConfig(JwtHeaderProperties jwtHeaderProperties) {
        this.jwtHeaderProperties = jwtHeaderProperties;
    }

    @Bean
    public OpenAPI movkOpenAPI() {
        Info info = new Info()
            .title("Movk RBAC 权限管理系统 API")
            .version("v1.0.0")
            .description("""
                ## 系统简介
                Movk Backend 是一个基于 Spring Boot 3.x 的 RBAC 权限管理系统后端服务。
                
                ## 主要功能
                - **用户管理**: 用户增删改查、角色分配、密码重置
                - **角色管理**: 角色增删改查、菜单权限分配、数据权限配置
                - **菜单管理**: 菜单增删改查、权限标识管理
                - **部门管理**: 部门树管理、数据权限隔离
                - **岗位管理**: 岗位增删改查
                - **字典管理**: 字典类型和数据管理
                - **系统配置**: 系统参数配置管理
                - **操作日志**: 用户操作记录查询
                - **登录日志**: 登录记录查询
                - **在线用户**: 在线用户管理、强制下线
                
                ## 认证方式
                使用 JWT Token 进行身份认证，请在请求头中携带 `Authorization: Bearer <token>`
                """)
            .contact(new Contact()
                .name("yixuanmiao")
                .email("admin@mhaibaraai.cn"))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT"));

        // 定义自定义 JWT 安全方案
        final String securitySchemeName = "BearerAuth";
        SecurityScheme auth = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name(jwtHeaderProperties.getName())
            .description("JWT 认证，格式: Bearer <token>");

        // 服务器配置
        List<Server> servers = List.of(
            new Server().url("http://localhost:36600").description("开发环境"),
            new Server().url("https://server.mhaibaraai.cn").description("生产环境")
        );

        return new OpenAPI()
            .info(info)
            .servers(servers)
            .components(new Components().addSecuritySchemes(securitySchemeName, auth))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
