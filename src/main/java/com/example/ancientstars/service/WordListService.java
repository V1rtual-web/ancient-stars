package com.example.ancientstars.service;

import com.example.ancientstars.common.ErrorCode;
import com.example.ancientstars.dto.VocabularyDTO;
import com.example.ancientstars.dto.WordListDTO;
import com.example.ancientstars.dto.WordListDetailDTO;
import com.example.ancientstars.entity.Vocabulary;
import com.example.ancientstars.entity.WordList;
import com.example.ancientstars.entity.WordListVocabulary;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.repository.VocabularyRepository;
import com.example.ancientstars.repository.WordListRepository;
import com.example.ancientstars.repository.WordListVocabularyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 词汇表服务层
 */
@Service
@Slf4j
public class WordListService {

    @Autowired
    private WordListRepository wordListRepository;

    @Autowired
    private WordListVocabularyRepository wordListVocabularyRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    /**
     * 创建词汇表
     */
    @Transactional
    public WordListDTO createWordList(WordListDTO dto, Long creatorId) {
        if (wordListRepository.existsByNameAndCreatorId(dto.getName(), creatorId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "词汇表名称已存在");
        }

        WordList wordList = new WordList();
        BeanUtils.copyProperties(dto, wordList);
        wordList.setCreatorId(creatorId);
        wordList.setWordCount(0);

        WordList saved = wordListRepository.save(wordList);
        return convertToDTO(saved);
    }

    /**
     * 更新词汇表
     */
    @Transactional
    public WordListDTO updateWordList(Long id, WordListDTO dto, Long userId) {
        WordList wordList = wordListRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "词汇表不存在"));

        // 检查权限
        if (!wordList.getCreatorId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限修改此词汇表");
        }

        // 检查名称是否重复（排除自己）
        if (!wordList.getName().equals(dto.getName())
                && wordListRepository.existsByNameAndCreatorId(dto.getName(), userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "词汇表名称已存在");
        }

        BeanUtils.copyProperties(dto, wordList, "id", "creatorId", "wordCount", "createdAt");
        WordList updated = wordListRepository.save(wordList);
        return convertToDTO(updated);
    }

    /**
     * 删除词汇表
     */
    @Transactional
    public void deleteWordList(Long id, Long userId) {
        WordList wordList = wordListRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "词汇表不存在"));

        // 检查权限
        if (!wordList.getCreatorId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限删除此词汇表");
        }

        // 删除词汇表及其关联
        wordListVocabularyRepository.deleteByWordListId(id);
        wordListRepository.delete(wordList);
        log.info("词汇表已删除: id={}, name={}", id, wordList.getName());
    }

    /**
     * 根据ID查询词汇表
     */
    public WordListDTO getWordListById(Long id) {
        WordList wordList = wordListRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "词汇表不存在"));
        return convertToDTO(wordList);
    }

    /**
     * 查询词汇表详情（包含词汇列表）
     */
    public WordListDetailDTO getWordListDetail(Long id) {
        WordList wordList = wordListRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "词汇表不存在"));

        List<Long> vocabularyIds = wordListVocabularyRepository.findVocabularyIdsByWordListId(id);
        List<Vocabulary> vocabularies = vocabularyRepository.findAllById(vocabularyIds);

        WordListDetailDTO detail = new WordListDetailDTO();
        detail.setWordList(convertToDTO(wordList));
        detail.setVocabularies(vocabularies.stream().map(this::convertVocabularyToDTO).toList());

        return detail;
    }

    /**
     * 分页查询所有词汇表
     */
    public Page<WordListDTO> getAllWordLists(Pageable pageable) {
        return wordListRepository.findAll(pageable).map(this::convertToDTO);
    }

/**
     * 根据创建者查询词汇表
     */
    public Page<WordListDTO> getWordListsByCreator(Long creatorId, Pageable pageable) {
        return wordListRepository.findByCreatorId(creatorId, pageable).map(this::convertToDTO);
    }

    /**
     * 搜索词汇表
     */
    public Page<WordListDTO> searchWordLists(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllWordLists(pageable);
        }
        return wordListRepository.findByNameContaining(keyword.trim(), pageable).map(this::convertToDTO);
    }

    /**
     * 添加词汇到词汇表
     */
    @Transactional
    public void addVocabularyToWordList(Long wordListId, Long vocabularyId) {
        // 检查词汇表是否存在
        WordList wordList = wordListRepository.findById(wordListId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "词汇表不存在"));

        // 检查词汇是否存在
        if (!vocabularyRepository.existsById(vocabularyId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "词汇不存在");
        }

        // 检查是否已添加
        if (wordListVocabularyRepository.existsByWordListIdAndVocabularyId(wordListId, vocabularyId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "词汇已在词汇表中");
        }

        // 创建关联
        WordListVocabulary wlv = new WordListVocabulary();
        wlv.setWordListId(wordListId);
        wlv.setVocabularyId(vocabularyId);
        wlv.setSortOrder(wordList.getWordCount());
        wordListVocabularyRepository.save(wlv);

        // 更新词汇表的词汇数量
        wordList.setWordCount(wordList.getWordCount() + 1);
        wordListRepository.save(wordList);

        log.info("词汇已添加到词汇表: wordListId={}, vocabularyId={}", wordListId, vocabularyId);
    }

    /**
     * 从词汇表移除词汇
     */
    @Transactional
    public void removeVocabularyFromWordList(Long wordListId, Long vocabularyId) {
        // 检查词汇表是否存在
        WordList wordList = wordListRepository.findById(wordListId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "词汇表不存在"));

        // 检查关联是否存在
        if (!wordListVocabularyRepository.existsByWordListIdAndVocabularyId(wordListId, vocabularyId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "词汇不在词汇表中");
        }

        // 删除关联
        wordListVocabularyRepository.deleteByWordListIdAndVocabularyId(wordListId, vocabularyId);

        // 更新词汇表的词汇数量
        wordList.setWordCount(Math.max(0, wordList.getWordCount() - 1));
        wordListRepository.save(wordList);

        log.info("词汇已从词汇表移除: wordListId={}, vocabularyId={}", wordListId, vocabularyId);
    }

    /**
     * 批量添加词汇到词汇表
     */
    @Transactional
    public void addVocabulariesToWordList(Long wordListId, List<Long> vocabularyIds) {
        for (Long vocabularyId : vocabularyIds) {
            try {
                addVocabularyToWordList(wordListId, vocabularyId);
            } catch (BusinessException e) {
                log.warn("添加词汇失败: vocabularyId={}, error={}", vocabularyId, e.getMessage());
            }
        }
    }

    /**
     * 实体转DTO
     */
    private WordListDTO convertToDTO(WordList wordList) {
        WordListDTO dto = new WordListDTO();
        BeanUtils.copyProperties(wordList, dto);
        return dto;
    }

    /**
     * 词汇实体转DTO
     */
    private VocabularyDTO convertVocabularyToDTO(Vocabulary vocabulary) {
        VocabularyDTO dto = new VocabularyDTO();
        BeanUtils.copyProperties(vocabulary, dto);
        return dto;
    }
}
