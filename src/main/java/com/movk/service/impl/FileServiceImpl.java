/*
 * @Author yixuanmiao
 * @Date 2025/12/14
 */

package com.movk.service.impl;

import com.movk.base.config.FileProperties;
import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.dto.file.FileQueryReq;
import com.movk.dto.file.FileResp;
import com.movk.dto.file.FileUploadResp;
import com.movk.entity.SysFile;
import com.movk.repository.SysFileRepository;
import com.movk.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileProperties fileProperties;
    private final SysFileRepository sysFileRepository;

    @Override
    @Transactional
    public FileUploadResp upload(MultipartFile file, String category) {
        validateFile(file);

        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getExtension(originalName);
        String storageName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        String relativePath = generateRelativePath(category, storageName);
        Path targetPath = getAbsolutePath(relativePath);

        try {
            // 确保目录存在
            Files.createDirectories(targetPath.getParent());

            // 计算 MD5
            String md5 = calculateMd5(file);

            // 检查是否存在相同 MD5 的文件（秒传）
            var existingFile = sysFileRepository.findByMd5AndDeletedFalse(md5);
            if (existingFile.isPresent()) {
                SysFile existing = existingFile.get();
                return buildUploadResp(existing);
            }

            // 保存文件到磁盘
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 保存文件元数据到数据库
            SysFile sysFile = new SysFile();
            sysFile.setOriginalName(originalName);
            sysFile.setStorageName(storageName);
            sysFile.setExtension(extension);
            sysFile.setContentType(file.getContentType());
            sysFile.setSize(file.getSize());
            sysFile.setPath(relativePath);
            sysFile.setMd5(md5);
            sysFile.setStorageType(fileProperties.getStorageType());
            sysFile.setCategory(category);

            sysFile = sysFileRepository.save(sysFile);

            log.info("文件上传成功: {} -> {}", originalName, relativePath);
            return buildUploadResp(sysFile);

        } catch (IOException e) {
            log.error("文件上传失败: {}", originalName, e);
            throw new BusinessException(RCode.INTERNAL_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<FileUploadResp> uploadBatch(MultipartFile[] files, String category) {
        return Arrays.stream(files)
                .map(file -> upload(file, category))
                .collect(Collectors.toList());
    }

    @Override
    public Resource download(UUID id) {
        SysFile sysFile = sysFileRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "文件不存在"));
        return loadFileAsResource(sysFile.getPath());
    }

    @Override
    public Resource downloadByStorageName(String storageName) {
        SysFile sysFile = sysFileRepository.findByStorageNameAndDeletedFalse(storageName)
                .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "文件不存在"));
        return loadFileAsResource(sysFile.getPath());
    }

    @Override
    public FileResp getFileInfo(UUID id) {
        SysFile sysFile = sysFileRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "文件不存在"));
        return buildFileResp(sysFile);
    }

    @Override
    public Page<FileResp> getFilePage(FileQueryReq query, Pageable pageable) {
        Page<SysFile> page = sysFileRepository.findByConditions(
                query.getOriginalName(),
                query.getCategory(),
                query.getContentType(),
                pageable
        );
        return page.map(this::buildFileResp);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        SysFile sysFile = sysFileRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "文件不存在"));

        // 逻辑删除
        sysFile.setDeleted(true);
        sysFile.setDeletedAt(OffsetDateTime.now());
        sysFileRepository.save(sysFile);

        // 物理删除文件（可选，根据业务需求决定是否立即删除）
        // deletePhysicalFile(sysFile.getPath());

        log.info("文件删除成功: {}", sysFile.getOriginalName());
    }

    @Override
    @Transactional
    public void deleteBatch(List<UUID> ids) {
        List<SysFile> files = sysFileRepository.findByIdInAndDeletedFalse(ids);
        OffsetDateTime now = OffsetDateTime.now();
        files.forEach(file -> {
            file.setDeleted(true);
            file.setDeletedAt(now);
        });
        sysFileRepository.saveAll(files);
        log.info("批量删除文件成功，数量: {}", files.size());
    }

    @Override
    public String getOriginalName(UUID id) {
        return sysFileRepository.findByIdAndDeletedFalse(id)
                .map(SysFile::getOriginalName)
                .orElseThrow(() -> new BusinessException(RCode.NOT_FOUND, "文件不存在"));
    }

    @Override
    public String getContentType(UUID id) {
        return sysFileRepository.findByIdAndDeletedFalse(id)
                .map(SysFile::getContentType)
                .orElse("application/octet-stream");
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(RCode.BAD_REQUEST, "上传文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > fileProperties.getMaxSize()) {
            throw new BusinessException(RCode.BAD_REQUEST,
                    "文件大小超出限制，最大允许: " + formatFileSize(fileProperties.getMaxSize()));
        }

        // 验证文件扩展名
        String originalName = file.getOriginalFilename();
        if (originalName != null && !originalName.isEmpty()) {
            String extension = getExtension(originalName).toLowerCase();
            if (!extension.isEmpty() && !fileProperties.getAllowedExtensions().contains(extension)) {
                throw new BusinessException(RCode.BAD_REQUEST,
                        "不支持的文件类型: " + extension);
            }
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    /**
     * 生成相对路径
     */
    private String generateRelativePath(String category, String storageName) {
        StringBuilder path = new StringBuilder();

        if (category != null && !category.isEmpty()) {
            path.append(category).append("/");
        }

        if (fileProperties.getDatePathEnabled()) {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            path.append(datePath).append("/");
        }

        path.append(storageName);
        return path.toString();
    }

    /**
     * 获取绝对路径
     */
    private Path getAbsolutePath(String relativePath) {
        return Paths.get(fileProperties.getBasePath()).resolve(relativePath).normalize();
    }

    /**
     * 计算文件 MD5
     */
    private String calculateMd5(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtils.md5DigestAsHex(inputStream);
        } catch (IOException e) {
            log.warn("计算文件 MD5 失败", e);
            return null;
        }
    }

    /**
     * 加载文件资源
     */
    private Resource loadFileAsResource(String relativePath) {
        try {
            Path filePath = getAbsolutePath(relativePath);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException(RCode.NOT_FOUND, "文件不存在或不可读");
            }
        } catch (MalformedURLException e) {
            throw new BusinessException(RCode.INTERNAL_ERROR, "文件路径错误");
        }
    }

    /**
     * 构建上传响应
     */
    private FileUploadResp buildUploadResp(SysFile sysFile) {
        return FileUploadResp.builder()
                .id(sysFile.getId())
                .originalName(sysFile.getOriginalName())
                .storageName(sysFile.getStorageName())
                .size(sysFile.getSize())
                .contentType(sysFile.getContentType())
                .downloadUrl("/api/system/files/download/" + sysFile.getId())
                .md5(sysFile.getMd5())
                .build();
    }

    /**
     * 构建文件响应
     */
    private FileResp buildFileResp(SysFile sysFile) {
        return FileResp.builder()
                .id(sysFile.getId())
                .originalName(sysFile.getOriginalName())
                .storageName(sysFile.getStorageName())
                .extension(sysFile.getExtension())
                .contentType(sysFile.getContentType())
                .size(sysFile.getSize())
                .sizeFormatted(formatFileSize(sysFile.getSize()))
                .path(sysFile.getPath())
                .md5(sysFile.getMd5())
                .storageType(sysFile.getStorageType())
                .category(sysFile.getCategory())
                .downloadUrl("/api/system/files/download/" + sysFile.getId())
                .createdAt(sysFile.getCreatedAt())
                .remark(sysFile.getRemark())
                .build();
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
