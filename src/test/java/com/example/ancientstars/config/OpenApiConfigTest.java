package com.example.ancientstars.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenAPI配置测试
 */
@SpringBootTest
class OpenApiConfigTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    void testOpenAPIConfiguration() {
        assertNotNull(openAPI, "OpenAPI配置不应为空");
        assertNotNull(openAPI.getInfo(), "API信息不应为空");
        assertEquals("旧星背单词学习管理系统 API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void testSecuritySchemeConfiguration() {
        assertNotNull(openAPI.getComponents(), "组件配置不应为空");
        assertNotNull(openAPI.getComponents().getSecuritySchemes(), "安全方案不应为空");
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("bearer-jwt"),
                "应该包含bearer-jwt安全方案");
    }

    @Test
    void testSecurityRequirement() {
        assertNotNull(openAPI.getSecurity(), "安全要求不应为空");
        assertFalse(openAPI.getSecurity().isEmpty(), "安全要求列表不应为空");
    }
}
