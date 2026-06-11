package com.rmtech.ecom.Service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service

public class TokenBlacklist_Service
{
    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklist_Service(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }
    public void addTokenToBlacklist(String token, LocalDateTime expiryDate)
    {
        Duration ttl= Duration.between(LocalDateTime.now(),expiryDate);
        redisTemplate.opsForValue().set(token,"Blacklisted",ttl);
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}
