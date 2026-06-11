package com.rmtech.ecom.Config;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RedisConfig {

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                System.out.println("Redis GET failed: " + exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                System.out.println("Redis PUT failed: " + exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                System.out.println("Redis EVICT failed: " + exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                System.out.println("Redis CLEAR failed: " + exception.getMessage());
            }
        };
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory){

        Map <String, RedisCacheConfiguration > configs=new HashMap<>();
        configs.put("Product",RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(java.time.Duration.ofMinutes(10)));

        configs.put("Category",RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(java.time.Duration.ofMinutes(5)));


        return RedisCacheManager.builder(redisConnectionFactory)
                .withInitialCacheConfigurations(configs)
                .build();
    }

    @Bean
    public RedisTemplate<String,String> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> template=new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
    @Bean
    public RedisTemplate<String,Object> redisTemplate2(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> template=new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
