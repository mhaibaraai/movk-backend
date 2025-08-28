/*
 * @Author yixuanmiao
 * @Date 2025/08/28 10:33
 */

package com.movk.application.security;

import com.movk.config.security.SecurityPasswordProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final SecurityPasswordProperties properties;

    // 构造函数注入
    public PasswordService(PasswordEncoder passwordEncoder, SecurityPasswordProperties properties) {
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

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
        String pepper = properties.getPepper();
        if (pepper == null || pepper.isEmpty()) {
            return raw;
        }
        return raw + "{" + pepper + "}";
    }

    // 使用示例
    public static void main(String[] args) {
        SecurityPasswordProperties props = new SecurityPasswordProperties();
        props.setPepper("mySecretPepper");
        props.setStrength(10);

        PasswordService service = new PasswordService(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(props.getStrength()), props);

        String password = "user123";
        String hashed = service.hash(password);

        System.out.println("原始密码: " + password);
        System.out.println("加密后: " + hashed);
        System.out.println("验证结果: " + service.matches(password, hashed));
    }
}
