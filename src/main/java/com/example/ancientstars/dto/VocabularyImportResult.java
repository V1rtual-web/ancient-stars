package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 词汇导入结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyImportResult {

    private Integer totalCount;

    private Integer successCount;

    private Integer failureCount;

    private List<String> errors = new ArrayList<>();

    public void addError(String error) {
        this.errors.add(error);
    }
}
