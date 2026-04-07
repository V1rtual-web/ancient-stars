package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务进度更新请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressUpdateRequest {

    private Long studentId;
    private Integer progress;
}
