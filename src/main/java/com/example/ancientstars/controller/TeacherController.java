package com.example.ancientstars.controller;

import com.example.ancientstars.common.ApiResponse;
import com.example.ancientstars.dto.*;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师控制器
 */
@RestController
@RequestMapping("/api/teacher")
@Tag(name = "教师管理", description = "教师端学生管理接口")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    @Autowired
    private UserService userService;

    /**
     * 创建学生账户
     */
    @PostMapping("/students")
    @Operation(summary = "创建学生账户", description = "教师创建学生账户，系统自动生成初始密码")
    public ApiResponse<CreateStudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        StudentCreationResult result = userService.createStudent(
                request.getUsername(),
                request.getRealName(),
                request.getEmail(),
                request.getClassId());

        CreateStudentResponse response = new CreateStudentResponse(
                result.getStudent().getId(),
                result.getStudent().getUsername(),
                result.getStudent().getRealName(),
                result.getStudent().getEmail(),
                result.getStudent().getClassId(),
                result.getInitialPassword(),
                "学生账户创建成功，请妥善保管初始密码");

        return ApiResponse.success(response);
    }

    /**
     * 获取学生列表
     */
    @GetMapping("/students")
    @Operation(summary = "获取学生列表", description = "获取所有学生或按班级筛选")
    public ApiResponse<List<StudentDTO>> getStudents(@RequestParam(required = false) Long classId) {
        List<User> students = userService.getStudentsByClass(classId);

        List<StudentDTO> studentDTOs = students.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ApiResponse.success(studentDTOs);
    }

    /**
     * 获取学生详情
     */
    @GetMapping("/students/{id}")
    @Operation(summary = "获取学生详情", description = "根据ID获取学生详细信息")
    public ApiResponse<StudentDTO> getStudent(@PathVariable Long id) {
        User student = userService.getUserById(id);
        return ApiResponse.success(convertToDTO(student));
    }

    /**
     * 更新学生信息
     */
    @PutMapping("/students/{id}")
    @Operation(summary = "更新学生信息", description = "更新学生的基本信息")
    public ApiResponse<StudentDTO> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentRequest request) {

        User updatedStudent = userService.updateStudent(
                id,
                request.getRealName(),
                request.getEmail(),
                request.getClassId());

        return ApiResponse.success(convertToDTO(updatedStudent));
    }

    /**
     * 禁用学生账户
     */
    @PostMapping("/students/{id}/disable")
    @Operation(summary = "禁用学生账户", description = "禁用学生账户，学生将无法登录")
    public ApiResponse<Void> disableStudent(@PathVariable Long id) {
        userService.disableUser(id);
        return ApiResponse.success(null);
    }

    /**
     * 启用学生账户
     */
    @PostMapping("/students/{id}/enable")
    @Operation(summary = "启用学生账户", description = "启用已禁用的学生账户")
    public ApiResponse<Void> enableStudent(@PathVariable Long id) {
        userService.enableUser(id);
        return ApiResponse.success(null);
    }

    /**
     * 转换User实体为StudentDTO
     */
    private StudentDTO convertToDTO(User user) {
        return new StudentDTO(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getClassId(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
