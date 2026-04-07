package com.example.ancientstars.controller;

import com.example.ancientstars.common.ApiResponse;
import com.example.ancientstars.dto.StudentProgressDTO;
import com.example.ancientstars.security.CustomUserDetails;
import com.example.ancientstars.service.DailyProgressService;
import com.example.ancientstars.service.TeacherAuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 教师进度查询控制器
 * 处理教师查看学生自主学习进度的接口
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "教师进度查询", description = "教师查看学生学习进度接口")
public class TeacherProgressController {

    private final DailyProgressService dailyProgressService;
    private final TeacherAuthorizationService teacherAuthorizationService;

    /**
     * 查询学生学习进度
     * 教师只能查看自己班级学生的进度
     */
    @GetMapping("/student-progress/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "查询学生学习进度", description = "教师查看学生的自主学习进度，只能查看自己班级的学生")
    public ApiResponse<StudentProgressDTO> getStudentProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "学生ID") @PathVariable Long studentId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long teacherId = userDetails.getUserId();
        log.info("教师 {} 查询学生 {} 的学习进度，开始日期: {}, 结束日期: {}",
                teacherId, studentId, startDate, endDate);

        // 验证教师是否有权限查看该学生的进度
        teacherAuthorizationService.verifyTeacherCanViewStudent(teacherId, studentId);

        // 查询学生进度
        StudentProgressDTO progress = dailyProgressService.getStudentProgress(studentId, startDate, endDate);

        return ApiResponse.success(progress);
    }

    /**
     * 查询班级学习统计
     * 返回班级整体统计和排名
     */
    @GetMapping("/class-progress")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "查询班级学习统计", description = "教师查看班级整体自主学习统计，包括平均每日学习量和学习最积极的学生")
    public ApiResponse<com.example.ancientstars.dto.ClassProgressDTO> getClassProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "班级ID（可选，默认为教师所在班级）") @RequestParam(required = false) Long classId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long teacherId = userDetails.getUserId();
        log.info("教师 {} 查询班级学习统计，班级ID: {}, 开始日期: {}, 结束日期: {}",
                teacherId, classId, startDate, endDate);

        // 如果没有指定班级ID，使用教师自己的班级ID
        Long effectiveClassId = classId;
        if (effectiveClassId == null) {
            // 获取教师信息以获取班级ID
            effectiveClassId = teacherAuthorizationService.getTeacherClassId(teacherId);
        } else {
            // 如果指定了班级ID，验证教师是否有权限查看该班级
            teacherAuthorizationService.verifyTeacherCanViewClass(teacherId, effectiveClassId);
        }

        // 查询班级进度
        com.example.ancientstars.dto.ClassProgressDTO progress = dailyProgressService.getClassProgress(
                effectiveClassId, startDate, endDate);

        return ApiResponse.success(progress);
    }
}
