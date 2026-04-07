package com.example.ancientstars.security;

import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT令牌提供者属性测试
 * 验证属性1: 用户认证令牌有效性
 */
@Tag("Feature: vocabulary-learning-system, Property 1: 用户认证令牌有效性")
class JwtTokenProviderPropertyTest {

    private final JwtTokenProvider tokenProvider;

    public JwtTokenProviderPropertyTest() {
        this.tokenProvider = new JwtTokenProvider();
        // 使用反射设置私有字段
        setField(tokenProvider, "jwtSecret",
                "YW5jaWVudFN0YXJzU2VjcmV0S2V5Rm9yVm9jYWJ1bGFyeUxlYXJuaW5nU3lzdGVtMjAyNA==");
        setField(tokenProvider, "jwtExpiration", 7200000L);
        setField(tokenProvider, "refreshExpiration", 604800000L);
    }

    @Property(tries = 100)
    @Label("属性1: 对于任何有效的用户凭据，登录后生成的JWT令牌应该能够通过验证并正确解析出用户信息")
    void jwtTokenShouldBeValidAndParseable(@ForAll("validUsernames") String username) {
        // 创建认证对象
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER")))
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // 生成令牌
        String token = tokenProvider.generateToken(authentication);

        // 验证令牌
        assertTrue(tokenProvider.validateToken(token), "生成的令牌应该是有效的");

        // 解析令牌
        String parsedUsername = tokenProvider.getUsernameFromToken(token);
        assertEquals(username, parsedUsername, "解析出的用户名应该与原始用户名一致");
    }

    @Property(tries = 100)
    @Label("刷新令牌应该能够通过验证并正确解析出用户信息")
    void refreshTokenShouldBeValidAndParseable(@ForAll("validUsernames") String username) {
        // 生成刷新令牌
        String refreshToken = tokenProvider.generateRefreshToken(username);

        // 验证令牌
        assertTrue(tokenProvider.validateToken(refreshToken), "生成的刷新令牌应该是有效的");

        // 解析令牌
        String parsedUsername = tokenProvider.getUsernameFromToken(refreshToken);
        assertEquals(username, parsedUsername, "解析出的用户名应该与原始用户名一致");
    }

    @Property(tries = 100)
    @Label("无效的令牌应该验证失败")
    void invalidTokenShouldFailValidation(@ForAll("invalidTokens") String invalidToken) {
        // 验证无效令牌 - validateToken方法会捕获异常并返回false
        boolean isValid = tokenProvider.validateToken(invalidToken);
        assertFalse(isValid, "无效的令牌应该验证失败，但返回了: " + isValid);
    }

    @Provide
    Arbitrary<String> validUsernames() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .ofMinLength(3)
                .ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> invalidTokens() {
        return Arbitraries.oneOf(
                Arbitraries.just(""),
                Arbitraries.just("invalid-token"),
                Arbitraries.just("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature"),
                Arbitraries.strings().ofMinLength(1).ofMaxLength(50));
    }

    /**
     * 使用反射设置私有字段
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
