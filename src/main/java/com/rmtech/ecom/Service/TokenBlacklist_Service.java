package com.rmtech.ecom.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j

public class TokenBlacklist_Service
{
    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklist_Service(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }
    public void addTokenToBlacklist(String token, LocalDateTime expiryDate)
    {
        try {
            Duration ttl = Duration.between(LocalDateTime.now(), expiryDate);
            // Token already expired (clock skew / replayed logout): nothing to blacklist.
            if (ttl.isNegative() || ttl.isZero()) {
                return;
            }
            redisTemplate.opsForValue().set(token, "Blacklisted", ttl);
        } catch (RuntimeException ex) {
            // Redis down must not break logout; refresh token is still deleted.
            log.warn("Failed to blacklist token (Redis unavailable)", ex);
        }
    }

    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(token));
        } catch (RuntimeException ex) {
            // Fail-open: an unavailable blacklist must not lock every user out.
            log.warn("Blacklist check failed (Redis unavailable), failing open", ex);
            return false;
        }
    }
}
