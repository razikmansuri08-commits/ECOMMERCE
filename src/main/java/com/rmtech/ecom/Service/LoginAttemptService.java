package com.rmtech.ecom.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final RedisTemplate<String, Object> redisTemplate;


    private static final int MAX_LOGIN_ATTEMPTS =5;
    private static final Duration LOCKED_DURATION = Duration.ofMinutes(15);

    private String getKey(String username) {
        return "login_attempts_" + username;
    }

    public String loginFailed(String username) {
        String key = getKey(username);
        try {
            Long attempts = redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, LOCKED_DURATION);
            long remainingAttempts = Math.max(0, MAX_LOGIN_ATTEMPTS - (attempts == null ? 0 : attempts));
            return "Number of RemainingAttempts: " + remainingAttempts;
        } catch (RuntimeException ex) {
            log.warn("loginFailed counter unavailable for {} (Redis down), failing open", username, ex);
            return "Number of RemainingAttempts: unknown (rate limiting unavailable)";
        }
    }

    public void loginSucces(String username) {
        String key = getKey(username);
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException ex) {
            log.warn("loginSucces cleanup failed for {} (Redis down)", username, ex);
        }
    }

    public boolean isLocked(String username) {
        String key = getKey(username);
        try {
            Object attempts = redisTemplate.opsForValue().get(key);
            log.info("Attempts: " + attempts);
            if (attempts == null) {
                return false;
            }
            return Integer.parseInt(attempts.toString()) >= MAX_LOGIN_ATTEMPTS;
        } catch (NumberFormatException ex) {
            log.warn("Corrupt login-attempt counter for {}", username, ex);
            return false;
        } catch (RuntimeException ex) {
            log.warn("isLocked check failed for {} (Redis down), failing open", username, ex);
            return false;
        }
    }

    public Long getremainingLockTime(String username) {
        try {
            Long remainingLockTime = redisTemplate.getExpire(getKey(username));
            return remainingLockTime == null ? 0 : Math.max(0, remainingLockTime);
        } catch (RuntimeException ex) {
            log.warn("getremainingLockTime failed for {} (Redis down)", username, ex);
            return 0L;
        }
    }

}
