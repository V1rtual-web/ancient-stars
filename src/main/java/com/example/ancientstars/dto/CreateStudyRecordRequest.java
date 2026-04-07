package com.example.ancientstars.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建学习记录请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudyRecordRequest {

    @NotNull(message = "词汇ID不能为空")
    private Long vocabularyId;
}
