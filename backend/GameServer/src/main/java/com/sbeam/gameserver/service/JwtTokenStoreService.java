package com.sbeam.gameserver.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

//JWT在Redis里的存取和删除
@Service
public class JwtTokenStoreService {

    private static final String ACCESS_TOKEN_KEY_PREFIX = "auth:jwt:access:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:jwt:refresh:";

    private final StringRedisTemplate stringRedisTemplate;

    public JwtTokenStoreService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void saveAccessToken(String email, String token, Duration ttl) {
        stringRedisTemplate.opsForValue().set(buildAccessTokenKey(email), token, ttl);
    }

    public String getAccessToken(String email) {
        return stringRedisTemplate.opsForValue().get(buildAccessTokenKey(email));
    }

    public void saveRefreshToken(String email, String token, Duration ttl) {
        stringRedisTemplate.opsForValue().set(buildRefreshTokenKey(email), token, ttl);
    }

    public String getRefreshToken(String email) {
        return stringRedisTemplate.opsForValue().get(buildRefreshTokenKey(email));
    }

    public void deleteAllTokens(String email) {
        stringRedisTemplate.delete(buildAccessTokenKey(email));
        stringRedisTemplate.delete(buildRefreshTokenKey(email));
    }

    private String buildAccessTokenKey(String email) {
        return ACCESS_TOKEN_KEY_PREFIX + email;
    }

    private String buildRefreshTokenKey(String email) {
        return REFRESH_TOKEN_KEY_PREFIX + email;
    }
}