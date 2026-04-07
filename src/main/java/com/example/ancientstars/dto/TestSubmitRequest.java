package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 测试提交请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestSubmitRequest {

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 答案列表
     */
    private List<AnswerDTO> answers;

    /**
     * 测试时长（秒）
     */
    private Integer durationSeconds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDTO {
        /**
         * 词汇ID
         */
        private Long vocabularyId;

        /**
         * 学生答案
         */
        private String answer;
    }
}
