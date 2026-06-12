package com.rmtech.ecom.Config;

import com.rmtech.ecom.DTOS.RateLimitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimiterService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final RateLimitConfig AUTH_LOGIN_LIMIT = new RateLimitConfig(10, Duration.ofMinutes(15));
    private static final RateLimitConfig AUTH_REFRESH_LIMIT = new RateLimitConfig(30, Duration.ofMinutes(15));
    private static final RateLimitConfig AUTH_LOGOUT_LIMIT = new RateLimitConfig(30, Duration.ofMinutes(1));
    private static final RateLimitConfig PUBLIC_REGISTER_LIMIT = new RateLimitConfig(5, Duration.ofHours(1));
    private static final RateLimitConfig PUBLIC_READ_LIMIT = new RateLimitConfig(120, Duration.ofMinutes(1));
    private static final RateLimitConfig PUBLIC_FILTER_LIMIT = new RateLimitConfig(60, Duration.ofMinutes(1));
    private static final RateLimitConfig USER_READ_LIMIT = new RateLimitConfig(60, Duration.ofMinutes(1));
    private static final RateLimitConfig USER_WRITE_LIMIT = new RateLimitConfig(30, Duration.ofMinutes(1));
    private static final RateLimitConfig ORDER_MUTATION_LIMIT = new RateLimitConfig(10, Duration.ofMinutes(1));
    private static final RateLimitConfig ADMIN_READ_LIMIT = new RateLimitConfig(60, Duration.ofMinutes(1));
    private static final RateLimitConfig ADMIN_WRITE_LIMIT = new RateLimitConfig(20, Duration.ofMinutes(1));
    private static final RateLimitConfig ADMIN_DELETE_LIMIT = new RateLimitConfig(10, Duration.ofMinutes(1));
    private static final RateLimitConfig ADMIN_STOCK_LIMIT = new RateLimitConfig(30, Duration.ofMinutes(1));

    private static final List<RateLimitRule> LIMITS = List.of(
            rule("POST", "/auth/login", "auth-login", AUTH_LOGIN_LIMIT),
            rule("POST", "/auth/refresh", "auth-refresh", AUTH_REFRESH_LIMIT),
            rule("POST", "/auth/logout", "auth-logout", AUTH_LOGOUT_LIMIT),

            rule("POST", "/public/user", "public-user-register", PUBLIC_REGISTER_LIMIT),
            rule("GET", "/public/products", "public-products", PUBLIC_READ_LIMIT),
            rule("GET", "/public/expensiveproducts", "public-expensive-products", PUBLIC_FILTER_LIMIT),
            rule("GET", "/public/cheapproducts", "public-cheap-products", PUBLIC_FILTER_LIMIT),
            rule("GET", "/public/parentcategorizedproducts", "public-parent-categorized-products", PUBLIC_FILTER_LIMIT),
            rule("GET", "/public/product/[^/]+", "public-product-detail", PUBLIC_READ_LIMIT),

            rule("GET", "/user/register", "user-profile", USER_READ_LIMIT),
            rule("PATCH", "/user/update", "user-update", USER_WRITE_LIMIT),
            rule("DELETE", "/user/delete", "user-delete", ADMIN_DELETE_LIMIT),
            rule("GET", "/user/getorderstatus", "user-order-status", USER_READ_LIMIT),

            rule("POST", "/user/cart/product/[^/]+", "cart-add-product", USER_WRITE_LIMIT),
            rule("DELETE", "/user/cart/product/[^/]+", "cart-delete-product", USER_WRITE_LIMIT),
            rule("GET", "/user/cart/cart", "cart-get", USER_READ_LIMIT),
            rule("DELETE", "/user/cart/cart", "cart-clear", USER_WRITE_LIMIT),

            rule("POST", "/user/orders/placeorder", "order-place", ORDER_MUTATION_LIMIT),
            rule("GET", "/user/orders/orders", "orders-list", USER_READ_LIMIT),
            rule("GET", "/user/orders/order/[^/]+", "order-detail", USER_READ_LIMIT),
            rule("DELETE", "/user/orders/order/[^/]+", "order-cancel", ORDER_MUTATION_LIMIT),

            rule("GET", "/admin/users", "admin-users", ADMIN_READ_LIMIT),
            rule("POST", "/admin/category", "admin-category-create", ADMIN_WRITE_LIMIT),
            rule("GET", "/admin/categorytree/[^/]+", "admin-category-tree", ADMIN_READ_LIMIT),
            rule("GET", "/admin/categories", "admin-categories", ADMIN_READ_LIMIT),
            rule("GET", "/admin/user/[^/]+", "admin-user-detail", ADMIN_READ_LIMIT),
            rule("DELETE", "/admin/user/[^/]+", "admin-user-delete", ADMIN_DELETE_LIMIT),
            rule("POST", "/admin/product", "admin-product-create", ADMIN_WRITE_LIMIT),
            rule("GET", "/admin/orders", "admin-orders", ADMIN_READ_LIMIT),
            rule("PATCH", "/admin/product/[^/]+", "admin-product-update", ADMIN_WRITE_LIMIT),
            rule("DELETE", "/admin/product/[^/]+", "admin-product-delete", ADMIN_DELETE_LIMIT),
            rule("PATCH", "/admin/admin", "admin-self-update", ADMIN_WRITE_LIMIT),
            rule("PATCH", "/admin/setorderstatus", "admin-order-status-update", ADMIN_WRITE_LIMIT),
            rule("GET", "/admin/getorderstatus", "admin-order-status", ADMIN_READ_LIMIT),
            rule("DELETE", "/admin/admin", "admin-self-delete", ADMIN_DELETE_LIMIT),
            rule("POST", "/admin/addstock/[^/]+", "admin-stock-add", ADMIN_STOCK_LIMIT),
            rule("POST", "/admin/removestock/[^/]+", "admin-stock-remove", ADMIN_STOCK_LIMIT)
    );

    public boolean isAllowed(String method, String path, String clientIdentifier) {
        RateLimitRule rule = getRule(method, path);

        if (rule == null) {
            return true;
        }

        String key = "ratelimit:" + rule.key() + ":" + clientIdentifier;

        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);
            if (currentCount == null) {
                log.warn("Redis returned null for rate limit key {}", key);
                return true;
            }
            if (currentCount == 1) {
                redisTemplate.expire(key, rule.config().duration());
            }
            return currentCount <= rule.config().maxRequests();
        } catch (RuntimeException ex) {
            log.warn("Rate limiter unavailable for key {}", key, ex);
            return true;
        }
    }

    public RateLimitConfig getLimit(String method, String path) {
        RateLimitRule rule = getRule(method, path);
        return rule == null ? null : rule.config();
    }

    public RateLimitConfig getLimit(String path) {
        return LIMITS.stream()
                .filter(rule -> rule.pathPattern().matcher(path).matches())
                .findFirst()
                .map(RateLimitRule::config)
                .orElse(null);
    }

    private RateLimitRule getRule(String method, String path) {
        return LIMITS.stream()
                .filter(rule -> rule.method().equalsIgnoreCase(method))
                .filter(rule -> rule.pathPattern().matcher(path).matches())
                .findFirst()
                .orElse(null);
    }

    private static RateLimitRule rule(String method, String pathRegex, String key, RateLimitConfig config) {
        return new RateLimitRule(method, Pattern.compile("^" + pathRegex + "$"), key, config);
    }

    private record RateLimitRule(
            String method,
            Pattern pathPattern,
            String key,
            RateLimitConfig config
    ) {
    }
}
