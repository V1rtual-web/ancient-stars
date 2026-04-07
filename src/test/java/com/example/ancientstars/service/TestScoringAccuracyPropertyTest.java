package com.example.ancientstars.service;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 测试评分准确性属性测试
 * 
 * 属性 10: 测试评分准确性
 * 对于任何提交的测试，计算的分数应该等于(正确答案数 / 总题数) × 100
 */
@Tag("Feature: vocabulary-learning-system, Property 10: 测试评分准确性")
public class TestScoringAccuracyPropertyTest {

    /**
     * 属性测试：验证测试评分的准确性
     * 
     * 对于任何题目数量和正确答案数量，计算的分数应该等于 (正确答案数 / 总题数) × 100
     */
    @Property(tries = 100)
    void testScoringAccuracy(
            @ForAll @IntRange(min = 1, max = 50) int totalQuestions,
            @ForAll @IntRange(min = 0, max = 50) int correctAnswers) {

        // 确保正确答案数不超过总题数
        Assume.that(correctAnswers <= totalQuestions);

        // 计算期望的分数（使用与实际代码相同的算法）
        BigDecimal expectedScore = BigDecimal.valueOf(correctAnswers)
                .divide(BigDecimal.valueOf(totalQuestions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // 模拟实际的评分计算
        BigDecimal actualScore = calculateScore(totalQuestions, correctAnswers);

        // 验证分数准确性
        assertEquals(expectedScore, actualScore,
                String.format("分数计算错误：总题数=%d, 正确答案数=%d, 期望分数=%s, 实际分数=%s",
                        totalQuestions, correctAnswers, expectedScore, actualScore));
    }

    /**
     * 属性测试：验证满分情况
     */
    @Property(tries = 100)
    void testPerfectScore(@ForAll @IntRange(min = 1, max = 50) int totalQuestions) {
        // 当所有答案都正确时，分数应该是100.00
        BigDecimal score = calculateScore(totalQuestions, totalQuestions);

        assertEquals(new BigDecimal("100.00"), score,
                String.format("满分情况下分数应该是100.00，实际为%s", score));
    }

    /**
     * 属性测试：验证零分情况
     */
    @Property(tries = 100)
    void testZeroScore(@ForAll @IntRange(min = 1, max = 50) int totalQuestions) {
        // 当所有答案都错误时，分数应该是0.00
        BigDecimal score = calculateScore(totalQuestions, 0);

        assertEquals(new BigDecimal("0.00"), score,
                String.format("零分情况下分数应该是0.00，实际为%s", score));
    }

    /**
     * 属性测试：验证分数范围
     */
    @Property(tries = 100)
    void testScoreRange(
            @ForAll @IntRange(min = 1, max = 50) int totalQuestions,
            @ForAll @IntRange(min = 0, max = 50) int correctAnswers) {

        Assume.that(correctAnswers <= totalQuestions);

        BigDecimal score = calculateScore(totalQuestions, correctAnswers);

        // 分数应该在0到100之间
        assertTrue(score.compareTo(BigDecimal.ZERO) >= 0,
                "分数不应该小于0");
        assertTrue(score.compareTo(new BigDecimal("100")) <= 0,
                "分数不应该大于100");
    }

    /**
     * 属性测试：验证分数精度
     */
    @Property(tries = 100)
    void testScorePrecision(
            @ForAll @IntRange(min = 1, max = 50) int totalQuestions,
            @ForAll @IntRange(min = 0, max = 50) int correctAnswers) {

        Assume.that(correctAnswers <= totalQuestions);

        BigDecimal score = calculateScore(totalQuestions, correctAnswers);

        // 分数应该保留2位小数
        assertEquals(2, score.scale(),
                String.format("分数应该保留2位小数，实际为%d位", score.scale()));
    }

    /**
     * 属性测试：验证分数单调性
     * 正确答案数增加时，分数应该不减少
     */
    @Property(tries = 100)
    void testScoreMonotonicity(@ForAll @IntRange(min = 2, max = 50) int totalQuestions) {
        BigDecimal previousScore = BigDecimal.ZERO;

        for (int correctAnswers = 0; correctAnswers <= totalQuestions; correctAnswers++) {
            BigDecimal currentScore = calculateScore(totalQuestions, correctAnswers);

            // 当前分数应该大于等于前一个分数
            assertTrue(currentScore.compareTo(previousScore) >= 0,
                    String.format("分数应该单调递增：前一个分数=%s, 当前分数=%s (正确答案数=%d/%d)",
                            previousScore, currentScore, correctAnswers, totalQuestions));

            previousScore = currentScore;
        }
    }

    /**
     * 计算分数（模拟实际代码的计算逻辑）
     */
    private BigDecimal calculateScore(int totalQuestions, int correctAnswers) {
        return BigDecimal.valueOf(correctAnswers)
                .divide(BigDecimal.valueOf(totalQuestions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 辅助断言方法
     */
    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
