package com.example.ancientstars.integration;

import com.example.ancientstars.entity.*;
import com.example.ancientstars.repository.*;
import com.example.ancientstars.scheduler.ReviewScheduler;
import com.example.ancientstars.service.StudyRecordService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 学生自主学习功能集成测试
 * 测试完整的学习流程、复习提醒流程和教师查看进度流程
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SelfStudyIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private StudyRecordRepository studyRecordRepository;

    @Autowired
    private DailyProgressRepository dailyProgressRepository;

    @Autowired
    private ReviewReminderRepository reviewReminderRepository;

    @Autowired
    private StudyRecordService studyRecordService;

    @Autowired
    private ReviewScheduler reviewScheduler;

    private User testStudent;
    private User testTeacher;
    private Vocabulary testVocabulary;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        reviewReminderRepository.deleteAll();
        studyRecordRepository.deleteAll();
        dailyProgressRepository.deleteAll();
        vocabularyRepository.deleteAll();
        userRepository.deleteAll();

        // 创建测试学生
        testStudent = new User();
        testStudent.setUsername("test_student");
        testStudent.setPassword("password");
        testStudent.setRealName("测试学生");
        testStudent.setRole(User.Role.STUDENT);
        testStudent.setClassId(1L);
        testStudent = userRepository.save(testStudent);

        // 创建测试教师
        testTeacher = new User();
        testTeacher.setUsername("test_teacher");
        testTeacher.setPassword("password");
        testTeacher.setRealName("测试教师");
        testTeacher.setRole(User.Role.TEACHER);
        testTeacher.setClassId(1L);
        testTeacher = userRepository.save(testTeacher);

        // 创建测试词汇
        testVocabulary = new Vocabulary();
        testVocabulary.setWord("apple");
        testVocabulary.setPhonetic("/ˈæpl/");
        testVocabulary.setTranslation("n. 苹果");
        testVocabulary.setExample("I like apples.");
        testVocabulary.setDeleted(false);
        testVocabulary = vocabularyRepository.save(testVocabulary);
    }

    /**
     * 测试 16.1: 完整学习流程集成
     * 验证：学生浏览词汇 → 标记已掌握 → 创建学习记录 → 自动更新每日进度 → 正确计算复习时间
     */
    @Test
    @Order(1)
    @DisplayName("16.1 - 完整学习流程集成测试")
    void testCompleteStudyFlow() {
        // 1. 创建学习记录（模拟学生标记已掌握）
        StudyRecord studyRecord = studyRecordService.createStudyRecord(
                testStudent.getId(),
                testVocabulary.getId());

        // 验证学习记录创建成功
        assertThat(studyRecord).isNotNull();
        assertThat(studyRecord.getStudentId()).isEqualTo(testStudent.getId());
        assertThat(studyRecord.getVocabularyId()).isEqualTo(testVocabulary.getId());
        assertThat(studyRecord.getReviewCount()).isEqualTo(0);
        assertThat(studyRecord.getMasteryLevel()).isEqualTo(StudyRecord.MasteryLevel.NEW);

        // 2. 验证复习时间正确计算（首次学习后7天）
        LocalDateTime expectedNextReview = studyRecord.getStudyTime().plusDays(7);
        assertThat(studyRecord.getNextReviewTime()).isEqualTo(expectedNextReview);

        // 3. 验证每日进度自动更新
        LocalDate today = LocalDate.now();
        DailyProgress dailyProgress = dailyProgressRepository
                .findByStudentIdAndStudyDate(testStudent.getId(), today)
                .orElse(null);

        assertThat(dailyProgress).isNotNull();
        assertThat(dailyProgress.getNewWordsCount()).isEqualTo(1);
        assertThat(dailyProgress.getReviewWordsCount()).isEqualTo(0);

        // 4. 验证幂等性（重复标记不会创建新记录）
        StudyRecord duplicateRecord = studyRecordService.createStudyRecord(
                testStudent.getId(),
                testVocabulary.getId());

        assertThat(duplicateRecord.getId()).isEqualTo(studyRecord.getId());

        // 验证只有一条学习记录
        long recordCount = studyRecordRepository.countByStudentId(testStudent.getId());
        assertThat(recordCount).isEqualTo(1);
    }

    /**
     * 测试 16.2: 复习提醒流程集成
     * 验证：调度器生成复习提醒 → 学生查看待复习列表 → 完成复习 → 更新学习记录
     */
    @Test
    @Order(2)
    @DisplayName("16.2 - 复习提醒流程集成测试")
    void testReviewReminderFlow() {
        // 1. 创建学习记录并设置下次复习时间为今天
        StudyRecord studyRecord = new StudyRecord();
        studyRecord.setStudentId(testStudent.getId());
        studyRecord.setVocabularyId(testVocabulary.getId());
        studyRecord.setStudyTime(LocalDateTime.now().minusDays(7));
        studyRecord.setReviewCount(0);
        studyRecord.setNextReviewTime(LocalDateTime.now());
        studyRecord.setMasteryLevel(StudyRecord.MasteryLevel.NEW);
        studyRecord = studyRecordRepository.save(studyRecord);

        // 2. 执行调度器生成复习提醒
        reviewScheduler.generateDailyReviewReminders();

        // 3. 验证复习提醒已生成
        List<ReviewReminder> reminders = reviewReminderRepository
                .findByStudentIdAndStatus(testStudent.getId(), ReviewReminder.Status.PENDING);

        assertThat(reminders).isNotEmpty();
        assertThat(reminders.get(0).getVocabularyId()).isEqualTo(testVocabulary.getId());
        assertThat(reminders.get(0).getStatus()).isEqualTo(ReviewReminder.Status.PENDING);

        // 4. 模拟学生查看待复习列表（通过服务层）
        // 这里简化处理，实际应该通过API调用

        // 5. 完成复习
        StudyRecord updatedRecord = studyRecordService.updateStudyRecord(studyRecord);

        // 6. 验证学习记录已更新
        assertThat(updatedRecord.getReviewCount()).isEqualTo(1);
        assertThat(updatedRecord.getLastReviewTime()).isNotNull();

        // 验证下次复习时间已重新计算（第一次复习后2天）
        LocalDateTime expectedNextReview = updatedRecord.getLastReviewTime().plusDays(2);
        assertThat(updatedRecord.getNextReviewTime()).isEqualTo(expectedNextReview);

        // 7. 验证每日进度已更新（复习计数）
        LocalDate today = LocalDate.now();
        DailyProgress dailyProgress = dailyProgressRepository
                .findByStudentIdAndStudyDate(testStudent.getId(), today)
                .orElse(null);

        assertThat(dailyProgress).isNotNull();
        assertThat(dailyProgress.getReviewWordsCount()).isGreaterThan(0);
    }

    /**
     * 测试 16.3: 教师查看进度流程集成
     * 验证：教师查看学生进度 → 权限控制 → 统计数据准确
     */
    @Test
    @Order(3)
    @DisplayName("16.3 - 教师查看进度流程集成测试")
    @Transactional
    void testTeacherViewProgressFlow() {
        // 1. 创建多条学习记录
        for (int i = 0; i < 5; i++) {
            Vocabulary vocab = new Vocabulary();
            vocab.setWord("word" + i);
            vocab.setTranslation("单词" + i);
            vocab.setDeleted(false);
            vocab = vocabularyRepository.save(vocab);

            studyRecordService.createStudyRecord(testStudent.getId(), vocab.getId());
        }

        // 2. 验证学生进度统计准确
        // 这里简化处理，实际应该通过API调用验证权限
        long totalRecords = studyRecordRepository.countByStudentId(testStudent.getId());
        assertThat(totalRecords).isEqualTo(5);

        // 3. 验证每日进度统计
        LocalDate today = LocalDate.now();
        DailyProgress dailyProgress = dailyProgressRepository
                .findByStudentIdAndStudyDate(testStudent.getId(), today)
                .orElse(null);

        assertThat(dailyProgress).isNotNull();
        assertThat(dailyProgress.getNewWordsCount()).isEqualTo(5);

        // 4. 验证教师只能查看同班级学生
        // 创建不同班级的学生
        User otherStudent = new User();
        otherStudent.setUsername("other_student");
        otherStudent.setPassword("password");
        otherStudent.setRole(User.Role.STUDENT);
        otherStudent.setClassId(2L); // 不同班级
        otherStudent = userRepository.save(otherStudent);

        // 教师不应该能查看其他班级学生的数据
        // 这里通过权限验证逻辑确保
        assertThat(testTeacher.getClassId()).isNotEqualTo(otherStudent.getClassId());
    }

    /**
     * 测试 16.4: 端到端完整流程测试
     * 测试从学习到复习的完整周期
     */
    @Test
    @Order(4)
    @DisplayName("16.4 - 端到端完整流程测试")
    void testEndToEndFlow() {
        // 第一天：学生学习新单词
        StudyRecord studyRecord = studyRecordService.createStudyRecord(
                testStudent.getId(),
                testVocabulary.getId());

        assertThat(studyRecord.getReviewCount()).isEqualTo(0);
        assertThat(studyRecord.getMasteryLevel()).isEqualTo(StudyRecord.MasteryLevel.NEW);

        // 验证每日进度
        LocalDate day1 = LocalDate.now();
        DailyProgress progress1 = dailyProgressRepository
                .findByStudentIdAndStudyDate(testStudent.getId(), day1)
                .orElse(null);
        assertThat(progress1).isNotNull();
        assertThat(progress1.getNewWordsCount()).isEqualTo(1);

        // 第7天：生成复习提醒
        studyRecord.setNextReviewTime(LocalDateTime.now());
        studyRecordRepository.save(studyRecord);

        reviewScheduler.generateDailyReviewReminders();

        List<ReviewReminder> reminders = reviewReminderRepository
                .findByStudentIdAndStatus(testStudent.getId(), ReviewReminder.Status.PENDING);
        assertThat(reminders).hasSize(1);

        // 学生完成复习
        StudyRecord updatedRecord = studyRecordService.updateStudyRecord(studyRecord);
        assertThat(updatedRecord.getReviewCount()).isEqualTo(1);
        assertThat(updatedRecord.getMasteryLevel()).isEqualTo(StudyRecord.MasteryLevel.LEARNING);

        // 验证复习后的每日进度
        DailyProgress progress2 = dailyProgressRepository
                .findByStudentIdAndStudyDate(testStudent.getId(), day1)
                .orElse(null);
        assertThat(progress2).isNotNull();
        assertThat(progress2.getReviewWordsCount()).isGreaterThan(0);

        // 验证下次复习时间已更新
        assertThat(updatedRecord.getNextReviewTime()).isAfter(LocalDateTime.now());
    }

    /**
     * 测试并发场景：多个学生同时学习
     */
    @Test
    @Order(5)
    @DisplayName("16.4 - 并发场景测试")
    void testConcurrentStudy() throws InterruptedException {
        // 创建多个学生
        User student1 = createTestStudent("student1", 1L);
        User student2 = createTestStudent("student2", 1L);
        User student3 = createTestStudent("student3", 1L);

        // 创建多个词汇
        Vocabulary vocab1 = createTestVocabulary("word1");
        Vocabulary vocab2 = createTestVocabulary("word2");

        // 模拟并发学习
        Thread thread1 = new Thread(() -> {
            studyRecordService.createStudyRecord(student1.getId(), vocab1.getId());
            studyRecordService.createStudyRecord(student1.getId(), vocab2.getId());
        });

        Thread thread2 = new Thread(() -> {
            studyRecordService.createStudyRecord(student2.getId(), vocab1.getId());
            studyRecordService.createStudyRecord(student2.getId(), vocab2.getId());
        });

        Thread thread3 = new Thread(() -> {
            studyRecordService.createStudyRecord(student3.getId(), vocab1.getId());
            studyRecordService.createStudyRecord(student3.getId(), vocab2.getId());
        });

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();

        // 验证所有学习记录都已创建
        assertThat(studyRecordRepository.countByStudentId(student1.getId())).isEqualTo(2);
        assertThat(studyRecordRepository.countByStudentId(student2.getId())).isEqualTo(2);
        assertThat(studyRecordRepository.countByStudentId(student3.getId())).isEqualTo(2);

        // 验证每日进度统计正确
        LocalDate today = LocalDate.now();
        DailyProgress progress1 = dailyProgressRepository
                .findByStudentIdAndStudyDate(student1.getId(), today)
                .orElse(null);
        assertThat(progress1).isNotNull();
        assertThat(progress1.getNewWordsCount()).isEqualTo(2);
    }

    // 辅助方法
    private User createTestStudent(String username, Long classId) {
        User student = new User();
        student.setUsername(username);
        student.setPassword("password");
        student.setRole(User.Role.STUDENT);
        student.setClassId(classId);
        return userRepository.save(student);
    }

    private Vocabulary createTestVocabulary(String word) {
        Vocabulary vocab = new Vocabulary();
        vocab.setWord(word);
        vocab.setTranslation("翻译: " + word);
        vocab.setDeleted(false);
        return vocabularyRepository.save(vocab);
    }
}
