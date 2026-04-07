package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成绩分布DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreDistributionDTO {

    /**
     * 分数段名称
     */
    private String range;

    /**
     * 该分数段的人数
     */
    private Long count;
}
