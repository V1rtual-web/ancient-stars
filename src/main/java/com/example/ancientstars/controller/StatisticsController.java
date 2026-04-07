package com.example.ancientstars.controller;

import com.example.ancientstars.common.ApiResponse;
import com.example.ancientstars.dto.ClassStatisticsDTO;
import com.example.ancientstars.dto.LearningReportDTO;
import com.example.ancientstars.dto.StudentStatisticsDTO;
import com.example.ancientstars.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 统计分析控制器
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "统计分析", description = "统计分析相关接口")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取学生个人统计
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    @Operation(summary = "获取学生个人统计", description = "获取学生的学习时长、掌握词汇数、成绩趋势等统计信息")
    public ApiResponse<StudentStatisticsDTO> getStudentStatistics(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        StudentStatisticsDTO statistics = statisticsService.getStudentStatistics(studentId, startDate, endDate);
        return ApiResponse.success(statistics);
    }

    /**
     * 获取班级统计
     */
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "获取班级统计", description = "获取班级的整体进度、成绩分布等统计信息")
    public ApiResponse<ClassStatisticsDTO> getClassStatistics(
            @PathVariable Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ClassStatisticsDTO statistics = statisticsService.getClassStatistics(classId, startDate, endDate);
        return ApiResponse.success(statistics);
    }

    /**
     * 生成学习报告
     */
    @GetMapping("/report/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    @Operation(summary = "生成学习报告", description = "生成包含详细统计信息和学习建议的学习报告")
    public ApiResponse<LearningReportDTO> generateLearningReport(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LearningReportDTO report = statisticsService.generateLearningReport(studentId, startDate, endDate);
        return ApiResponse.success(report);
    }
}
