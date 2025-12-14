/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.controller;

import com.movk.base.result.R;
import com.movk.dto.post.*;
import com.movk.security.annotation.Log;
import com.movk.security.annotation.RequiresPermission;
import com.movk.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.movk.common.enums.OperationType.*;

/**
 * 岗位管理 Controller
 */
@Tag(name = "岗位管理", description = "岗位相关接口")
@RestController
@RequestMapping("/api/system/posts")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostService postService;

    /**
     * 获取岗位列表
     */
    @GetMapping
    @RequiresPermission("system:post:list")
    public R<List<PostResp>> getPostList() {
        return R.success(postService.getAllPosts());
    }

    /**
     * 获取岗位详情
     */
    @GetMapping("/{id}")
    @RequiresPermission("system:post:query")
    public R<PostResp> getPostById(@PathVariable UUID id) {
        return R.success(postService.getPostById(id));
    }

    /**
     * 新增岗位
     */
    @PostMapping
    @RequiresPermission("system:post:add")
    @Log(module = "岗位管理", operation = CREATE)
    public R<UUID> createPost(@Valid @RequestBody PostCreateReq req) {
        return R.success(postService.createPost(req));
    }

    /**
     * 修改岗位
     */
    @PutMapping
    @RequiresPermission("system:post:edit")
    @Log(module = "岗位管理", operation = UPDATE)
    public R<Void> updatePost(@Valid @RequestBody PostUpdateReq req) {
        postService.updatePost(req);
        return R.ok();
    }

    /**
     * 删除岗位
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("system:post:delete")
    @Log(module = "岗位管理", operation = DELETE)
    public R<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return R.ok();
    }

    /**
     * 检查岗位编码是否存在
     */
    @GetMapping("/exists")
    @RequiresPermission("system:post:query")
    public R<Boolean> checkPostCode(@RequestParam String code) {
        return R.success(postService.existsByCode(code));
    }
}
