package com.example.ancientstars.service;

import com.example.ancientstars.dto.LearningRecordDTO;
import com.example.ancientstars.entity.LearningRecord;
import com.example.ancientstars.entity.LearningRecord.MasteryLevel;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.entity.Vocabulary;
import com.example.ancientstars.repository.LearningRecordRepository;
import com.example.ancientstars.repository.TaskRepository;
import com.example.ancientstars.repository.UserRepository;
import com.example.ancientstars.repository.VocabularyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 学习记录服务层
 */
@Service
public class LearningRecordService {

    @Autowired
    private LearningRecordRepository learningRecordRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 标记词汇掌握度（幂等操作）
     * 如果记录已存在则更新，否则创建新记录
     */
    @Transactional
    public LearningRecord markMastery(Long studentId, Long vocabularyId, Long taskId, MasteryLevel masteryLevel) {
        // 验证学生存在
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        // 验证词汇存在
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new RuntimeException("Vocabulary not found with id: " + vocabularyId));

        // 如果指定了任务ID，验证任务存在
        if (taskId != null) {
            taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        }

        // 查找现有记录（幂等性保证）
        Optional<LearningRecord> existingRecord = learningRecordRepository
                .findByStudentIdAndVocabularyIdAndTaskId(studentId, vocabularyId, taskId);

        LearningRecord record;
        if (existingRecord.isPresent()) {
            // 更新现有记录
            record = existingRecord.get();
            record.setMasteryLevel(masteryLevel);
            record.setReviewCount(record.getReviewCount() + 1);
            record.setLastReviewedAt(LocalDateTime.now());
        } else {
            // 创建新记录
            record = new LearningRecord();
            record.setStudentId(studentId);
            record.setVocabularyId(vocabularyId);
            record.setTaskId(taskId);
            record.setMasteryLevel(masteryLevel);
            record.setReviewCount(1);
            record.setLastReviewedAt(LocalDateTime.now());
        }

        return learningRecordRepository.save(record);
    }

    /**
     * 获取学生的学习记录（分页）
     */
    public Page<LearningRecordDTO> getStudentLearningRecords(Long studentId, Pageable pageable) {
        Page<LearningRecord> records = learningRecordRepository.findByStudentId(studentId, pageable);
        return records.map(this::convertToDTO);
    }

    /**
     * 获取学生在特定任务中的学习记录
     */
    public List<LearningRecordDTO> getStudentTaskRecords(Long studentId, Long taskId) {
        List<LearningRecord> records = learningRecordRepository.findByStudentIdAndTaskId(studentId, taskId);
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取学生特定掌握程度的词汇
     */
    public List<LearningRecordDTO> getStudentRecordsByMastery(Long studentId, MasteryLevel masteryLevel) {
        List<LearningRecord> records = learningRecordRepository
                .findByStudentIdAndMasteryLevel(studentId, masteryLevel);
        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 统计学生已掌握的词汇数量
     */
    public long countMasteredVocabulary(Long studentId) {
        return learningRecordRepository.countByStudentIdAndMasteryLevel(studentId, MasteryLevel.MASTERED);
    }

    /**
     * 统计学生在特定任务中已掌握的词汇数量
     */
    public long countTaskMasteredVocabulary(Long studentId, Long taskId) {
        return learningRecordRepository.countByStudentIdAndTaskIdAndMasteryLevel(
                studentId, taskId, MasteryLevel.MASTERED);
    }

    /**
     * 获取学习记录详情
     */
    public LearningRecordDTO getLearningRecordById(Long id) {
        LearningRecord record = learningRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning record not found with id: " + id));
        return convertToDTO(record);
    }

    /**
     * 删除学习记录
     */
    @Transactional
    public void deleteLearningRecord(Long id) {
        learningRecordRepository.deleteById(id);
    }

    /**
     * 转换实体为DTO
     */
    private LearningRecordDTO convertToDTO(LearningRecord record) {
        LearningRecordDTO dto = new LearningRecordDTO();
        dto.setId(record.getId());
        dto.setStudentId(record.getStudentId());
        dto.setVocabularyId(record.getVocabularyId());
        dto.setTaskId(record.getTaskId());
        dto.setMasteryLevel(record.getMasteryLevel());
        dto.setReviewCount(record.getReviewCount());
        dto.setLastReviewedAt(record.getLastReviewedAt());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());

        // 填充扩展字段
        vocabularyRepository.findById(record.getVocabularyId()).ifPresent(vocab -> {
            dto.setWord(vocab.getWord());
            dto.setTranslation(vocab.getTranslation());
            dto.setPhonetic(vocab.getPhonetic());
        });

        if (record.getTaskId() != null) {
            taskRepository.findById(record.getTaskId()).ifPresent(task -> {
                dto.setTaskTitle(task.getTitle());
            });
        }

        return dto;
    }
}
