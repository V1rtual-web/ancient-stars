package com.example.ancientstars.service;

import com.example.ancientstars.algorithm.EbbinghausAlgorithm;
import com.example.ancientstars.entity.StudyRecord;
import com.example.ancientstars.repository.StudyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 学习记录服务
 * 负责管理学生自主背单词的学习记录，包括创建、更新和复习时间计算
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudyRecordService {

    private final StudyRecordRepository studyRecordRepository;
    private final DailyProgressService dailyProgressService;
    private final EbbinghausAlgorithm ebbinghausAlgorithm;

    /**
     * 创建学习记录
     * 实现幂等性：如果学生已经学习过该词汇，则更新现有记录而不是创建新记录
     * 
     * @param studentId    学生ID
     * @param vocabularyId 词汇ID
     * @return 学习记录
     */
    @Transactional
    public StudyRecord createStudyRecord(Long studentId, Long vocabularyId) {
        log.info("创建学习记录 - 学生ID: {}, 词汇ID: {}", studentId, vocabularyId);

        // 检查是否已存在记录（幂等性检查）
        Optional<StudyRecord> existingRecord = studyRecordRepository
                .findByStudentIdAndVocabularyId(studentId, vocabularyId);

        if (existingRecord.isPresent()) {
            log.info("学习记录已存在，更新现有记录 - 记录ID: {}", existingRecord.get().getId());
            return updateStudyRecord(existingRecord.get());
        }

        // 创建新记录
        StudyRecord record = new StudyRecord();
        record.setStudentId(studentId);
        record.setVocabularyId(vocabularyId);
        record.setStudyTime(LocalDateTime.now());
        record.setReviewCount(0);
        record.setLastReviewTime(null);

        // 使用艾宾浩斯算法计算下次复习时间
        LocalDateTime nextReviewTime = ebbinghausAlgorithm.calculateNextReviewTime(
                record.getStudyTime(), 0);
        record.setNextReviewTime(nextReviewTime);
        record.setMasteryLevel(StudyRecord.MasteryLevel.NEW);

        StudyRecord savedRecord = studyRecordRepository.save(record);
        log.info("学习记录创建成功 - 记录ID: {}, 下次复习时间: {}",
                savedRecord.getId(), nextReviewTime);

        // 更新每日进度（新学单词）
        dailyProgressService.updateDailyProgress(studentId, LocalDate.now(), true);

        return savedRecord;
    }

    /**
     * 更新学习记录
     * 用于学生完成复习时更新复习次数和时间
     * 
     * @param record 学习记录
     * @return 更新后的学习记录
     */
    @Transactional
    public StudyRecord updateStudyRecord(StudyRecord record) {
        log.info("更新学习记录 - 记录ID: {}", record.getId());

        // 更新复习次数
        int newReviewCount = record.getReviewCount() + 1;
        record.setReviewCount(newReviewCount);

        // 更新最后复习时间
        LocalDateTime now = LocalDateTime.now();
        record.setLastReviewTime(now);

        // 使用艾宾浩斯算法计算下次复习时间
        LocalDateTime nextReviewTime = ebbinghausAlgorithm.calculateNextReviewTime(
                now, newReviewCount);
        record.setNextReviewTime(nextReviewTime);

        // 更新掌握程度
        record.setMasteryLevel(calculateMasteryLevel(newReviewCount));

        StudyRecord updatedRecord = studyRecordRepository.save(record);
        log.info("学习记录更新成功 - 记录ID: {}, 复习次数: {}, 下次复习时间: {}",
                updatedRecord.getId(), newReviewCount, nextReviewTime);

        // 更新每日进度（复习单词）
        dailyProgressService.updateDailyProgress(record.getStudentId(), LocalDate.now(), false);

        return updatedRecord;
    }

    /**
     * 根据复习次数计算掌握程度
     * 
     * @param reviewCount 复习次数
     * @return 掌握程度
     */
    private StudyRecord.MasteryLevel calculateMasteryLevel(int reviewCount) {
        if (reviewCount == 0) {
            return StudyRecord.MasteryLevel.NEW;
        } else if (reviewCount <= 2) {
            return StudyRecord.MasteryLevel.LEARNING;
        } else if (reviewCount <= 4) {
            return StudyRecord.MasteryLevel.FAMILIAR;
        } else {
            return StudyRecord.MasteryLevel.MASTERED;
        }
    }
}
