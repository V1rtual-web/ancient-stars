package com.example.ancientstars.controller;

import com.example.ancientstars.common.ApiResponse;
import com.example.ancientstars.dto.LearningRecordDTO;
import com.example.ancientstars.dto.MarkMasteryRequest;
import com.example.ancientstars.entity.LearningRecord;
import com.example.ancientstars.entity.LearningRecord.MasteryLevel;
import com.example.ancientstars.service.LearningRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习记录控制器
 */
@RestController
@RequestMapping("/api/learning")
@Tag(name = "学习记录管理", description = "学习记录相关接口")
public class LearningRecordController {

    @Autowired
    private LearningRecordService learningRecordService;

    /**
     * 标记词汇掌握度
     */
    @PostMapping("/mark-mastery")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "标记词汇掌握度", description = "学生标记词汇的掌握程度")
    public ResponseEntity<ApiResponse<LearningRecordDTO>> markMastery(@RequestBody MarkMasteryRequest request) {
        LearningRecord record = learningRecordService.markMastery(
                request.getStudentId(),
                request.getVocabularyId(),
                request.getTaskId(),
                request.getMasteryLevel());

        LearningRecordDTO dto = learningRecordService.getLearningRecordById(record.getId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * 获取学生的学习记录（分页）
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "获取学生学习记录", description = "分页查询学生的学习记录")
    public ResponseEntity<ApiResponse<Page<LearningRecordDTO>>> getStudentRecords(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastReviewedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<LearningRecordDTO> records = learningRecordService.getStudentLearningRecords(studentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * 获取学生在特定任务中的学习记录
     */
    @GetMapping("/student/{studentId}/task/{taskId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "获取任务学习记录", description = "获取学生在特定任务中的学习记录")
    public ResponseEntity<ApiResponse<List<LearningRecordDTO>>> getStudentTaskRecords(
            @PathVariable Long studentId,
            @PathVariable Long taskId) {

        List<LearningRecordDTO> records = learningRecordService.getStudentTaskRecords(studentId, taskId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * 获取学生特定掌握程度的词汇
     */
    @GetMapping("/student/{studentId}/mastery/{masteryLevel}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "按掌握度查询", description = "获取学生特定掌握程度的词汇")
    public ResponseEntity<ApiResponse<List<LearningRecordDTO>>> getRecordsByMastery(
            @PathVariable Long studentId,
            @PathVariable MasteryLevel masteryLevel) {

        List<LearningRecordDTO> records = learningRecordService.getStudentRecordsByMastery(studentId, masteryLevel);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * 统计学生已掌握的词汇数量
     */
    @GetMapping("/student/{studentId}/mastered-count")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "统计已掌握词汇", description = "统计学生已掌握的词汇数量")
    public ResponseEntity<ApiResponse<Long>> getMasteredCount(@PathVariable Long studentId) {
        long count = learningRecordService.countMasteredVocabulary(studentId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * 统计学生在特定任务中已掌握的词汇数量
     */
    @GetMapping("/student/{studentId}/task/{taskId}/mastered-count")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "统计任务已掌握词汇", description = "统计学生在特定任务中已掌握的词汇数量")
    public ResponseEntity<ApiResponse<Long>> getTaskMasteredCount(
            @PathVariable Long studentId,
            @PathVariable Long taskId) {

        long count = learningRecordService.countTaskMasteredVocabulary(studentId, taskId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * 获取学习记录详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @Operation(summary = "获取学习记录详情", description = "根据ID获取学习记录详情")
    public ResponseEntity<ApiResponse<LearningRecordDTO>> getLearningRecord(@PathVariable Long id) {
        LearningRecordDTO record = learningRecordService.getLearningRecordById(id);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    /**
     * 删除学习记录
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "删除学习记录", description = "删除指定的学习记录")
    public ResponseEntity<ApiResponse<Void>> deleteLearningRecord(@PathVariable Long id) {
        learningRecordService.deleteLearningRecord(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
