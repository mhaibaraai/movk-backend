/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.service;

import com.movk.dto.post.*;

import java.util.List;
import java.util.UUID;

/**
 * 岗位服务接口
 */
public interface PostService {

    /**
     * 创建岗位
     */
    UUID createPost(PostCreateReq req);

    /**
     * 更新岗位
     */
    void updatePost(PostUpdateReq req);

    /**
     * 删除岗位（逻辑删除）
     */
    void deletePost(UUID postId);

    /**
     * 根据ID查询岗位
     */
    PostResp getPostById(UUID postId);

    /**
     * 查询所有岗位列表
     */
    List<PostResp> getAllPosts();

    /**
     * 检查岗位编码是否存在
     */
    boolean existsByCode(String postCode);
}
