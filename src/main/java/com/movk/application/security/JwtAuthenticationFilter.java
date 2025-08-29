/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.application.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtService.getBearerToken(request);
        if (token == null || token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Jws<Claims> jws = jwtService.parseAndValidate(token);
            Claims claims = jws.getBody();
            if (!"access".equals(claims.get("typ"))) {
                filterChain.doFilter(request, response);
                return;
            }

            // 精简：不做黑名单与单设备校验

            var auth = new UsernamePasswordAuthenticationToken(claims.get("email"), null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ignored) {
            // 交由后续链路/异常处理器统一处理未认证/鉴权失败场景
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith(request.getContextPath() + "/auth/")
                || path.startsWith(request.getContextPath() + "/v3/api-docs")
                || path.startsWith(request.getContextPath() + "/swagger-ui");
    }
}
