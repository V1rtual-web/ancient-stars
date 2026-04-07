package com.example.ancientstars.exception;

import com.example.ancientstars.common.ApiResponse;
import com.example.ancientstars.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局异常处理器测试
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleBusinessException() {
        BusinessException exception = new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBusinessException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), response.getBody().getError().getCode());
    }

    @Test
    void testHandleBadCredentialsException() {
        BadCredentialsException exception = new BadCredentialsException("用户名或密码错误");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleAuthenticationException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.LOGIN_FAILED.getCode(), response.getBody().getError().getCode());
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("权限不足");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleAccessDeniedException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.FORBIDDEN.getCode(), response.getBody().getError().getCode());
    }

    @Test
    void testHandleSQLException() {
        SQLException exception = new SQLException("数据库连接失败");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleSQLException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.DATABASE_ERROR.getCode(), response.getBody().getError().getCode());
    }

    @Test
    void testHandleGenericException() {
        Exception exception = new Exception("未知错误");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), response.getBody().getError().getCode());
    }
}
