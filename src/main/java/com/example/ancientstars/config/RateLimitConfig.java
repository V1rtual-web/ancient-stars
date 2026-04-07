package com.example.ancientstars.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API限流配置
 * 使用Bucket4j实现令牌桶算法进行限流
 */
@Configuration
public class RateLimitConfig {

    /**
     * 存储每个用户的令牌桶
     * Key: 用户标识（IP地址或用户ID）
     * Value: 令牌桶实例
     */
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * 获取或创建用户的令牌桶
     * 限流规则：每分钟60次请求
     * 
     * @param key 用户标识
     * @return 令牌桶实例
     */
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * 创建新的令牌桶
     * 配置：容量60个令牌，每分钟补充60个令牌
     * 
     * @return 新的令牌桶实例
     */
    private Bucket createNewBucket() {
        // 定义带宽限制：每分钟60个令牌
        Bandwidth limit = Bandwidth.builder()
                .capacity(60)
                .refillIntervally(60, Duration.ofMinutes(1))
                .build();

        // 创建并返回令牌桶
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * 清理缓存中的令牌桶
     * 可以定期调用此方法清理不活跃的令牌桶
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * 获取缓存大小
     * 
     * @return 当前缓存的令牌桶数量
     */
    public int getCacheSize() {
        return cache.size();
    }
}
