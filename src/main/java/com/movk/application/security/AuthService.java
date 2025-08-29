/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.application.security;

import com.movk.adapters.persistence.rbac.entity.User;
import com.movk.adapters.persistence.rbac.repository.UserRepository;
import com.movk.adapters.web.auth.dto.TokenResponse;
import com.movk.adapters.web.support.ApiRCode;
import com.movk.adapters.web.support.BusinessException;
import com.movk.domain.rbac.model.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordService passwordService,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public TokenResponse loginByEmail(String email, String rawPassword) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException(ApiRCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ENABLED) {
            throw new BusinessException(ApiRCode.USER_DISABLED);
        }

        if (!passwordService.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(ApiRCode.INVALID_CREDENTIALS);
        }

        UUID userId = user.getId();
        String access = jwtService.generateAccessToken(userId, email);
        return new TokenResponse(access, "Bearer", getAccessTtl().toSeconds());
    }

    private Duration getAccessTtl() {
        return Duration.parse("PT15M");
    }

    private static Duration remainDuration(Instant exp) {
        Instant now = Instant.now();
        if (exp.isBefore(now)) {
            return Duration.ZERO;
        }
        return Duration.between(now, exp);
    }
}
