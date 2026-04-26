package com.sbeam.gameserver.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecretKey secretKey;
    private final long accessExpirationSeconds;
    private final long refreshExpirationSeconds;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.access-expiration-seconds}") long accessExpirationSeconds,
                      @Value("${jwt.refresh-expiration-seconds}") long refreshExpirationSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationSeconds = accessExpirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    //生成token，共用一个私有方法
    public String generateAccessToken(String email) {
        return generateToken(email, ACCESS_TOKEN_TYPE, accessExpirationSeconds);
    }
    public String generateRefreshToken(String email) {
        return generateToken(email, REFRESH_TOKEN_TYPE, refreshExpirationSeconds);
    }

    //解析token
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }
    public Instant extractExpiration(String token) {
        return extractAllClaims(token).getExpiration().toInstant();
    }

    //获取有效期时长
    public Duration getAccessTokenTtl() {
        return Duration.ofSeconds(accessExpirationSeconds);
    }
    public Duration getRefreshTokenTtl() {
        return Duration.ofSeconds(refreshExpirationSeconds);
    }

    //校验token
    public boolean isAccessTokenValid(String token, String email) {
        return isTokenValidByType(token, email, ACCESS_TOKEN_TYPE);
    }
    public boolean isRefreshTokenValid(String token, String email) {
        return isTokenValidByType(token, email, REFRESH_TOKEN_TYPE);
    }

    //通过expirationSeconds控制是at还是rt
    private String generateToken(String email, String tokenType, long expirationSeconds) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(email)                     //谁的token
                .claim(TOKEN_TYPE_CLAIM, tokenType) //类型
                .issuedAt(Date.from(now))           //签发时间
                .expiration(Date.from(expireAt))    //剩余过期时间
                .signWith(secretKey)        //签名
                .compact();
    }

    //校验方法
    private boolean isTokenValidByType(String token, String email, String expectedType) {
        Claims claims = extractAllClaims(token);
        String subject = claims.getSubject();
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        Instant expiration = claims.getExpiration().toInstant();

        return subject.equals(email)
                && expectedType.equals(tokenType)       //检查是kt还是rt
                && expiration.isAfter(Instant.now());   //检查过期情况
    }

    //解析方法
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}