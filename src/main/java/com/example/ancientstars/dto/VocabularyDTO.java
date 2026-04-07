package com.example.ancientstars.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 词汇DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyDTO {

    private Long id;

    @NotBlank(message = "单词不能为空")
    @Size(max = 100, message = "单词长度不能超过100")
    private String word;

    @Size(max = 100, message = "音标长度不能超过100")
    private String phonetic;

    private String definition;

    private String translation;

    private String example;

    private Integer difficultyLevel;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
