/*
 * @Author yixuanmiao
 * @Date 2025/08/26 22:18
 */

package com.movk.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI movkOpenAPI() {
        Info info = new Info()
                .title("Movk Backend API")
                .version("v1");

        SecurityScheme bearerJwt = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityScheme githubOAuth2 = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                        .authorizationUrl("https://github.com/login/oauth/authorize")
                        .tokenUrl("https://github.com/login/oauth/access_token")
                        .scopes(new Scopes()
                                .addString("read:user", "Read GitHub user profile")
                                .addString("user:email", "Read GitHub user email")
                        )));

        Components components = new Components()
                .addSecuritySchemes("bearer-jwt", bearerJwt)
                .addSecuritySchemes("oauth2-github", githubOAuth2);

        return new OpenAPI()
                .info(info)
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}




