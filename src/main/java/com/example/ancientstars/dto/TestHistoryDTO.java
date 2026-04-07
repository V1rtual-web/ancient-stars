package com.example.ancientstars.dto;

import com.example.ancientstars.entity.TestRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测试历史记录DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestHistoryDTO {

    /**
     * 测试记录ID
     */
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务标题
     */
    private String taskTitle;

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
     * 测试时长（秒）
     */
    private Integer durationSeconds;

    /**
     * 题型
     */
    private TestRecord.TestType testType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
