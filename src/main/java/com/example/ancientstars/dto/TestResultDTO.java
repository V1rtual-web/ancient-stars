package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 测试结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResultDTO {

    /**
     * 测试记录ID
     */
    private Long testRecordId;

    /**
     * 分数
     */
    private BigDecimal score;

    /**
     * 总题数
     */
    private Integer totalQuestions;

    /**
     * 正确答案数
     */
    private Integer correctAnswers;

    /**
     * 正确率（百分比）
     */
    private BigDecimal accuracy;

    /**
     * 错题列表
     */
    private List<WrongQuestionDTO> wrongQuestions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WrongQuestionDTO {
        /**
         * 词汇ID
         */
        private Long vocabularyId;

        /**
         * 单词
         */
        private String word;

        /**
         * 题目文本
         */
        private String questionText;

        /**
         * 学生答案
         */
        private String studentAnswer;

        /**
         * 正确答案
         */
        private String correctAnswer;
    }
}
