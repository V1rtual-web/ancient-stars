package com.example.ancientstars.dto;

import com.example.ancientstars.entity.LearningRecord.MasteryLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学习记录数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningRecordDTO {

    private Long id;
    private Long studentId;
    private Long vocabularyId;
    private Long taskId;
    private MasteryLevel masteryLevel;
    private Integer reviewCount;
    private LocalDateTime lastReviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 扩展字段（用于返回详细信息）
    private String word; // 单词
    private String translation; // 释义
    private String phonetic; // 音标
    private String taskTitle; // 任务标题
}
