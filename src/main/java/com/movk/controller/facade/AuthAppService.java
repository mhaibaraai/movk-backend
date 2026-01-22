package com.movk.controller.facade;

import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.common.enums.LoginType;
import com.movk.common.enums.UserStatus;
import com.movk.controller.vo.UserInfoVO;
import com.movk.dto.menu.MenuTreeResp;
import com.movk.entity.User;
import com.movk.repository.UserRepository;
import com.movk.security.model.AuthTokensDTO;
import com.movk.security.model.LoginUser;
import com.movk.security.service.LoginUserDetailsService;
import com.movk.security.service.PermissionService;
import com.movk.security.service.TokenService;
import com.movk.service.LoginLogService;
import com.movk.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 认证应用服务
 * 编排登录、注册等认证相关的业务流程
 */
@Service
@RequiredArgsConstructor
public class AuthAppService {

    private final UserRepository userRepository;
    private final LoginUserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final PermissionService permissionService;
    private final MenuService menuService;
    private final PasswordEncoder passwordEncoder;
    private final LoginLogService loginLogService;

    /**
     * 登录并签发 Token
     *
     * @param email      邮箱
     * @param password   密码
     * @param rememberMe 是否记住我
     * @return 双 Token 响应
     */
    public AuthTokensDTO loginAndIssueTokens(String email, String password, boolean rememberMe) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            loginLogService.recordLoginFailure(email, LoginType.PASSWORD, "用户名或密码错误");
            throw new BusinessException(RCode.INVALID_CREDENTIALS);
        }

        validateUserStatus(user);

        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(user.getUsername());
        AuthTokensDTO tokens = tokenService.generateTokenPair(loginUser, rememberMe);

        // 更新最后登录时间
        user.setLoginDate(OffsetDateTime.now());
        userRepository.save(user);

        loginLogService.recordLoginSuccess(user.getUsername(), user.getId(), LoginType.PASSWORD, "登录成功");

        return tokens;
    }

    /**
     * 注册并签发 Token
     */
    @Transactional
    public AuthTokensDTO registerAndIssueTokens(String email, String password, String nickname) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(RCode.BAD_REQUEST, "邮箱已被使用");
        }

        String username = generateUsernameFromEmail(email);

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(nickname != null && !nickname.isBlank() ? nickname : username)
                .email(email)
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        loginLogService.recordLoginSuccess(user.getUsername(), user.getId(), LoginType.PASSWORD, "注册成功");

        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(user.getUsername());
        return tokenService.generateTokenPair(loginUser, false);
    }

    /**
     * 构建用户信息 VO
     */
    public UserInfoVO buildUserInfo(LoginUser currentUser) {
        List<String> roles = currentUser.getRoles();
        List<String> permissions = permissionService.getPermissionsByRoles(roles);
        List<MenuTreeResp> menus = menuService.getUserMenuTree(currentUser.getId());

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new BusinessException(RCode.USER_NOT_FOUND));

        return UserInfoVO.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .nickname(currentUser.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roles)
                .permissions(permissions)
                .menus(menus)
                .build();
    }

    /**
     * 验证用户状态
     */
    private void validateUserStatus(User user) {
        switch (user.getStatus()) {
            case DISABLED -> {
                loginLogService.recordLoginFailure(user.getUsername(), LoginType.PASSWORD, "用户已被禁用");
                throw new BusinessException(RCode.USER_DISABLED);
            }
            case LOCKED, DELETED -> {
                loginLogService.recordLoginFailure(user.getUsername(), LoginType.PASSWORD, "用户已被锁定或删除");
                throw new BusinessException(RCode.FORBIDDEN);
            }
            case ACTIVE -> {}
            default -> {
                loginLogService.recordLoginFailure(user.getUsername(), LoginType.PASSWORD, "用户状态异常");
                throw new BusinessException(RCode.BUSINESS_ERROR);
            }
        }
    }

    /**
     * 从邮箱生成用户名
     */
    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.substring(0, email.indexOf('@'));
        String username = baseUsername;
        int suffix = 1;

        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + suffix;
            suffix++;
        }

        return username;
    }
}
