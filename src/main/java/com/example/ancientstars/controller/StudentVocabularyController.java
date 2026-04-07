package com.example.ancientstars.controller;

import com.example.ancientstars.common.ApiResponse;
import com.example.ancientstars.dto.PageResponse;
import com.example.ancientstars.dto.StudentVocabularyDTO;
import com.example.ancientstars.security.CustomUserDetails;
import com.example.ancientstars.service.StudentVocabularyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 学生词汇浏览控制器
 */
@RestController
@RequestMapping("/api/student/vocabularies")
@RequiredArgsConstructor
@Tag(name = "学生词汇浏览", description = "学生自主学习词汇浏览接口")
public class StudentVocabularyController {

    private final StudentVocabularyService studentVocabularyService;

    /**
     * 获取词汇列表（支持分页、筛选、搜索）
     */
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "获取词汇列表", description = "学生浏览词汇列表，支持分页、按词汇表筛选、关键词搜索")
    public ApiResponse<PageResponse<StudentVocabularyDTO>> getVocabularies(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "词汇表ID筛选") @RequestParam(required = false) Long wordListId,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword) {

        Long studentId = userDetails.getUserId();
        PageResponse<StudentVocabularyDTO> response = studentVocabularyService.getVocabulariesForStudent(
                studentId, page, size, wordListId, keyword);

        return ApiResponse.success(response);
    }
}
