package com.example.ancientstars.common;

import com.example.ancientstars.controller.AuthController;
import com.example.ancientstars.controller.VocabularyController;
import com.example.ancientstars.dto.LoginRequest;
import com.example.ancientstars.dto.LoginResponse;
import com.example.ancientstars.dto.VocabularyDTO;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.exception.GlobalExceptionHandler;
import com.example.ancientstars.service.UserService;
import com.example.ancientstars.service.VocabularyService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotBlank;
import org.junit.jupiter.api.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * API响应格式一致性属性测试
 * 
 * **Validates: Requirements 9.2, 9.3**
 * 
 * 属性 15: API响应格式一致性
 * 对于任何API响应，成功时应该返回统一的JSON格式包含data字段，
 * 失败时应该返回包含error字段的JSON格式
 */
@Tag("Feature: vocabulary-learning-system, Property 15: API响应格式一致性")
public class ApiResponseFormatPropertyTest {

    /**
     * 属性测试：验证成功响应的格式一致性
     * 
     * 对于任何成功的API调用，响应应该包含：
     * - success = true
     * - data 字段（可以为null）
     * - timestamp 字段
     * - error 字段应该为null
     */
    @Property(tries = 100)
    void successResponseShouldHaveConsistentFormat(@ForAll("successData") Object data) {
        // 创建成功响应
        ApiResponse<?> response = ApiResponse.success(data);

        // 验证响应格式
        assertTrue(response.isSuccess(), "成功响应的success字段应该为true");
        assertEquals(data, response.getData(), "成功响应的data字段应该包含返回的数据");
        assertNotNull(response.getTimestamp(), "成功响应应该包含timestamp字段");
        assertNull(response.getError(), "成功响应的error字段应该为null");

        // 验证timestamp是合理的时间
        LocalDateTime now = LocalDateTime.now();
        assertTrue(response.getTimestamp().isBefore(now.plusSeconds(1)) &&
                response.getTimestamp().isAfter(now.minusSeconds(5)),
                "timestamp应该是当前时间附近");
    }

    /**
     * 属性测试：验证失败响应的格式一致性
     * 
     * 对于任何失败的API调用，响应应该包含：
     * - success = false
     * - error 字段（包含code和message）
     * - timestamp 字段
     * - data 字段应该为null
     */
    @Property(tries = 100)
    void errorResponseShouldHaveConsistentFormat(
            @ForAll @NotBlank String errorCode,
            @ForAll @NotBlank String errorMessage) {
        
        // 创建失败响应
        ApiResponse<?> response = ApiResponse.error(errorCode, errorMessage);

        // 验证响应格式
        assertFalse(response.isSuccess(), "失败响应的success字段应该为false");
        assertNull(response.getData(), "失败响应的data字段应该为null");
        assertNotNull(response.getError(), "失败响应应该包含error字段");
        assertNotNull(response.getTimestamp(), "失败响应应该包含timestamp字段");
        
        // 验证error对象的结构
        ApiResponse.ErrorInfo error = response.getError();
        assertEquals(errorCode, error.getCode(), "error对象应该包含正确的错误码");
        assertEquals(errorMessage, error.getMessage(), "error对象应该包含正确的错误消息");
        
        // 验证timestamp是合理的时间
        LocalDateTime now = LocalDateTime.now();
        assertTrue(response.getTimestamp().isBefore(now.plusSeconds(1)) &&
                   response.getTimestamp().isAfter(now.minusSeconds(5)),
                   "timestamp应该是当前时间附近");
    }

/**
     * 属性测试：验证Controller成功响应的格式一致性
     * 
     * 测试实际Controller返回的响应格式
     */
    @Property(tries = 100)
    void controllerSuccessResponseShouldHaveConsistentFormat(
            @ForAll("vocabularyDTO") VocabularyDTO vocabularyDTO) {
        
        // Mock服务层
        VocabularyService vocabularyService = mock(VocabularyService.class);
        when(vocabularyService.createVocabulary(any())).thenReturn(vocabularyDTO);
        
        // 创建Controller
        VocabularyController controller = new VocabularyController();
        setField(controller, "vocabularyService", vocabularyService);
        
        // 调用Controller方法
        ApiResponse<VocabularyDTO> response = controller.createVocabulary(vocabularyDTO);
        
        // 验证响应格式
        assertTrue(response.isSuccess(), "Controller成功响应的success字段应该为true");
        assertNotNull(response.getData(), "Controller成功响应应该包含data字段");
        assertNull(response.getError(), "Controller成功响应的error字段应该为null");
        assertNotNull(response.getTimestamp(), "Controller成功响应应该包含timestamp字段");
    }

    /**
     * 属性测试：验证异常处理器返回的错误响应格式一致性
     */
    @Property(tries = 100)
    void exceptionHandlerErrorResponseShouldHaveConsistentFormat(
            @ForAll @NotBlank String errorCode,
            @ForAll @NotBlank String errorMessage) {
        
        // 创建全局异常处理器
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        
        // 创建业务异常
        BusinessException exception = new BusinessException(errorCode, errorMessage);
        
        // 调用异常处理器
        ResponseEntity<ApiResponse<Void>> responseEntity = 
            exceptionHandler.handleBusinessException(exception);
        
        // 获取响应体
        ApiResponse<Void> response = responseEntity.getBody();
        assertNotNull(response, "异常处理器应该返回响应体");
        
        // 验证响应格式
        assertFalse(response.isSuccess(), "异常响应的success字段应该为false");
        assertNull(response.getData(), "异常响应的data字段应该为null");
        assertNotNull(response.getError(), "异常响应应该包含error字段");
        assertNotNull(response.getTimestamp(), "异常响应应该包含timestamp字段");
        
        // 验证error对象
        ApiResponse.ErrorInfo error = response.getError();
        assertEquals(errorCode, error.getCode(), "异常响应应该包含正确的错误码");
        assertEquals(errorMessage, error.getMessage(), "异常响应应该包含正确的错误消息");
    }

    /**
     * 属性测试：验证分页响应的格式一致性
     */
    @Property(tries = 100)
    void paginatedResponseShouldHaveConsistentFormat(
            @ForAll @IntRange(min = 0, max = 100) int totalElements) {
        
        // 创建分页数据
        Page<VocabularyDTO> page = new PageImpl<>(
            Collections.emptyList(),
            PageRequest.of(0, 20),
            totalElements
        );
        
        // 创建成功响应
        ApiResponse<Page<VocabularyDTO>> response = ApiResponse.success(page);
        
        // 验证响应格式
        assertTrue(response.isSuccess(), "分页响应的success字段应该为true");
        assertNotNull(response.getData(), "分页响应应该包含data字段");
        assertNull(response.getError(), "分页响应的error字段应该为null");
        assertNotNull(response.getTimestamp(), "分页响应应该包含timestamp字段");
        
        // 验证分页数据
        Page<VocabularyDTO> responseData = response.getData();
        assertEquals(totalElements, responseData.getTotalElements(),
            "分页响应的data应该包含正确的总元素数");
    }

    /**
     * 属性测试：验证空数据响应的格式一致性
     */
    @Property(tries = 100)
    void emptyDataResponseShouldHaveConsistentFormat() {
        // 创建空数据成功响应
        ApiResponse<Void> response = ApiResponse.success();
        
        // 验证响应格式
        assertTrue(response.isSuccess(), "空数据响应的success字段应该为true");
        assertNull(response.getData(), "空数据响应的data字段应该为null");
        assertNull(response.getError(), "空数据响应的error字段应该为null");
        assertNotNull(response.getTimestamp(), "空数据响应应该包含timestamp字段");
    }

    /**
     * 属性测试：验证使用ErrorCode的错误响应格式一致性
     */
    @Property(tries = 100)
    void errorCodeResponseShouldHaveConsistentFormat() {
        // 使用ErrorCode创建失败响应
        ApiResponse<?> response = ApiResponse.error(ErrorCode.INVALID_PARAMETER);
        
        // 验证响应格式
        assertFalse(response.isSuccess(), "ErrorCode响应的success字段应该为false");
        assertNull(response.getData(), "ErrorCode响应的data字段应该为null");
        assertNotNull(response.getError(), "ErrorCode响应应该包含error字段");
        assertNotNull(response.getTimestamp(), "ErrorCode响应应该包含timestamp字段");
        
        // 验证error对象
        ApiResponse.ErrorInfo error = response.getError();
        assertEquals(ErrorCode.INVALID_PARAMETER.getCode(), error.getCode(),
            "ErrorCode响应应该包含正确的错误码");
        assertEquals(ErrorCode.INVALID_PARAMETER.getMessage(), error.getMessage(),
            "ErrorCode响应应该包含正确的错误消息");
    }

    /**
     * 数据提供者：生成各种类型的成功数据
     */
    @Provide
    Arbitrary<Object> successData() {
        return Arbitraries.oneOf(
            Arbitraries.just(null),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
            Arbitraries.integers(),
            Arbitraries.longs(),
            Arbitraries.doubles()
        );
    }

    /**
     * 数据提供者：生成VocabularyDTO对象
     */
    @Provide
    Arbitrary<VocabularyDTO> vocabularyDTO() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200)
        ).as((word, translation) -> {
            VocabularyDTO dto = new VocabularyDTO();
            dto.setWord(word);
            dto.setTranslation(translation);
            return dto;
        });
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
