package com.example.ancientstars.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存配置类
 * 配置不同缓存区域的过期时间和序列化策略
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 缓存名称常量
     */
    public static final String VOCABULARY_CACHE = "vocabulary";
    public static final String STUDENT_PROGRESS_CACHE = "studentProgress";
    public static final String CLASS_PROGRESS_CACHE = "classProgress";
    public static final String REVIEW_REMINDERS_CACHE = "reviewReminders";

    /**
     * 配置缓存管理器
     * 为不同的缓存区域设置不同的过期时间
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置（1小时）
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // 为不同缓存区域配置不同的过期时间
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 词汇查询缓存：24小时
        cacheConfigurations.put(VOCABULARY_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(24)));

        // 学生进度统计缓存：5分钟
        cacheConfigurations.put(STUDENT_PROGRESS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 班级进度统计缓存：5分钟
        cacheConfigurations.put(CLASS_PROGRESS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 复习提醒缓存：10分钟
        cacheConfigurations.put(REVIEW_REMINDERS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
