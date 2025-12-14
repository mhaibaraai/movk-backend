/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

import com.movk.dto.user.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 创建用户
     */
    UUID createUser(UserCreateReq req);

    /**
     * 更新用户
     */
    void updateUser(UserUpdateReq req);

    /**
     * 删除用户（逻辑删除）
     */
    void deleteUser(UUID userId);

    /**
     * 批量删除用户
     */
    void deleteUsers(Iterable<UUID> userIds);

    /**
     * 根据ID查询用户
     */
    UserResp getUserById(UUID userId);

    /**
     * 查询用户详情（包含角色和岗位信息）
     */
    UserDetailResp getUserDetail(UUID userId);

    /**
     * 分页查询用户列表
     */
    Page<UserResp> getUserPage(UserQueryReq queryReq, Pageable pageable);

    /**
     * 分配用户角色
     */
    void assignRoles(UUID userId, Iterable<UUID> roleIds);

    /**
     * 分配用户岗位
     */
    void assignPosts(UUID userId, Iterable<UUID> postIds);

    /**
     * 更新用户密码
     */
    void updatePassword(UpdatePasswordReq req);

    /**
     * 重置用户密码
     */
    void resetPassword(ResetPasswordReq req);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}
