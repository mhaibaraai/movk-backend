package com.movk.application.security;

import com.movk.config.security.SecurityPasswordProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final SecurityPasswordProperties properties;

    public PasswordService(PasswordEncoder passwordEncoder, SecurityPasswordProperties properties) {
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    public String hash(String rawPassword) {
        String material = applyPepper(rawPassword);
        return passwordEncoder.encode(material);
    }

    public boolean matches(String rawPassword, String hashed) {
        String material = applyPepper(rawPassword);
        return passwordEncoder.matches(material, hashed);
    }

    private String applyPepper(String raw) {
        String pepper = properties.getPepper();
        if (pepper == null || pepper.isEmpty()) {
            return raw;
        }
        return raw + "{" + pepper + "}";
    }
}


