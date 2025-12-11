/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.dto.notice;

import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.NoticeType;

public record NoticeCreateReq(
    String noticeTitle,
    NoticeType noticeType,
    String noticeContent,
    EnableStatus status
) {}
