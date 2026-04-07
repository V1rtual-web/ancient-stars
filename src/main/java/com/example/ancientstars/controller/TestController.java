package com.example.ancientstars.controller;

import com.example.ancientstars.common.ApiResponse;
import com.example.ancientstars.dto.*;
import com.example.ancientstars.service.TestRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "测试管理", description = "测试相关接口")
public class TestController {

    private final TestRecordService testRecordService;

    /**
     * 生成测试题目
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "生成测试题目", description = "根据任务ID和题型生成测试题目")
    public ApiResponse<List<TestQuestionDTO>> generateTest(@RequestBody TestGenerateRequest request) {
        List<TestQuestionDTO> questions = testRecordService.generateTest(request);
        return ApiResponse.success(questions);
    }

    /**
     * 提交测试答案
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "提交测试答案", description = "提交测试答案并获取评分结果")
    public ApiResponse<TestResultDTO> submitTest(@RequestBody TestSubmitRequest request) {
        TestResultDTO result = testRecordService.submitTest(request);
        return ApiResponse.success(result);
    }

    /**
     * 查询历史成绩
     */
    @GetMapping("/history/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    @Operation(summary = "查询历史成绩", description = "查询学生的历史测试成绩")
    public ApiResponse<Page<TestHistoryDTO>> getTestHistory(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TestHistoryDTO> history = testRecordService.getTestHistory(studentId, page, size);
        return ApiResponse.success(history);
    }

    /**
     * 查询测试详情
     */
    @GetMapping("/{testId}/details")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    @Operation(summary = "查询测试详情", description = "查询测试详情，包括错题列表")
    public ApiResponse<TestResultDTO> getTestDetails(@PathVariable Long testId) {
        TestResultDTO details = testRecordService.getTestDetails(testId);
        return ApiResponse.success(details);
    }
}
