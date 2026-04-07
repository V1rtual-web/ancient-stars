package com.example.ancientstars.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Web配置测试
 */
@SpringBootTest
class WebConfigTest {

    @Autowired
    private WebConfig webConfig;

    @Test
    void testWebConfigLoaded() {
        assertNotNull(webConfig, "WebConfig应该被成功加载");
    }

    @Test
    void testCorsConfiguration() {
        // 验证CORS配置方法存在且可调用
        CorsRegistry registry = new CorsRegistry();
        webConfig.addCorsMappings(registry);
        // 如果没有抛出异常，说明配置正常
    }
}
