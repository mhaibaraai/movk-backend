package com.movk.config.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(SecurityPasswordProperties.class)
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder(SecurityPasswordProperties props) {
        // 仅支持 bcrypt，后续可根据 props.getEncoder() 扩展
        return new BCryptPasswordEncoder(props.getStrength());
    }
}


