/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.entity;

import com.movk.common.converter.EnableStatusConverter;
import com.movk.common.converter.NoticeTypeConverter;
import com.movk.common.enums.EnableStatus;
import com.movk.common.enums.NoticeType;
import com.movk.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

/**
 * 通知公告实体
 */
@Entity
@Table(name = "sys_notice")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "notice_title", nullable = false, length = 100)
    private String noticeTitle;

    @Convert(converter = NoticeTypeConverter.class)
    @Column(name = "notice_type", nullable = false)
    private NoticeType noticeType;

    @Column(name = "notice_content", columnDefinition = "TEXT")
    private String noticeContent;

    @Convert(converter = EnableStatusConverter.class)
    @Column(nullable = false)
    private EnableStatus status;
}
