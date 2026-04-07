package com.example.ancientstars.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API响应格式测试
 */
class ApiResponseTest {

    @Test
    void testSuccessResponseWithData() {
        String testData = "test data";
        ApiResponse<String> response = ApiResponse.success(testData);

        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertNull(response.getError());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testSuccessResponseWithoutData() {
        ApiResponse<Void> response = ApiResponse.success();

        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertNull(response.getError());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testErrorResponseWithCodeAndMessage() {
        String errorCode = "1001";
        String errorMessage = "系统错误";
        ApiResponse<Void> response = ApiResponse.error(errorCode, errorMessage);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals(errorCode, response.getError().getCode());
        assertEquals(errorMessage, response.getError().getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testErrorResponseWithErrorCode() {
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.UNAUTHORIZED);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), response.getError().getCode());
        assertEquals(ErrorCode.UNAUTHORIZED.getMessage(), response.getError().getMessage());
        assertNotNull(response.getTimestamp());
    }
}
