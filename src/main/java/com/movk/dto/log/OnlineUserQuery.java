/*
 * @Author yixuanmiao
 * @Date 2025/12/14
 */

package com.movk.dto.log;

import lombok.Data;

/**
 * 在线用户查询参数
 */
@Data
public class OnlineUserQuery {

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录IP
     */
    private String loginIp;
}
