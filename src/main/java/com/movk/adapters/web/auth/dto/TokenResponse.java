/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.adapters.web.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
}
