package com.example.ancientstars.service;

import com.example.ancientstars.common.ErrorCode;
import com.example.ancientstars.config.CacheConfig;
import com.example.ancientstars.dto.VocabularyDTO;
import com.example.ancientstars.dto.VocabularyImportRequest;
import com.example.ancientstars.dto.VocabularyImportResult;
import com.example.ancientstars.entity.Vocabulary;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.repository.VocabularyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 词汇服务层
 */
@Service
@Slf4j
public class VocabularyService {

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private WordListService wordListService;

    /**
     * 创建词汇
     */
    @Transactional
    @CacheEvict(value = CacheConfig.VOCABULARY_CACHE, allEntries = true)
    public VocabularyDTO createVocabulary(VocabularyDTO dto, Long createdBy) {
        if (vocabularyRepository.existsByWordAndIsDeletedFalse(dto.getWord())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "单词已存在: " + dto.getWord());
        }
        Vocabulary vocabulary = new Vocabulary();
        BeanUtils.copyProperties(dto, vocabulary);
        vocabulary.setIsDeleted(false);
        vocabulary.setCreatedBy(createdBy);
        Vocabulary saved = vocabularyRepository.save(vocabulary);
        return convertToDTO(saved);
    }

    /**
     * 更新词汇
     */
    @Transactional
    @CacheEvict(value = CacheConfig.VOCABULARY_CACHE, allEntries = true)
    public VocabularyDTO updateVocabulary(Long id, VocabularyDTO dto) {
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "词汇不存在"));
        if (!vocabulary.getWord().equals(dto.getWord())
                && vocabularyRepository.existsByWordAndIsDeletedFalse(dto.getWord())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "单词已存在: " + dto.getWord());
        }
        BeanUtils.copyProperties(dto, vocabulary, "id", "createdAt", "isDeleted", "createdBy");
        Vocabulary updated = vocabularyRepository.save(vocabulary);
        return convertToDTO(updated);
    }

    /**
     * 删除词汇（软删除）
     */
    @Transactional
    public void deleteVocabulary(Long id) {
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "词汇不存在"));

        vocabularyRepository.delete(vocabulary); // 触发软删除
        log.info("词汇已软删除: id={}, word={}", id, vocabulary.getWord());
    }

    /**
     * 根据ID查询词汇
     */
    @Cacheable(value = CacheConfig.VOCABULARY_CACHE, key = "'vocab:' + #id")
    public VocabularyDTO getVocabularyById(Long id) {
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "词汇不存在"));
        return convertToDTO(vocabulary);
    }

    /**
     * 分页查询所有词汇
     */
    @Cacheable(value = CacheConfig.VOCABULARY_CACHE, key = "'all:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<VocabularyDTO> getAllVocabularies(Pageable pageable) {
        return vocabularyRepository.findByIsDeletedFalse(pageable).map(this::convertToDTO);
    }

    /**
     * 搜索词汇（模糊查询）
     */
    public Page<VocabularyDTO> searchVocabularies(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllVocabularies(pageable);
        }
        return vocabularyRepository.searchByKeyword(keyword.trim(), pageable).map(this::convertToDTO);
    }

    /**
     * 根据难度级别查询
     */
    public List<VocabularyDTO> getVocabulariesByDifficulty(Integer difficultyLevel) {
        return vocabularyRepository.findByDifficultyLevelAndIsDeletedFalse(difficultyLevel)
                .stream().map(this::convertToDTO).toList();
    }

    /**
     * 批量导入词汇
     */
    @Transactional
    public VocabularyImportResult importVocabularies(VocabularyImportRequest request, Long createdBy) {
        VocabularyImportResult result = new VocabularyImportResult();
        result.setTotalCount(request.getVocabularies().size());
        result.setSuccessCount(0);
        result.setFailureCount(0);

        for (int i = 0; i < request.getVocabularies().size(); i++) {
            VocabularyDTO dto = request.getVocabularies().get(i);
            try {
                if (vocabularyRepository.existsByWordAndIsDeletedFalse(dto.getWord())) {
                    result.addError("第" + (i + 1) + "行: 单词已存在 - " + dto.getWord());
                    result.setFailureCount(result.getFailureCount() + 1);
                    continue;
                }

                Vocabulary vocabulary = new Vocabulary();
                BeanUtils.copyProperties(dto, vocabulary);
                vocabulary.setIsDeleted(false);
                vocabulary.setCreatedBy(createdBy);
                Vocabulary saved = vocabularyRepository.save(vocabulary);

                if (request.getWordListId() != null) {
                    wordListService.addVocabularyToWordList(request.getWordListId(), saved.getId());
                }

                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.addError("第" + (i + 1) + "行: " + e.getMessage());
                result.setFailureCount(result.getFailureCount() + 1);
                log.error("导入词汇失败: {}", dto.getWord(), e);
            }
        }

        return result;
    }

    /**
     * 从CSV文件批量导入词汇（优化版：批量查重+批量插入）
     */
    @Transactional
    public VocabularyImportResult importFromCSV(InputStream inputStream, Long wordListId, Long createdBy)
            throws IOException {
        List<Vocabulary> toSave = new ArrayList<>();
        VocabularyImportResult result = new VocabularyImportResult();
        int totalCount = 0;
        int skipCount = 0;

        // 先加载所有已存在的单词到内存，避免逐条查库
        java.util.Set<String> existingWords = new java.util.HashSet<>(
                vocabularyRepository.findAllWords());

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty())
                    continue;

                // 处理CSV中含逗号的字段（被引号包裹）
                String[] fields = parseCsvLine(line);
                if (fields.length < 1 || fields[0].trim().isEmpty())
                    continue;

                totalCount++;
                String word = fields[0].trim();

                if (existingWords.contains(word.toLowerCase())) {
                    skipCount++;
                    continue;
                }

                Vocabulary v = new Vocabulary();
                v.setWord(word);
                v.setPhonetic(fields.length > 1 ? truncate(fields[1].trim(), 100) : null);
                v.setDefinition(fields.length > 2 ? fields[2].trim() : null);
                v.setTranslation(fields.length > 3 ? fields[3].trim() : null);
                v.setExample(fields.length > 4 ? fields[4].trim() : null);
                v.setDifficultyLevel(fields.length > 5 && !fields[5].trim().isEmpty()
                        ? parseIntSafe(fields[5].trim())
                        : 2);
                v.setIsDeleted(false);
                v.setCreatedBy(createdBy);

                toSave.add(v);
                existingWords.add(word.toLowerCase()); // 防止同文件内重复
            }
        }

        // 分批插入，每批500条
        int batchSize = 500;
        int successCount = 0;
        for (int i = 0; i < toSave.size(); i += batchSize) {
            List<Vocabulary> batch = toSave.subList(i, Math.min(i + batchSize, toSave.size()));
            try {
                vocabularyRepository.saveAll(batch);
                successCount += batch.size();
                log.info("导入进度: {}/{}", successCount, toSave.size());
            } catch (Exception e) {
                log.error("批次导入失败 [{}-{}]: {}", i, i + batchSize, e.getMessage());
                result.addError("批次 " + (i / batchSize + 1) + " 导入失败: " + e.getMessage());
                result.setFailureCount(result.getFailureCount() + batch.size());
            }
        }

        result.setTotalCount(totalCount);
        result.setSuccessCount(successCount);
        result.setFailureCount(result.getFailureCount() + (toSave.size() - successCount));
        if (skipCount > 0) {
            result.addError("跳过已存在单词: " + skipCount + " 条");
        }

        log.info("CSV导入完成: 总计={}, 成功={}, 跳过={}", totalCount, successCount, skipCount);
        return result;
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) : s;
    }

    private Integer parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 2;
        }
    }

    /**
     * 实体转DTO
     */
    private VocabularyDTO convertToDTO(Vocabulary vocabulary) {
        VocabularyDTO dto = new VocabularyDTO();
        BeanUtils.copyProperties(vocabulary, dto);
        return dto;
    }
}
