package com.example.ancientstars.controller;

import com.example.ancientstars.common.ApiResponse;
import com.example.ancientstars.dto.CompleteReviewResponse;
import com.example.ancientstars.dto.CreateStudyRecordRequest;
import com.example.ancientstars.dto.ReviewReminderDTO;
import com.example.ancientstars.dto.StudyRecordDTO;
import com.example.ancientstars.dto.StudentProgressDTO;
import com.example.ancientstars.entity.StudyRecord;
import com.example.ancientstars.security.CustomUserDetails;
import com.example.ancientstars.service.DailyProgressService;
import com.example.ancientstars.service.ReviewReminderService;
import com.example.ancientstars.service.StudyRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 学生学习记录控制器
 * 处理学生自主背单词的学习记录创建和复习提醒
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "学生学习记录", description = "学生自主学习记录和复习提醒接口")
public class StudentStudyController {

    private final StudyRecordService studyRecordService;
    private final ReviewReminderService reviewReminderService;
    private final DailyProgressService dailyProgressService;

    /**
     * 创建学习记录
     * 当学生标记已掌握某个词汇时调用此接口
     */
    @PostMapping("/study-records")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "创建学习记录", description = "学生标记已掌握词汇，创建学习记录")
    public ApiResponse<StudyRecordDTO> createStudyRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateStudyRecordRequest request) {

        Long studentId = userDetails.getUserId();
        log.info("学生 {} 创建学习记录，词汇ID: {}", studentId, request.getVocabularyId());

        // 调用服务层创建学习记录
        StudyRecord studyRecord = studyRecordService.createStudyRecord(
                studentId,
                request.getVocabularyId());

        // 转换为DTO并返回
        StudyRecordDTO responseDTO = StudyRecordDTO.fromEntity(studyRecord);
        return ApiResponse.success(responseDTO);
    }

    /**
     * 获取待复习词汇列表
     * 查询当前学生的所有待复习词汇
     */
    @GetMapping("/review-reminders")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "获取待复习词汇列表", description = "查询学生的待复习词汇列表")
    public ApiResponse<List<ReviewReminderDTO>> getReviewReminders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long studentId = userDetails.getUserId();
        log.info("学生 {} 查询待复习词汇列表", studentId);

        List<ReviewReminderDTO> reminders = reviewReminderService.getStudentRemindersWithDetails(studentId);

        return ApiResponse.success(reminders);
    }

    /**
     * 完成复习
     * 标记某个复习提醒为已完成，并更新学习记录
     */
    @PostMapping("/review-reminders/{id}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "完成复习", description = "标记复习提醒为已完成，更新学习记录")
    public ApiResponse<CompleteReviewResponse> completeReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        Long studentId = userDetails.getUserId();
        log.info("学生 {} 完成复习，提醒ID: {}", studentId, id);

        // 完成复习并获取更新后的学习记录
        StudyRecord updatedRecord = reviewReminderService.completeReview(id);

        // 构建响应
        CompleteReviewResponse response = CompleteReviewResponse.success(updatedRecord.getNextReviewTime());

        return ApiResponse.success(response);
    }

    /**
     * 获取学生学习进度统计
     * 返回今日、本周、累计统计数据以及每日进度详情
     */
    @GetMapping("/progress")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "获取学习进度统计", description = "查询学生的学习进度，包括今日、本周、累计统计和每日进度详情")
    public ApiResponse<StudentProgressDTO> getStudentProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long studentId = userDetails.getUserId();
        log.info("学生 {} 查询学习进度，开始日期: {}, 结束日期: {}", studentId, startDate, endDate);

        StudentProgressDTO progress = dailyProgressService.getStudentProgress(studentId, startDate, endDate);

        return ApiResponse.success(progress);
    }
}
