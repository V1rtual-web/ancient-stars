package com.example.ancientstars.algorithm;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 艾宾浩斯遗忘曲线算法
 * 用于计算单词复习的最佳时间间隔
 */
@Component
public class EbbinghausAlgorithm {

    /**
     * 计算下次复习时间
     * 
     * @param lastReviewTime 上次复习时间（首次学习时传入学习时间）
     * @param reviewCount    已复习次数（首次学习时为0）
     * @return 下次复习时间
     */
    public LocalDateTime calculateNextReviewTime(LocalDateTime lastReviewTime, int reviewCount) {
        if (lastReviewTime == null) {
            throw new IllegalArgumentException("上次复习时间不能为空");
        }

        if (reviewCount < 0) {
            throw new IllegalArgumentException("复习次数不能为负数");
        }

        int daysToAdd = getDaysInterval(reviewCount);
        return lastReviewTime.plusDays(daysToAdd);
    }

    /**
     * 根据复习次数获取复习间隔天数
     * 基于艾宾浩斯遗忘曲线的时间间隔设计
     * 
     * @param reviewCount 复习次数
     * @return 间隔天数
     */
    private int getDaysInterval(int reviewCount) {
        switch (reviewCount) {
            case 0: // 第一次学习后
                return 7; // 7天后复习（根据需求要求）
            case 1: // 第一次复习后
                return 2; // 2天后复习
            case 2: // 第二次复习后
                return 4; // 4天后复习
            case 3: // 第三次复习后
                return 7; // 7天后复习
            case 4: // 第四次复习后
                return 15; // 15天后复习
            default: // 第五次及以后
                return 30; // 30天后复习
        }
    }

    /**
     * 判断是否需要复习
     * 
     * @param nextReviewTime 下次复习时间
     * @param currentTime    当前时间
     * @return 是否需要复习
     */
    public boolean needsReview(LocalDateTime nextReviewTime, LocalDateTime currentTime) {
        if (nextReviewTime == null || currentTime == null) {
            return false;
        }
        return currentTime.isAfter(nextReviewTime) || currentTime.isEqual(nextReviewTime);
    }

    /**
     * 计算距离下次复习的天数
     * 
     * @param nextReviewTime 下次复习时间
     * @param currentTime    当前时间
     * @return 距离复习的天数（负数表示已过期）
     */
    public long getDaysUntilReview(LocalDateTime nextReviewTime, LocalDateTime currentTime) {
        if (nextReviewTime == null || currentTime == null) {
            return 0;
        }
        return java.time.Duration.between(currentTime, nextReviewTime).toDays();
    }
}