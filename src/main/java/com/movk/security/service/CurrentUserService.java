/*
 * @Author yixuanmiao
 * @Date 2025/08/31 01:57
 */

package com.movk.security.service;

import com.movk.base.exception.BusinessException;
import com.movk.base.result.RCode;
import com.movk.security.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrentUserService {

    public LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        
        throw new BusinessException(RCode.UNAUTHORIZED);
    }

    public List<String> getCurrentUserRoles() {
        return getCurrentUser().getRoles();
    }
}
