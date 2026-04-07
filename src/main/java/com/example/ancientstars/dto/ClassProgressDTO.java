package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 班级学习进度DTO
 * 用于教师查看班级整体自主学习统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassProgressDTO {

    /**
     * 班级ID
     */
    private Long classId;

    /**
     * 学生总数
     */
    private Integer totalStudents;

    /**
     * 活跃学生数（在查询时间范围内有学习记录的学生）
     */
    private Integer activeStudents;

    /**
     * 平均每日学习单词数
     */
    private Double averageDailyWords;

    /**
     * 学习最积极的学生列表（Top 5）
     */
    private List<TopStudent> topStudents;

    /**
     * 学习最积极的学生信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopStudent {
        /**
         * 学生ID
         */
        private Long studentId;

        /**
         * 学生姓名
         */
        private String studentName;

        /**
         * 累计学习单词数
         */
        private Integer totalWords;
    }
}
