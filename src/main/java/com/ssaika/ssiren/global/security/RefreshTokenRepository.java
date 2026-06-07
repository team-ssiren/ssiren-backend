package com.ssaika.ssiren.global.security;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh:";

    private final RedisTemplate<String, String> redisTemplate;

    public void save(Long userId, String refreshToken, long ttlSeconds) {
        redisTemplate.opsForValue().set(buildKey(userId), refreshToken, ttlSeconds, TimeUnit.SECONDS);
    }

    public Optional<String> find(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(buildKey(userId)));
    }

    public void delete(Long userId) {
        redisTemplate.delete(buildKey(userId));
    }

    private String buildKey(Long userId) {
        return KEY_PREFIX + userId;
    }
}
