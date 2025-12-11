/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.notice;

import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.NoticeType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NoticeResp(
    UUID id,
    String noticeTitle,
    NoticeType noticeType,
    String noticeContent,
    EnableStatus status,
    UUID creator,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
