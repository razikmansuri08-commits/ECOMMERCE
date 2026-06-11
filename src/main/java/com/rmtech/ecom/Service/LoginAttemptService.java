package com.rmtech.ecom.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final int MAX_LOGIN_ATTEMPTS =5;
    private static final Duration LOCKED_DURATION = Duration.ofMinutes(15);

    private String getKey(String username) {
        return "login_attempts_" + username;
    }

    public String loginFailed(String username) {
        String key= getKey(username);

        Long attempts = redisTemplate.opsForValue().increment(key, 1);
        redisTemplate.expire(key, LOCKED_DURATION);
        Long remaining_attempts=5-attempts;
        return "Number of RemainingAttempts: " + remaining_attempts;
    }

    public void loginSucces(String username) {
        String key= getKey(username);
        redisTemplate.delete(key);
    }

    public boolean isLocked(String username) {
        String key= getKey(username);
        Object attempts = redisTemplate.hasKey(key);
        if(attempts == null) {
            return false;
        }
        return Integer.parseInt(attempts.toString()) >= MAX_LOGIN_ATTEMPTS;
    }

    public Long getremainingLockTime(String username) {
        Long remainingLockTime = redisTemplate.getExpire(getKey(username));
        return remainingLockTime==null?0:remainingLockTime;
    }

}
