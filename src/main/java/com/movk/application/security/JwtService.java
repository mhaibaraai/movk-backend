/*
 * @Author yixuanmiao
 * @Date 2025/08/29
 */

package com.movk.application.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.issuer}")
    private String issuer;
    @Value("${jwt.access-ttl}")
    private Duration accessTtl;
    @Value("${jwt.header}")
    private String header;
    @Value("${jwt.prefix}")
    private String prefix;

    private SecretKey getKey() {
        // 兼容纯文本或Base64密钥：长度不足将自动加盐派生
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes();
        }
        return Keys.hmacShaKeyFor(Keys.hmacShaKeyFor(keyBytes).getEncoded());
    }

    public String generateAccessToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTtl);
        String jti = UUID.randomUUID().toString();

        Map<String, Object> claims = Map.of(
                "email", email,
                "typ", "access"
        );

        return Jwts.builder()
                .setId(jti)
                .setSubject(userId.toString())
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(claims)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // refresh token 已移除

    public Jws<Claims> parseAndValidate(String token) {
        return Jwts.parserBuilder()
                .requireIssuer(issuer)
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);
    }

    // isRefreshToken 已移除

    public String getBearerToken(HttpServletRequest request) {
        String value = request.getHeader(header);
        if (value == null || value.isEmpty()) {
            return null;
        }
        String pf = prefix == null ? "Bearer " : prefix;
        if (value.startsWith(pf)) {
            return value.substring(pf.length()).trim();
        }
        return null;
    }
}
