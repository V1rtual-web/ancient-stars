package com.example.ancientstars.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库配置类
 * 
 * 提供数据库连接池配置和连接测试
 */
@Configuration
@Slf4j
public class DatabaseConfig {

    private final DataSource dataSource;

    public DatabaseConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 应用启动时测试数据库连接
     */
    @PostConstruct
    public void testDatabaseConnection() {
        int maxRetries = 3;
        int retryDelay = 2000; // 2秒
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                testConnection();
                log.info("数据库连接测试成功");
                return;
            } catch (Exception e) {
                log.warn("数据库连接测试失败 (尝试 {}/{}): {}", i + 1, maxRetries, e.getMessage());
                
                if (i < maxRetries - 1) {
                    try {
                        Thread.sleep(retryDelay);
                        retryDelay *= 2; // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("数据库连接测试失败，已达到最大重试次数");
                    log.error("请检查数据库配置和数据库服务是否正常运行");
                }
            }
        }
    }

    /**
     * 测试数据库连接
     */
    private void testConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                log.debug("数据库连接有效");
            } else {
                throw new SQLException("数据库连接无效");
            }
        }
    }
}
