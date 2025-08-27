package com.movk.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("security.password")
public class SecurityPasswordProperties {
    private String encoder = "bcrypt";
    private Integer strength = 10;
    private String pepper;

    public String getEncoder() {
        return encoder;
    }

    public void setEncoder(String encoder) {
        this.encoder = encoder;
    }

    public Integer getStrength() {
        return strength;
    }

    public void setStrength(Integer strength) {
        this.strength = strength;
    }

    public String getPepper() {
        return pepper;
    }

    public void setPepper(String pepper) {
        this.pepper = pepper;
    }
}


