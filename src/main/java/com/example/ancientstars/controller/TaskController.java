package com.example.ancientstars.controller;

import com.example.ancientstars.common.ApiResponse;
import com.example.ancientstars.dto.TaskAssignmentDTO;
import com.example.ancientstars.dto.TaskDTO;
import com.example.ancientstars.dto.TaskProgressUpdateRequest;
import com.example.ancientstars.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务管理控制器
 */
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "学习任务相关接口")
public class TaskController {

    private final TaskService taskService;

    /**
     * 创建任务
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "创建任务", description = "教师创建学习任务并分配给学生")
    public ApiResponse<TaskDTO> createTask(@RequestBody TaskDTO taskDTO) {
        TaskDTO result = taskService.createTask(taskDTO);
        return ApiResponse.success(result);
    }

    /**
     * 分配任务给学生
     */
    @PostMapping("/{taskId}/assign")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "分配任务", description = "将任务分配给指定学生")
    public ApiResponse<Void> assignTask(
            @PathVariable Long taskId,
            @RequestBody List<Long> studentIds) {
        taskService.assignTaskToStudents(taskId, studentIds);
        return ApiResponse.success(null);
    }

    /**
     * 更新任务进度
     */
    @PutMapping("/{taskId}/progress")
    @Operation(summary = "更新任务进度", description = "更新学生的任务完成进度")
    public ApiResponse<Void> updateProgress(
            @PathVariable Long taskId,
            @RequestBody TaskProgressUpdateRequest request) {
        taskService.updateTaskProgress(taskId, request);
        return ApiResponse.success(null);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "获取任务详情", description = "根据ID获取任务详细信息")
    public ApiResponse<TaskDTO> getTask(@PathVariable Long taskId) {
        TaskDTO task = taskService.getTaskById(taskId);
        return ApiResponse.success(task);
    }

    /**
     * 获取教师创建的任务列表
     */
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "获取教师任务列表", description = "获取教师创建的所有任务")
    public ApiResponse<List<TaskDTO>> getTeacherTasks(@PathVariable Long teacherId) {
        List<TaskDTO> tasks = taskService.getTasksByTeacher(teacherId);
        return ApiResponse.success(tasks);
    }

    /**
     * 获取学生的任务列表
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "获取学生任务列表", description = "获取分配给学生的所有任务")
    public ApiResponse<List<TaskAssignmentDTO>> getStudentTasks(@PathVariable Long studentId) {
        List<TaskAssignmentDTO> tasks = taskService.getStudentTasks(studentId);
        return ApiResponse.success(tasks);
    }

    /**
     * 获取任务的所有分配记录
     */
    @GetMapping("/{taskId}/assignments")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "获取任务分配记录", description = "获取任务的所有学生分配情况")
    public ApiResponse<List<TaskAssignmentDTO>> getTaskAssignments(@PathVariable Long taskId) {
        List<TaskAssignmentDTO> assignments = taskService.getTaskAssignments(taskId);
        return ApiResponse.success(assignments);
    }

    /**
     * 更新任务信息
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "更新任务", description = "更新任务的基本信息")
    public ApiResponse<TaskDTO> updateTask(
            @PathVariable Long taskId,
            @RequestBody TaskDTO taskDTO) {
        TaskDTO result = taskService.updateTask(taskId, taskDTO);
        return ApiResponse.success(result);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "删除任务", description = "删除指定任务")
    public ApiResponse<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ApiResponse.success(null);
    }
}
