package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 完成复习响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteReviewResponse {

    private String message;
    private LocalDateTime nextReviewTime;

    /**
     * 创建成功响应
     */
    public static CompleteReviewResponse success(LocalDateTime nextReviewTime) {
        return new CompleteReviewResponse("复习完成", nextReviewTime);
    }
}
