package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 学生学习进度DTO
 * 用于返回学生的学习进度统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressDTO {

    /**
     * 今日新学单词数
     */
    private Integer todayNewWords;

    /**
     * 今日复习单词数
     */
    private Integer todayReviewWords;

    /**
     * 本周新学单词数
     */
    private Integer weekNewWords;

    /**
     * 累计已学单词数
     */
    private Integer totalLearnedWords;

    /**
     * 每日进度详情列表
     */
    private List<DailyProgressDetail> dailyProgress;

    /**
     * 每日进度详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyProgressDetail {
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 新学单词数
         */
        private Integer newWords;

        /**
         * 复习单词数
         */
        private Integer reviewWords;
    }
}
