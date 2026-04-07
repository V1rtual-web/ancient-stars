package com.example.ancientstars.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 词汇批量导入请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyImportRequest {

    @NotNull(message = "词汇列表不能为空")
    private List<VocabularyDTO> vocabularies;

    private Long wordListId;
}
