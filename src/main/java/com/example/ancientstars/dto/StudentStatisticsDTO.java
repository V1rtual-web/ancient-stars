package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 学生统计DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatisticsDTO {

    private Long studentId;
    private String studentName;

    // 学习时长（天数）
    private Long learningDays;

    // 掌握词汇数
    private Long masteredVocabularyCount;

    // 测试次数
    private Long testCount;

    // 平均分
    private BigDecimal averageScore;

    // 成绩趋势（最近5次测试分数）
    private List<BigDecimal> scoresTrend;

    // 学习记录总数
    private Long totalLearningRecords;
}
