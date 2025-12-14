/*
 * @Author yixuanmiao
 * @Date 2025/12/14
 */

package com.movk.service;

import com.movk.dto.file.FileQueryReq;
import com.movk.dto.file.FileResp;
import com.movk.dto.file.FileUploadResp;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传单个文件
     *
     * @param file     文件
     * @param category 文件分类
     * @return 文件上传响应
     */
    FileUploadResp upload(MultipartFile file, String category);

    /**
     * 批量上传文件
     *
     * @param files    文件列表
     * @param category 文件分类
     * @return 文件上传响应列表
     */
    List<FileUploadResp> uploadBatch(MultipartFile[] files, String category);

    /**
     * 下载文件
     *
     * @param id 文件 ID
     * @return 文件资源
     */
    Resource download(UUID id);

    /**
     * 根据存储名称下载文件
     *
     * @param storageName 存储名称
     * @return 文件资源
     */
    Resource downloadByStorageName(String storageName);

    /**
     * 获取文件详情
     *
     * @param id 文件 ID
     * @return 文件详情
     */
    FileResp getFileInfo(UUID id);

    /**
     * 分页查询文件列表
     *
     * @param query    查询条件
     * @param pageable 分页参数
     * @return 文件分页列表
     */
    Page<FileResp> getFilePage(FileQueryReq query, Pageable pageable);

    /**
     * 删除文件
     *
     * @param id 文件 ID
     */
    void delete(UUID id);

    /**
     * 批量删除文件
     *
     * @param ids 文件 ID 列表
     */
    void deleteBatch(List<UUID> ids);

    /**
     * 获取文件的原始文件名
     *
     * @param id 文件 ID
     * @return 原始文件名
     */
    String getOriginalName(UUID id);

    /**
     * 获取文件的 Content-Type
     *
     * @param id 文件 ID
     * @return Content-Type
     */
    String getContentType(UUID id);
}
