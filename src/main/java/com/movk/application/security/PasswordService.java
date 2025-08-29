/*
 * @Author yixuanmiao
 * @Date 2025/08/28 10:33
 */

package com.movk.application.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    // 对密码进行加盐哈希
    public String hash(String rawPassword) {
        String material = applyPepper(rawPassword);
        return passwordEncoder.encode(material);
    }

    // 验证密码
    public boolean matches(String rawPassword, String hashed) {
        String material = applyPepper(rawPassword);
        return passwordEncoder.matches(material, hashed);
    }

    // 应用pepper
    private String applyPepper(String raw) {
        String pepper = "movk-secret-pepper";
        return raw + "{" + pepper + "}";
    }

    // 使用示例
    public static void main(String[] args) {

        PasswordService service = new PasswordService();

        String password = "user123";
        String hashed = service.hash(password);

        System.out.println("原始密码: " + password);
        System.out.println("加密后: " + hashed);
        System.out.println("验证结果: " + service.matches(password, hashed));
    }
}
