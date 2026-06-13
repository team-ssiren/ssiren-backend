package com.ssaika.ssiren.domain.report.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportReactionDailyLimitRepository {

    private static final String KEY_PREFIX = "report-reaction:daily:report:v2:";
    private static final Duration TTL = Duration.ofDays(1);

    private final RedisTemplate<String, String> redisTemplate;

    public boolean markIfAbsent(Long reportId, Long userId) {
        Boolean saved = redisTemplate.opsForValue()
            .setIfAbsent(buildKey(reportId, userId), "1", TTL);

        return Boolean.TRUE.equals(saved);
    }

    private String buildKey(Long reportId, Long userId) {
        return KEY_PREFIX + reportId + ":" + userId;
    }
}
