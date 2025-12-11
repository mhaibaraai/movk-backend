/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service.impl;

import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.dto.post.*;
import com.movk.entity.Post;
import com.movk.repository.PostRepository;
import com.movk.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 岗位服务实现
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    @Transactional
    public UUID createPost(PostCreateReq req) {
        if (postRepository.findByPostCodeAndDeletedFalse(req.postCode()).isPresent()) {
            throw new BusinessException(RCode.BAD_REQUEST, "岗位编码已存在");
        }

        Post post = Post.builder()
            .postCode(req.postCode())
            .postName(req.postName())
            .orderNum(req.orderNum())
            .status(req.status())
            .remark(req.remark())
            .build();

        post = postRepository.save(post);
        return post.getId();
    }

    @Override
    @Transactional
    public void updatePost(PostUpdateReq req) {
        Post post = postRepository.findById(req.id())
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "岗位不存在"));

        post.setPostName(req.postName());
        post.setOrderNum(req.orderNum());
        post.setStatus(req.status());
        post.setRemark(req.remark());

        postRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(UUID postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "岗位不存在"));

        post.setDeleted(true);
        post.setDeletedAt(OffsetDateTime.now());
        postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResp getPostById(UUID postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "岗位不存在"));

        return toPostResp(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResp> getAllPosts() {
        List<Post> posts = postRepository.findByDeletedFalseOrderByOrderNumAsc();
        return posts.stream()
            .map(this::toPostResp)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String postCode) {
        return postRepository.findByPostCodeAndDeletedFalse(postCode).isPresent();
    }

    private PostResp toPostResp(Post post) {
        return new PostResp(
            post.getId(),
            post.getPostCode(),
            post.getPostName(),
            post.getOrderNum(),
            post.getStatus(),
            post.getRemark(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }
}
