/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.adapters.web.support.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movk.adapters.web.support.ApiRCode;
import com.movk.adapters.web.support.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = ApiResponse.error(ApiRCode.UNAUTHORIZED, null);
        mapper.writeValue(response.getOutputStream(), body);
    }
}
