package com.movk.security.service;

import com.movk.security.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

/**
 * JWT 服务
 * 仅负责 AccessToken (JWT) 的生成和验证
 * RefreshToken 由 TokenService 通过数据库管理
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration:900}")
    private int accessTokenExpiration;

    @PostConstruct
    public void validateJwtSecret() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("jwt.secret 必须为 Base64 编码字符串", e);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret 解码后长度不足 256 bit (32 bytes)");
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 AccessToken
     */
    public String generateAccessToken(LoginUser loginUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", loginUser.getRoles());
        claims.put("userId", loginUser.getId().toString());
        claims.put("nickname", loginUser.getNickname());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration * 1000L);

        return Jwts.builder()
                .claims(claims)
                .subject(loginUser.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验证 AccessToken
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取 AccessToken 有效期（秒）
     */
    public int getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (List<String>) claims.get("roles");
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        String userIdStr = (String) claims.get("userId");
        return UUID.fromString(userIdStr);
    }

    public String getNicknameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("nickname");
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
