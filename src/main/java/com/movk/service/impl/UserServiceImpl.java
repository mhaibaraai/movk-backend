/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.dto.user.*;
import com.movk.entity.*;
import com.movk.entity.id.UserRoleId;
import com.movk.repository.*;
import com.movk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PostRepository postRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserPostRepository userPostRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UUID createUser(UserCreateReq req) {
        if (userRepository.findByUsername(req.username()).isPresent()) {
            throw new BusinessException(RCode.BAD_REQUEST, "用户名已存在");
        }

        if (req.email() != null && userRepository.findByEmail(req.email()).isPresent()) {
            throw new BusinessException(RCode.BAD_REQUEST, "邮箱已被使用");
        }

        User user = User.builder()
            .username(req.username())
            .password(passwordEncoder.encode(req.password()))
            .nickname(req.nickname())
            .email(req.email())
            .phone(req.phone())
            .gender(req.gender())
            .avatar(req.avatar())
            .status(req.status())
            .deptId(req.deptId())
            .remark(req.remark())
            .build();

        user = userRepository.save(user);

        if (req.roleIds() != null && !req.roleIds().isEmpty()) {
            assignRoles(user.getId(), req.roleIds());
        }

        if (req.postIds() != null && !req.postIds().isEmpty()) {
            assignPosts(user.getId(), req.postIds());
        }

        return user.getId();
    }

    @Override
    @Transactional
    public void updateUser(UserUpdateReq req) {
        User user = userRepository.findById(req.id())
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "用户不存在"));

        if (req.email() != null && !req.email().equals(user.getEmail())) {
            if (userRepository.findByEmail(req.email()).isPresent()) {
                throw new BusinessException(RCode.BAD_REQUEST, "邮箱已被使用");
            }
        }

        user.setNickname(req.nickname());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        user.setGender(req.gender());
        user.setAvatar(req.avatar());
        user.setStatus(req.status());
        user.setDeptId(req.deptId());
        user.setRemark(req.remark());

        userRepository.save(user);

        if (req.roleIds() != null) {
            assignRoles(user.getId(), req.roleIds());
        }

        if (req.postIds() != null) {
            assignPosts(user.getId(), req.postIds());
        }
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "用户不存在"));

        user.setDeleted(true);
        user.setDeletedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUsers(Iterable<UUID> userIds) {
        List<UUID> idList = new ArrayList<>();
        userIds.forEach(idList::add);

        if (idList.isEmpty()) {
            return;
        }

        List<User> users = userRepository.findAllById(idList);
        OffsetDateTime now = OffsetDateTime.now();

        users.forEach(user -> {
            user.setDeleted(true);
            user.setDeletedAt(now);
        });

        userRepository.saveAll(users);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResp getUserById(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "用户不存在"));

        String deptName = null;
        if (user.getDeptId() != null) {
            deptName = departmentRepository.findById(user.getDeptId())
                .map(Department::getDeptName)
                .orElse(null);
        }

        return new UserResp(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getEmail(),
            user.getPhone(),
            user.getGender(),
            user.getAvatar(),
            user.getStatus(),
            user.getDeptId(),
            deptName,
            user.getLoginIp(),
            user.getLoginDate(),
            user.getRemark(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResp getUserDetail(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "用户不存在"));

        String deptName = null;
        if (user.getDeptId() != null) {
            deptName = departmentRepository.findById(user.getDeptId())
                .map(Department::getDeptName)
                .orElse(null);
        }

        List<Role> roles = roleRepository.findRolesByUserId(userId);
        List<UUID> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        List<String> roleCodes = roles.stream().map(Role::getCode).collect(Collectors.toList());
        List<String> roleNames = roles.stream().map(Role::getName).collect(Collectors.toList());

        List<UUID> postIds = userPostRepository.findPostIdsByUserId(userId);
        List<Post> posts = postIds.isEmpty() ? List.of() : postRepository.findAllById(postIds);
        List<String> postCodes = posts.stream().map(Post::getPostCode).collect(Collectors.toList());
        List<String> postNames = posts.stream().map(Post::getPostName).collect(Collectors.toList());

        return new UserDetailResp(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getEmail(),
            user.getPhone(),
            user.getGender(),
            user.getAvatar(),
            user.getStatus(),
            user.getDeptId(),
            deptName,
            roleIds,
            roleCodes,
            roleNames,
            postIds,
            postCodes,
            postNames,
            user.getLoginIp(),
            user.getLoginDate(),
            user.getRemark(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResp> getUserPage(UserQueryReq queryReq, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            if (queryReq.username() != null && !queryReq.username().isBlank()) {
                predicates.add(cb.like(root.get("username"), "%" + queryReq.username() + "%"));
            }

            if (queryReq.nickname() != null && !queryReq.nickname().isBlank()) {
                predicates.add(cb.like(root.get("nickname"), "%" + queryReq.nickname() + "%"));
            }

            if (queryReq.phone() != null && !queryReq.phone().isBlank()) {
                predicates.add(cb.like(root.get("phone"), "%" + queryReq.phone() + "%"));
            }

            if (queryReq.email() != null && !queryReq.email().isBlank()) {
                predicates.add(cb.like(root.get("email"), "%" + queryReq.email() + "%"));
            }

            if (queryReq.status() != null) {
                predicates.add(cb.equal(root.get("status"), queryReq.status()));
            }

            if (queryReq.deptId() != null) {
                predicates.add(cb.equal(root.get("deptId"), queryReq.deptId()));
            }

            if (queryReq.createdAtStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), queryReq.createdAtStart()));
            }

            if (queryReq.createdAtEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), queryReq.createdAtEnd()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);

        Map<UUID, String> deptNameMap = new HashMap<>();
        Set<UUID> deptIds = userPage.getContent().stream()
            .map(User::getDeptId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (!deptIds.isEmpty()) {
            List<Department> depts = departmentRepository.findAllById(deptIds);
            deptNameMap = depts.stream()
                .collect(Collectors.toMap(Department::getId, Department::getDeptName));
        }

        Map<UUID, String> finalDeptNameMap = deptNameMap;
        List<UserResp> userResps = userPage.getContent().stream()
            .map(user -> new UserResp(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getGender(),
                user.getAvatar(),
                user.getStatus(),
                user.getDeptId(),
                user.getDeptId() != null ? finalDeptNameMap.get(user.getDeptId()) : null,
                user.getLoginIp(),
                user.getLoginDate(),
                user.getRemark(),
                user.getCreatedAt(),
                user.getUpdatedAt()
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(userResps, pageable, userPage.getTotalElements());
    }

    @Override
    @Transactional
    public void assignRoles(AssignRoleReq req) {
        assignRoles(req.userId(), req.roleIds());
    }

    private void assignRoles(UUID userId, Iterable<UUID> roleIds) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(RCode.NOT_FOUND, "用户不存在");
        }

        // 批量删除旧的用户角色关联
        userRoleRepository.deleteByUserId(userId);

        User user = userRepository.findById(userId).orElseThrow();

        // 批量查询角色
        List<UUID> roleIdList = new ArrayList<>();
        roleIds.forEach(roleIdList::add);

        if (roleIdList.isEmpty()) {
            return;
        }

        List<Role> roles = roleRepository.findAllById(roleIdList);
        if (roles.size() != roleIdList.size()) {
            throw new BusinessException(RCode.NOT_FOUND, "部分角色不存在");
        }

        // 批量保存用户角色关联
        List<UserRole> userRoles = roles.stream()
            .map(role -> UserRole.builder()
                .id(new UserRoleId(userId, role.getId()))
                .user(user)
                .role(role)
                .build())
            .toList();

        userRoleRepository.saveAll(userRoles);
    }

    @Override
    @Transactional
    public void assignPosts(UUID userId, Iterable<UUID> postIds) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(RCode.NOT_FOUND, "用户不存在");
        }

        // 批量删除旧的用户岗位关联
        userPostRepository.deleteByUserId(userId);

        List<UUID> postIdList = new ArrayList<>();
        postIds.forEach(postIdList::add);

        if (postIdList.isEmpty()) {
            return;
        }

        // 批量验证岗位是否存在
        long existCount = postRepository.countByIdIn(postIdList);
        if (existCount != postIdList.size()) {
            throw new BusinessException(RCode.NOT_FOUND, "部分岗位不存在");
        }

        // 批量保存用户岗位关联
        List<UserPost> userPosts = postIdList.stream()
            .map(postId -> UserPost.builder()
                .userId(userId)
                .postId(postId)
                .build())
            .toList();

        userPostRepository.saveAll(userPosts);
    }

    @Override
    @Transactional
    public void updatePassword(UpdatePasswordReq req) {
        User user = userRepository.findById(req.userId())
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "用户不存在"));

        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new BusinessException(RCode.BAD_REQUEST, "原密码错误");
        }

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordReq req) {
        User user = userRepository.findById(req.userId())
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "用户不存在"));

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
