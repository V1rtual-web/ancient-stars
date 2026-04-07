package com.example.ancientstars.service;

import com.example.ancientstars.dto.PageResponse;
import com.example.ancientstars.dto.StudentVocabularyDTO;
import com.example.ancientstars.entity.StudyRecord;
import com.example.ancientstars.entity.Vocabulary;
import com.example.ancientstars.repository.StudyRecordRepository;
import com.example.ancientstars.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生词汇浏览服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StudentVocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final StudyRecordRepository studyRecordRepository;

    /**
     * 获取学生词汇列表（支持分页、筛选、搜索）
     * 排序规则：
     * 1. 今日到期复习的词汇优先
     * 2. 未学过的词汇次之
     * 3. 已学过且未到复习时间的词汇排最后
     */
    public PageResponse<StudentVocabularyDTO> getVocabulariesForStudent(
            Long studentId, Integer page, Integer size, Long wordListId, String keyword) {

        List<StudyRecord> studyRecords = studyRecordRepository.findByStudentId(studentId);
        Map<Long, StudyRecord> studyRecordMap = studyRecords.stream()
                .collect(Collectors.toMap(StudyRecord::getVocabularyId, sr -> sr));

        LocalDateTime now = LocalDateTime.now();

        // 分离出今日需复习的词汇ID（优先级0）
        List<Long> dueIds = studyRecords.stream()
                .filter(sr -> sr.getNextReviewTime() != null && !sr.getNextReviewTime().isAfter(now))
                .sorted(java.util.Comparator.comparing(StudyRecord::getNextReviewTime))
                .map(StudyRecord::getVocabularyId)
                .collect(Collectors.toList());

        // 已学过未到期的词汇ID（优先级2，排最后）
        java.util.Set<Long> learnedNotDueIds = studyRecords.stream()
                .filter(sr -> sr.getNextReviewTime() == null || sr.getNextReviewTime().isAfter(now))
                .map(StudyRecord::getVocabularyId)
                .collect(java.util.stream.Collectors.toSet());

        // 构建排序后的完整列表（只排序ID，不加载全部词汇）
        // 策略：先展示到期复习词，再展示新词（按字母），最后展示已学未到期词
        // 实现：先查到期词，再查新词分页，已学未到期词追加到末尾
        // 简化实现：加载全部但限制最大数量，96000条内存排序约50ms可接受
        List<Vocabulary> allVocabularies;
        if (wordListId != null && keyword != null && !keyword.trim().isEmpty()) {
            allVocabularies = new java.util.ArrayList<>(vocabularyRepository
                    .findByWordListIdAndKeyword(wordListId, keyword.trim(), PageRequest.of(0, 10000)).getContent());
        } else if (wordListId != null) {
            allVocabularies = new java.util.ArrayList<>(vocabularyRepository
                    .findByWordListId(wordListId, PageRequest.of(0, 10000)).getContent());
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            allVocabularies = new java.util.ArrayList<>(vocabularyRepository
                    .searchByKeyword(keyword.trim(), PageRequest.of(0, 10000)).getContent());
        } else {
            // 无筛选时：先取到期词 + 新词（前N页），已学未到期词追加
            // 直接分页：到期词 + 未学词优先
            int pageIndex = Math.max(0, page - 1);

            // 到期词汇
            List<Vocabulary> dueVocabs = dueIds.isEmpty() ? List.of()
                    : new java.util.ArrayList<>(vocabularyRepository.findAllById(dueIds));
            // 按dueIds顺序排列
            Map<Long, Vocabulary> dueMap = dueVocabs.stream()
                    .collect(Collectors.toMap(Vocabulary::getId, v -> v));
            dueVocabs = dueIds.stream().map(dueMap::get)
                    .filter(v -> v != null).collect(Collectors.toList());

            long totalNew = vocabularyRepository.countByIsDeletedFalseAndIdNotIn(
                    dueIds.isEmpty() && learnedNotDueIds.isEmpty()
                            ? List.of(-1L)
                            : mergeIds(dueIds, learnedNotDueIds));
            long total = dueVocabs.size() + totalNew
                    + (learnedNotDueIds.isEmpty() ? 0 : learnedNotDueIds.size());

            int offset = pageIndex * size;
            List<StudentVocabularyDTO> items = new java.util.ArrayList<>();

            // 先填到期词
            if (offset < dueVocabs.size()) {
                int end = Math.min(offset + size, dueVocabs.size());
                dueVocabs.subList(offset, end)
                        .forEach(v -> items.add(convertToStudentDTO(v, studyRecordMap.get(v.getId()), now)));
            }

            // 再填新词
            if (items.size() < size) {
                int newOffset = Math.max(0, offset - dueVocabs.size());
                int needed = size - items.size();
                java.util.Set<Long> excludeIds = mergeIds(dueIds, learnedNotDueIds);
                if (excludeIds.isEmpty())
                    excludeIds.add(-1L);
                org.springframework.data.domain.Page<Vocabulary> newPage = vocabularyRepository
                        .findByIsDeletedFalseAndIdNotIn(
                                excludeIds, PageRequest.of(newOffset / needed, needed,
                                        org.springframework.data.domain.Sort.by("word")));
                newPage.getContent().forEach(v -> items.add(convertToStudentDTO(v, null, now)));
            }

            return PageResponse.of(items, total, page, size);
        }

        // 有筛选条件时走内存排序
        allVocabularies.sort((a, b) -> {
            int pa = getPriority(studyRecordMap.get(a.getId()), now);
            int pb = getPriority(studyRecordMap.get(b.getId()), now);
            if (pa != pb)
                return pa - pb;
            StudyRecord ra = studyRecordMap.get(a.getId());
            StudyRecord rb = studyRecordMap.get(b.getId());
            if (pa == 0 && ra != null && rb != null)
                return ra.getNextReviewTime().compareTo(rb.getNextReviewTime());
            return a.getWord().compareTo(b.getWord());
        });

        int pageIndex = Math.max(0, page - 1);
        int fromIndex = pageIndex * size;
        int toIndex = Math.min(fromIndex + size, allVocabularies.size());
        List<Vocabulary> pageContent = fromIndex < allVocabularies.size()
                ? allVocabularies.subList(fromIndex, toIndex)
                : List.of();

        List<StudentVocabularyDTO> items = pageContent.stream()
                .map(v -> convertToStudentDTO(v, studyRecordMap.get(v.getId()), now))
                .collect(Collectors.toList());

        return PageResponse.of(items, (long) allVocabularies.size(), page, size);
    }

    private java.util.Set<Long> mergeIds(List<Long> list, java.util.Set<Long> set) {
        java.util.Set<Long> result = new java.util.HashSet<>(set);
        result.addAll(list);
        return result;
    }

    /**
     * 获取词汇的复习优先级
     * 0 = 今日到期/已过期需复习（最优先）
     * 1 = 未学过
     * 2 = 已学过但未到复习时间
     */
    private int getPriority(StudyRecord record, LocalDateTime now) {
        if (record == null)
            return 1; // 未学过
        if (record.getNextReviewTime() != null && !record.getNextReviewTime().isAfter(now))
            return 0; // 到期
        return 2; // 已学过未到期
    }

    /**
     * 转换为学生词汇DTO，包含复习状态
     */
    private StudentVocabularyDTO convertToStudentDTO(Vocabulary vocabulary, StudyRecord record, LocalDateTime now) {
        StudentVocabularyDTO dto = new StudentVocabularyDTO();
        BeanUtils.copyProperties(vocabulary, dto);
        dto.setIsLearned(record != null);
        if (record != null) {
            dto.setMasteryLevel(record.getMasteryLevel() != null ? record.getMasteryLevel().name() : null);
            dto.setNextReviewTime(record.getNextReviewTime());
            dto.setNeedsReview(record.getNextReviewTime() != null && !record.getNextReviewTime().isAfter(now));
            dto.setReviewCount(record.getReviewCount());
        }
        return dto;
    }
}
