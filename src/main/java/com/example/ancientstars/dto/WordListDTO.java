package com.example.ancientstars.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 词汇表DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordListDTO {

    private Long id;

    @NotBlank(message = "词汇表名称不能为空")
    @Size(max = 100, message = "名称长度不能超过100")
    private String name;

    private String description;

    private Long creatorId;

    private Integer wordCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
