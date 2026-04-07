package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习报告DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningReportDTO {

    private Long studentId;
    private String studentName;

    // 报告生成时间
    private LocalDateTime generatedAt;

    // 学生统计信息
    private StudentStatisticsDTO statistics;

    // 学习建议
    private List<String> suggestions;

    // 最近的学习活动
    private List<RecentActivityDTO> recentActivities;

    /**
     * 最近活动DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityDTO {
        private String activityType; // LEARNING, TEST
        private String description;
        private LocalDateTime timestamp;
    }
}
