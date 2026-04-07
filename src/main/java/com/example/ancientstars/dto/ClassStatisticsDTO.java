package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 班级统计DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassStatisticsDTO {

    private Long classId;
    private String className;

    // 学生总数
    private Long totalStudents;

    // 平均进度（百分比）
    private BigDecimal averageProgress;

    // 平均分
    private BigDecimal averageScore;

    // 成绩分布（分数段 -> 人数）
    private Map<String, Long> scoreDistribution;

    // 总测试次数
    private Long totalTests;
}
