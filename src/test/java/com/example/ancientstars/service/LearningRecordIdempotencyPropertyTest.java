package com.example.ancientstars.service;

import com.example.ancientstars.entity.LearningRecord;
import com.example.ancientstars.entity.LearningRecord.MasteryLevel;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.entity.Vocabulary;
import com.example.ancientstars.repository.LearningRecordRepository;
import com.example.ancientstars.repository.TaskRepository;
import com.example.ancientstars.repository.UserRepository;
import com.example.ancientstars.repository.VocabularyRepository;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 学习记录幂等性属性测试
 * 属性 9: 学习记录幂等性
 * 验证需求: 5.3
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("Feature: vocabulary-learning-system, Property 9: 学习记录幂等性")
class LearningRecordIdempotencyPropertyTest {

    @Autowired
    private LearningRecordService learningRecordService;

    @Autowired
    private LearningRecordRepository learningRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * 属性 9: 对于任何词汇，同一学生对同一任务中的同一词汇多次标记掌握，应该只更新现有记录而不创建重复记录
     */
    @Property(tries = 100)
    @Label("属性9: 学习记录幂等性 - 多次标记同一词汇应该只更新现有记录")
    @Transactional
    void multipleMarksShouldUpdateSameRecord(
            @ForAll("masteryLevels") MasteryLevel masteryLevel,
            @ForAll("markCounts") int markCount) {

        // 准备测试数据
        User student = createTestStudent();
        Vocabulary vocabulary = createTestVocabulary();
        Long taskId = 1L;

        // 多次标记同一词汇
        LearningRecord firstRecord = null;
        for (int i = 0; i < markCount; i++) {
            LearningRecord record = learningRecordService.markMastery(
                    student.getId(),
                    vocabulary.getId(),
                    taskId,
                    masteryLevel);

            if (firstRecord == null) {
                firstRecord = record;
            }

            // 验证返回的记录ID始终相同（幂等性）
            assertEquals(firstRecord.getId(), record.getId(),
                    "多次标记应该返回相同的记录ID");
        }

        // 验证数据库中只有一条记录
        List<LearningRecord> records = learningRecordRepository
                .findByStudentIdAndTaskId(student.getId(), taskId);

        assertEquals(1, records.size(),
                "数据库中应该只有一条学习记录，不应该创建重复记录");

        // 验证记录的复习次数正确
        LearningRecord finalRecord = records.get(0);
        assertEquals(markCount, finalRecord.getReviewCount(),
                "复习次数应该等于标记次数");

        // 验证掌握程度是最后一次标记的值
        assertEquals(masteryLevel, finalRecord.getMasteryLevel(),
                "掌握程度应该是最后一次标记的值");

        // 清理测试数据
        learningRecordRepository.deleteById(finalRecord.getId());
        vocabularyRepository.deleteById(vocabulary.getId());
        userRepository.deleteById(student.getId());
    }

    /**
     * 属性 9: 不同学生标记同一词汇应该创建不同的记录
     */
    @Property(tries = 100)
    @Label("属性9: 学习记录幂等性 - 不同学生标记同一词汇应该创建不同记录")
    @Transactional
    void differentStudentsShouldHaveSeparateRecords(
            @ForAll("masteryLevels") MasteryLevel masteryLevel,
            @ForAll("studentCounts") int studentCount) {

        // 准备测试数据
        Vocabulary vocabulary = createTestVocabulary();
        Long taskId = 1L;

        // 创建多个学生并标记同一词汇
        for (int i = 0; i < studentCount; i++) {
            User student = createTestStudent();
            learningRecordService.markMastery(
                    student.getId(),
                    vocabulary.getId(),
                    taskId,
                    masteryLevel);
        }

        // 验证数据库中有对应数量的记录
        List<LearningRecord> records = learningRecordRepository.findByVocabularyId(vocabulary.getId());
        assertEquals(studentCount, records.size(),
                "不同学生应该有各自独立的学习记录");

        // 清理测试数据
        for (LearningRecord record : records) {
            learningRecordRepository.deleteById(record.getId());
            userRepository.deleteById(record.getStudentId());
        }
        vocabularyRepository.deleteById(vocabulary.getId());
    }

    /**
     * 属性 9: 同一学生标记不同词汇应该创建不同的记录
     */
    @Property(tries = 100)
    @Label("属性9: 学习记录幂等性 - 同一学生标记不同词汇应该创建不同记录")
    @Transactional
    void sameStudentDifferentVocabularyShouldHaveSeparateRecords(
            @ForAll("masteryLevels") MasteryLevel masteryLevel,
            @ForAll("vocabularyCounts") int vocabularyCount) {

        // 准备测试数据
        User student = createTestStudent();
        Long taskId = 1L;

        // 创建多个词汇并标记
        for (int i = 0; i < vocabularyCount; i++) {
            Vocabulary vocabulary = createTestVocabulary();
            learningRecordService.markMastery(
                    student.getId(),
                    vocabulary.getId(),
                    taskId,
                    masteryLevel);
        }

        // 验证数据库中有对应数量的记录
        List<LearningRecord> records = learningRecordRepository.findByStudentIdAndTaskId(student.getId(), taskId);
        assertEquals(vocabularyCount, records.size(),
                "同一学生标记不同词汇应该有各自独立的学习记录");

        // 清理测试数据
        for (LearningRecord record : records) {
            learningRecordRepository.deleteById(record.getId());
            vocabularyRepository.deleteById(record.getVocabularyId());
        }
        userRepository.deleteById(student.getId());
    }

    // ==================== 数据生成器 ====================

    @Provide
    Arbitrary<MasteryLevel> masteryLevels() {
        return Arbitraries.of(MasteryLevel.values());
    }

    @Provide
    Arbitrary<Integer> markCounts() {
        return Arbitraries.integers().between(1, 10);
    }

    @Provide
    Arbitrary<Integer> studentCounts() {
        return Arbitraries.integers().between(1, 5);
    }

    @Provide
    Arbitrary<Integer> vocabularyCounts() {
        return Arbitraries.integers().between(1, 5);
    }

    // ==================== 辅助方法 ====================

    private User createTestStudent() {
        User student = new User();
        student.setUsername("student_" + System.nanoTime());
        student.setPassword("password123");
        student.setRealName("Test Student");
        student.setEmail("student@test.com");
        student.setRole(User.Role.STUDENT);
        student.setStatus(User.Status.ACTIVE);
        return userRepository.save(student);
    }

    private Vocabulary createTestVocabulary() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setWord("word_" + System.nanoTime());
        vocabulary.setPhonetic("/test/");
        vocabulary.setDefinition("Test definition");
        vocabulary.setTranslation("测试翻译");
        vocabulary.setExample("Test example");
        vocabulary.setDifficultyLevel(1);
        vocabulary.setDeleted(false);
        vocabulary.setCreatedAt(LocalDateTime.now());
        vocabulary.setUpdatedAt(LocalDateTime.now());
        return vocabularyRepository.save(vocabulary);
    }
}
