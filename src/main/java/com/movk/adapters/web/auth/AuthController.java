/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.adapters.web.auth;

import com.movk.adapters.web.auth.dto.LoginRequest;
import com.movk.adapters.web.auth.dto.TokenResponse;
import com.movk.adapters.web.support.ApiResponse;
import com.movk.application.security.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokens = authService.loginByEmail(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(tokens));
    }
}
