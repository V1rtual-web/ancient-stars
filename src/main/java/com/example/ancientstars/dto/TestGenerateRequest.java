package com.example.ancientstars.dto;

import com.example.ancientstars.entity.TestRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试生成请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestGenerateRequest {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 题型
     */
    private TestRecord.TestType testType;
}
