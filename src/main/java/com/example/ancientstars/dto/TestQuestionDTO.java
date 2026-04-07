package com.example.ancientstars.dto;

import com.example.ancientstars.entity.TestRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 测试题目DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestionDTO {

    /**
     * 题目ID（词汇ID）
     */
    private Long vocabularyId;

    /**
     * 题型
     */
    private TestRecord.TestType questionType;

    /**
     * 题目文本
     */
    private String questionText;

    /**
     * 选项（仅选择题有）
     */
    private List<String> options;

    /**
     * 正确答案（用于后端验证，不返回给前端）
     */
    private String correctAnswer;
}
