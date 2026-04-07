package com.example.ancientstars.dto;

import com.example.ancientstars.entity.LearningRecord.MasteryLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标记词汇掌握度请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkMasteryRequest {

    private Long studentId;
    private Long vocabularyId;
    private Long taskId;
    private MasteryLevel masteryLevel;
}
