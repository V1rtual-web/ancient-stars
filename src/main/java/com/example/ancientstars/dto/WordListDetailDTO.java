package com.example.ancientstars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 词汇表详情DTO（包含词汇列表）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordListDetailDTO {

    private WordListDTO wordList;

    private List<VocabularyDTO> vocabularies;
}
