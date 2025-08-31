/*
 * @Author yixuanmiao
 * @Date 2025/08/31 16:35
 */

package com.movk.controller.facade;

import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.controller.vo.UserInfoVO;
import com.movk.entity.User;
import com.movk.repository.PermissionRepository;
import com.movk.repository.UserRepository;
import com.movk.security.model.AuthTokensDTO;
import com.movk.security.model.LoginUser;
import com.movk.security.service.LoginUserDetailsService;
import com.movk.security.service.PermissionService;
import com.movk.security.service.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthAppService {

    private final UserRepository userRepository;
    private final LoginUserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;

    public AuthAppService(UserRepository userRepository,
                          LoginUserDetailsService userDetailsService,
                          TokenService tokenService,
                          PermissionService permissionService,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
        this.permissionService = permissionService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthTokensDTO loginAndIssueTokens(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(RCode.INVALID_CREDENTIALS);
        }

        switch (user.getStatus()) {
            case DISABLED -> throw new BusinessException(RCode.USER_DISABLED);
            case LOCKED, DELETED -> throw new BusinessException(RCode.FORBIDDEN);
            case ACTIVE -> {}
            default -> throw new BusinessException(RCode.BUSINESS_ERROR);
        }

        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(user.getUsername());
        AuthTokensDTO tokens = tokenService.generateTokens(loginUser);

        // 更新最后登录时间
        user.setLastLoginAt(java.time.OffsetDateTime.now());
        userRepository.save(user);
        return tokens;
    }

    public UserInfoVO buildUserInfo(LoginUser currentUser) {
        List<String> roles = currentUser.getRoles();
        List<String> permissions = permissionService.getPermissionsByRoles(roles);

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new BusinessException(RCode.USER_NOT_FOUND));

        return UserInfoVO.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .displayName(currentUser.getDisplayName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roles)
                .permissions(permissions)
                .menus(java.util.List.of())
                .build();
    }
}
