/*
 * @Author yixuanmiao
 * @Date 2025/08/31 16:35
 */

package com.movk.security.filter;

import com.movk.base.config.JwtHeaderProperties;
import com.movk.security.config.SecurityEndpoints;
import com.movk.security.model.LoginUser;
import com.movk.security.service.JwtService;
import com.movk.security.service.LoginUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final LoginUserDetailsService userDetailsService;
    private final JwtHeaderProperties jwtHeaderProperties;

    public JwtAuthenticationFilter(JwtService jwtService, 
                                   LoginUserDetailsService userDetailsService,
                                   JwtHeaderProperties jwtHeaderProperties) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.jwtHeaderProperties = jwtHeaderProperties;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token) && jwtService.validateToken(token)) {
                String username = jwtService.getUsernameFromToken(token);
                List<String> roles = jwtService.getRolesFromToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (userDetailsService.isUserValid(username)) {
                        LoginUser loginUser = userDetailsService.buildLoginUser(username, roles);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        loginUser,
                                        null,
                                        loginUser.getAuthorities()
                                );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtHeaderProperties.getName());

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtHeaderProperties.getPrefix())) {
            return bearerToken.substring(jwtHeaderProperties.getPrefix().length());
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        for (String pattern : SecurityEndpoints.PUBLIC_APIS) {
            if (matches(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(String pattern, String path) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return pattern.equals(path);
    }
}
