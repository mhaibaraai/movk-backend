/*
 * @Author yixuanmiao
 * @Date 2025/12/14
 */

package com.movk.entity;

import com.movk.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 系统文件实体
 * 存储文件元数据信息
 */
@Entity
@Table(name = "sys_file")
@Getter
@Setter
public class SysFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /**
     * 原始文件名
     */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    /**
     * 存储文件名（UUID + 扩展名）
     */
    @Column(name = "storage_name", nullable = false, length = 100)
    private String storageName;

    /**
     * 文件扩展名
     */
    @Column(name = "extension", length = 50)
    private String extension;

    /**
     * 文件 MIME 类型
     */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /**
     * 文件大小（字节）
     */
    @Column(name = "size", nullable = false)
    private Long size;

    /**
     * 文件存储路径（相对路径）
     */
    @Column(name = "path", nullable = false, length = 500)
    private String path;

    /**
     * 文件 MD5 哈希值
     */
    @Column(name = "md5", length = 32)
    private String md5;

    /**
     * 存储类型：local-本地存储，oss-对象存储
     */
    @Column(name = "storage_type", nullable = false, length = 20)
    private String storageType = "local";

    /**
     * 文件分类/业务模块
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
}
