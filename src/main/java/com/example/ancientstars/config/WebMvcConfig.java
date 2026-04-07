package com.example.ancientstars.config;

import com.example.ancientstars.security.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 注册拦截器和其他Web相关配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册限流拦截器
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**") // 对所有API接口进行限流
                .excludePathPatterns(
                        "/api/auth/**", // 排除认证接口
                        "/api-docs/**", // 排除API文档
                        "/swagger-ui/**", // 排除Swagger UI
                        "/swagger-ui.html" // 排除Swagger UI
                );
    }
}
