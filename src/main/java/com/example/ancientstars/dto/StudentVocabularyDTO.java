package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生词汇DTO（包含学习状态和复习信息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentVocabularyDTO {

    private Long id;
    private String word;
    private String phonetic;
    private String definition;
    private String translation;
    private String example;
    private Integer difficultyLevel;
    private Boolean isLearned; // 是否已学习
    private String masteryLevel; // 掌握程度：NEW/LEARNING/FAMILIAR/MASTERED
    private LocalDateTime nextReviewTime; // 下次复习时间
    private Boolean needsReview; // 是否今日需要复习
    private Integer reviewCount; // 已复习次数
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
