package com.example.ancientstars.security;

import com.example.ancientstars.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * API限流拦截器
 * 使用令牌桶算法限制每个用户的API调用频率
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitConfig rateLimitConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 获取用户标识（优先使用用户名，否则使用IP地址）
        String key = getUserKey(request);

        // 获取该用户的令牌桶
        Bucket bucket = rateLimitConfig.resolveBucket(key);

        // 尝试消费一个令牌
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // 令牌消费成功，允许请求通过
            // 在响应头中添加限流信息
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            log.debug("API请求通过限流检查 - 用户: {}, 剩余令牌: {}", key, probe.getRemainingTokens());
            return true;
        } else {
            // 令牌不足，拒绝请求
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));

            String errorMessage = String.format(
                    "{\"success\":false,\"code\":429,\"message\":\"请求过于频繁，请在%d秒后重试\",\"data\":null}",
                    waitForRefill);
            response.getWriter().write(errorMessage);

            log.warn("API请求被限流拒绝 - 用户: {}, 需等待: {}秒", key, waitForRefill);
            return false;
        }
    }

    /**
     * 获取用户标识
     * 优先使用已认证用户的用户名，否则使用IP地址
     * 
     * @param request HTTP请求
     * @return 用户标识
     */
    private String getUserKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            // 使用已认证用户的用户名
            return "user:" + authentication.getName();
        } else {
            // 使用IP地址
            String ipAddress = getClientIP(request);
            return "ip:" + ipAddress;
        }
    }

    /**
     * 获取客户端真实IP地址
     * 考虑代理和负载均衡的情况
     * 
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For可能包含多个IP，取第一个
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}
