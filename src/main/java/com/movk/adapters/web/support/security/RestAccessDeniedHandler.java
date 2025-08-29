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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = ApiResponse.error(ApiRCode.FORBIDDEN, null);
        mapper.writeValue(response.getOutputStream(), body);
    }
}
