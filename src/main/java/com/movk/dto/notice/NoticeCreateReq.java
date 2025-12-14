/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.notice;

import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.NoticeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NoticeCreateReq(
    @NotBlank(message = "公告标题不能为空")
    @Size(min = 1, max = 100, message = "公告标题长度必须在 1-100 之间")
    String noticeTitle,

    @NotNull(message = "公告类型不能为空")
    NoticeType noticeType,

    @Size(max = 10000, message = "公告内容长度不能超过 10000")
    String noticeContent,

    EnableStatus status
) {}
