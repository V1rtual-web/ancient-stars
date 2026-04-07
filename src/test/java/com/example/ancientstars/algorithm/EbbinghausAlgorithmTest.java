package com.example.ancientstars.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 艾宾浩斯遗忘曲线算法测试
 * 验证复习时间计算的正确性
 */
@DisplayName("艾宾浩斯算法测试")
class EbbinghausAlgorithmTest {

    private EbbinghausAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new EbbinghausAlgorithm();
    }

    @Test
    @DisplayName("首次学习后应在7天后复习 - Requirements 3.1, 3.2")
    void testFirstReview() {
        LocalDateTime studyTime = LocalDateTime.of(2026, 4, 1, 10, 0);
        LocalDateTime nextReview = algorithm.calculateNextReviewTime(studyTime, 0);

        LocalDateTime expected = LocalDateTime.of(2026, 4, 8, 10, 0);
        assertEquals(expected, nextReview, "首次学习后应在7天后复习");
    }

    @Test
    @DisplayName("第一次复习后应在2天后复习 - Requirements 3.1, 3.2")
    void testSecondReview() {
        LocalDateTime firstReviewTime = LocalDateTime.of(2026, 4, 8, 10, 0);
        LocalDateTime nextReview = algorithm.calculateNextReviewTime(firstReviewTime, 1);

        LocalDateTime expected = LocalDateTime.of(2026, 4, 10, 10, 0);
        assertEquals(expected, nextReview, "第一次复习后应在2天后复习");
    }

    @Test
    @DisplayName("第二次复习后应在4天后复习 - Requirements 3.1, 3.2")
    void testThirdReview() {
        LocalDateTime secondReviewTime = LocalDateTime.of(2026, 4, 10, 10, 0);
        LocalDateTime nextReview = algorithm.calculateNextReviewTime(secondReviewTime, 2);

        LocalDateTime expected = LocalDateTime.of(2026, 4, 14, 10, 0);
        assertEquals(expected, nextReview, "第二次复习后应在4天后复习");
    }

    @Test
    @DisplayName("第三次复习后应在7天后复习 - Requirements 3.1, 3.2")
    void testFourthReview() {
        LocalDateTime thirdReviewTime = LocalDateTime.of(2026, 4, 14, 10, 0);
        LocalDateTime nextReview = algorithm.calculateNextReviewTime(thirdReviewTime, 3);

        LocalDateTime expected = LocalDateTime.of(2026, 4, 21, 10, 0);
        assertEquals(expected, nextReview, "第三次复习后应在7天后复习");
    }

    @Test
    @DisplayName("第四次复习后应在15天后复习 - Requirements 3.1, 3.2")
    void testFifthReview() {
        LocalDateTime fourthReviewTime = LocalDateTime.of(2026, 4, 21, 10, 0);
        LocalDateTime nextReview = algorithm.calculateNextReviewTime(fourthReviewTime, 4);

        LocalDateTime expected = LocalDateTime.of(2026, 5, 6, 10, 0);
        assertEquals(expected, nextReview, "第四次复习后应在15天后复习");
    }

    @Test
    @DisplayName("第五次及以后复习应在30天后复习 - Requirements 3.1, 3.2")
    void testSubsequentReviews() {
        LocalDateTime fifthReviewTime = LocalDateTime.of(2026, 5, 6, 10, 0);
        LocalDateTime nextReview = algorithm.calculateNextReviewTime(fifthReviewTime, 5);

        LocalDateTime expected = LocalDateTime.of(2026, 6, 5, 10, 0);
        assertEquals(expected, nextReview, "第五次复习后应在30天后复习");

        // 测试第六次
        LocalDateTime sixthReview = algorithm.calculateNextReviewTime(nextReview, 6);
        LocalDateTime expectedSixth = LocalDateTime.of(2026, 7, 5, 10, 0);
        assertEquals(expectedSixth, sixthReview, "第六次复习后应在30天后复习");
    }

    @Test
    @DisplayName("上次复习时间为null时应抛出异常")
    void testNullLastReviewTime() {
        assertThrows(IllegalArgumentException.class,
                () -> algorithm.calculateNextReviewTime(null, 0),
                "上次复习时间为null时应抛出IllegalArgumentException");
    }

    @Test
    @DisplayName("复习次数为负数时应抛出异常")
    void testNegativeReviewCount() {
        LocalDateTime studyTime = LocalDateTime.of(2026, 4, 1, 10, 0);
        assertThrows(IllegalArgumentException.class,
                () -> algorithm.calculateNextReviewTime(studyTime, -1),
                "复习次数为负数时应抛出IllegalArgumentException");
    }

    @Test
    @DisplayName("验证完整的复习周期序列")
    void testCompleteReviewCycle() {
        LocalDateTime initialStudyTime = LocalDateTime.of(2026, 1, 1, 10, 0);
        
        // 首次学习 -> 7天后
        LocalDateTime review1 = algorithm.calculateNextReviewTime(initialStudyTime, 0);
        assertEquals(LocalDateTime.of(2026, 1, 8, 10, 0), review1);
        
        // 第一次复习 -> 2天后
        LocalDateTime review2 = algorithm.calculateNextReviewTime(review1, 1);
        assertEquals(LocalDateTime.of(2026, 1, 10, 10, 0), review2);
        
        // 第二次复习 -> 4天后
        LocalDateTime review3 = algorithm.calculateNextReviewTime(review2, 2);
        assertEquals(LocalDateTime.of(2026, 1, 14, 10, 0), review3);
        
        // 第三次复习 -> 7天后
        LocalDateTime review4 = algorithm.calculateNextReviewTime(review3, 3);
        assertEquals(LocalDateTime.of(2026, 1, 21, 10, 0), review4);
        
        // 第四次复习 -> 15天后
        LocalDateTime review5 = algorithm.calculateNextReviewTime(review4, 4);
        assertEquals(LocalDateTime.of(2026, 2, 5, 10, 0), review5);
        
        // 第五次复习 -> 30天后
        LocalDateTime review6 = algorithm.calculateNextReviewTime(review5, 5);
        assertEquals(LocalDateTime.of(2026, 3, 7, 10, 0), review6);
    }

@Test
    @DisplayName("needsReview方法应正确判断是否需要复习")
    void testNeedsReview() {
        LocalDateTime nextReviewTime = LocalDateTime.of(2026, 4, 8, 10, 0);
        
        // 当前时间在复习时间之前
        LocalDateTime beforeTime = LocalDateTime.of(2026, 4, 7, 10, 0);
        assertFalse(algorithm.needsReview(nextReviewTime, beforeTime), 
            "当前时间在复习时间之前，不需要复习");
        
        // 当前时间等于复习时间
        LocalDateTime equalTime = LocalDateTime.of(2026, 4, 8, 10, 0);
        assertTrue(algorithm.needsReview(nextReviewTime, equalTime), 
            "当前时间等于复习时间，需要复习");
        
        // 当前时间在复习时间之后
        LocalDateTime afterTime = LocalDateTime.of(2026, 4, 9, 10, 0);
        assertTrue(algorithm.needsReview(nextReviewTime, afterTime),
         "当前时间在复习时间之后，需要复习");
    }

    @Test
    @DisplayName("needsReview方法处理null参数")
    void testNeedsReviewWithNull() {
        LocalDateTime time = LocalDateTime.of(2026, 4, 8, 10, 0);
        
        assertFalse(algorithm.needsReview(null, time), 
            "nextReviewTime为null时应返回false");
        assertFalse(algorithm.needsReview(time, null), 
            "currentTime为null时应返回false");
        assertFalse(algorithm.needsReview(null, null), 
            "两个参数都为null时应返回false");
    }

    @Test
    @DisplayName("getDaysUntilReview方法应正确计算距离复习的天数")
    void testGetDaysUntilReview() {
        LocalDateTime nextReviewTime = LocalDateTime.of(2026, 4, 8, 10, 0);
        LocalDateTime currentTime = LocalDateTime.of(2026, 4, 1, 10, 0);
        
        long days = algorithm.getDaysUntilReview(nextReviewTime, currentTime);
        assertEquals(7, days, "应该还有7天到复习时间");
        
        // 测试已过期的情况
        LocalDateTime overdueTime = LocalDateTime.of(2026, 4, 10, 10, 0);
        long overdueDays = algorithm.getDaysUntilReview(nextReviewTime, overdueTime);
        assertEquals(-2, overdueDays, "已过期2天");
    }

    @Test
    @DisplayName("getDaysUntilReview方法处理null参数")
    void testGetDaysUntilReviewWithNull() {
        LocalDateTime time = LocalDateTime.of(2026, 4, 8, 10, 0);
        
        assertEquals(0, algorithm.getDaysUntilReview(null, time), 
            "nextReviewTime为null时应返回0");
        assertEquals(0, algorithm.getDaysUntilReview(time, null), 
            "currentTime为null时应返回0");
        assertEquals(0, algorithm.getDaysUntilReview(null, null), 
            "两个参数都为null时应返回0");
    }
}
