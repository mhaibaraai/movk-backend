package com.movk.repository;

import com.movk.entity.RefreshToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RefreshToken Repository
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * 根据 Token 值查询
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 根据 Token 值查询有效的 Token（未撤销且未过期）
     */
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.token = :token " +
           "AND rt.revoked = false " +
           "AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidToken(@Param("token") String token, @Param("now") OffsetDateTime now);

    /**
     * 查询用户的所有有效会话
     */
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.userId = :userId " +
           "AND rt.revoked = false " +
           "AND rt.expiresAt > :now " +
           "ORDER BY rt.issuedAt DESC")
    List<RefreshToken> findActiveByUserId(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);

    /**
     * 统计用户的有效会话数
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt " +
           "WHERE rt.userId = :userId " +
           "AND rt.revoked = false " +
           "AND rt.expiresAt > :now")
    int countActiveByUserId(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);

    /**
     * 撤销用户的所有 Token（踢出用户）
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET " +
           "rt.revoked = true, " +
           "rt.revokedAt = :now, " +
           "rt.revokedReason = :reason " +
           "WHERE rt.userId = :userId " +
           "AND rt.revoked = false")
    int revokeAllByUserId(@Param("userId") UUID userId,
                          @Param("now") OffsetDateTime now,
                          @Param("reason") String reason);

    /**
     * 撤销用户其他 Token（保留指定 Token，用于单点登录）
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET " +
           "rt.revoked = true, " +
           "rt.revokedAt = :now, " +
           "rt.revokedReason = :reason " +
           "WHERE rt.userId = :userId " +
           "AND rt.token != :currentToken " +
           "AND rt.revoked = false")
    int revokeOthersByUserId(@Param("userId") UUID userId,
                             @Param("currentToken") String currentToken,
                             @Param("now") OffsetDateTime now,
                             @Param("reason") String reason);

    /**
     * 清理过期的 Token（定时任务）
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :before")
    int deleteExpiredBefore(@Param("before") OffsetDateTime before);

    /**
     * 分页查询所有有效会话（管理后台用）
     */
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.revoked = false " +
           "AND rt.expiresAt > :now " +
           "ORDER BY rt.issuedAt DESC")
    Page<RefreshToken> findAllActive(@Param("now") OffsetDateTime now, Pageable pageable);

    /**
     * 统计所有有效会话数
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt " +
           "WHERE rt.revoked = false " +
           "AND rt.expiresAt > :now")
    long countAllActive(@Param("now") OffsetDateTime now);
}
