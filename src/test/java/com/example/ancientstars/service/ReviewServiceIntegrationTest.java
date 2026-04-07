package com.example.ancientstars.service;

import com.example.ancientstars.entity.ReviewReminder;
import com.example.ancientstars.entity.StudyRecord;
import com.example.ancientstars.repository.ReviewReminderRepository;
import com.example.ancientstars.repository.StudyRecordRepository;
import com.example.ancientstars.scheduler.ReviewScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReviewService 集成测试
 * 测试复习提醒生成流程、复习完成流程和调度器执行
 * 
 * <p>
 * 验证需求：
 * </p>
 * <ul>
 * <li>需求 3.3: 每天定时检查需要复习的词汇</li>
 * <li>需求 3.6: 完成复习后更新学习记录并重新计算下一次复习时间</li>
 * <li>需求 6.2: 查询所有需要在当天复习的学习记录</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ReviewService 集成测试")
class ReviewServiceIntegrationTest {

    @Autowired
    private ReviewReminderService reviewReminderService;

    @Autowired
    private StudyRecordService studyRecordService;

    @Autowired
    private ReviewScheduler reviewScheduler;

    @Autowired
    private StudyRecordRepository studyRecordRepository;

    @Autowired
    private ReviewReminderRepository reviewReminderRepository;

    private Long testStudentId;
    private Long testVocabularyId1;
    private Long testVocabularyId2;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        reviewReminderRepository.deleteAll();
        studyRecordRepository.deleteAll();

        // 初始化测试数据
        testStudentId = 1000L;
        testVocabularyId1 = 2000L;
        testVocabularyId2 = 2001L;
    }

    // ==================== 复习提醒生成流程测试 ====================

    @Test
    @DisplayName("集成测试 - 复习提醒生成流程 - 验证需求 3.3, 6.2")
    void testReminderGenerationFlow() {
        // Given: 创建一个需要复习的学习记录
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        StudyRecord record = createStudyRecord(testStudentId, testVocabularyId1, pastTime);

        // When: 创建复习提醒
        LocalDateTime remindTime = LocalDateTime.now();
        ReviewReminder reminder = reviewReminderService.createReminder(
                testStudentId,
                testVocabularyId1,
                record.getId(),
                remindTime);

        // Then: 验证提醒创建成功
        assertThat(reminder).isNotNull();
        assertThat(reminder.getId()).isNotNull();
        assertThat(reminder.getStudentId()).isEqualTo(testStudentId);
        assertThat(reminder.getVocabularyId()).isEqualTo(testVocabularyId1);
        assertThat(reminder.getStudyRecordId()).isEqualTo(record.getId());
        assertThat(reminder.getStatus()).isEqualTo(ReviewReminder.Status.PENDING);

        // 验证提醒已保存到数据库
        ReviewReminder savedReminder = reviewReminderRepository.findById(reminder.getId()).orElse(null);
        assertThat(savedReminder).isNotNull();
        assertThat(savedReminder.getStatus()).isEqualTo(ReviewReminder.Status.PENDING);
    }

    @Test
    @DisplayName("集成测试 - 批量生成复习提醒 - 验证需求 3.3, 6.2")
    void testBatchReminderGeneration() {
        // Given: 创建多个需要复习的学习记录
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        StudyRecord record1 = createStudyRecord(testStudentId, testVocabularyId1, pastTime);
        StudyRecord record2 = createStudyRecord(testStudentId, testVocabularyId2, pastTime);

        // When: 批量创建复习提醒
        LocalDateTime remindTime = LocalDateTime.now();
        ReviewReminder reminder1 = reviewReminderService.createReminder(
                testStudentId, testVocabularyId1, record1.getId(), remindTime);
        ReviewReminder reminder2 = reviewReminderService.createReminder(
                testStudentId, testVocabularyId2, record2.getId(), remindTime);

        // Then: 验证所有提醒创建成功
        assertThat(reminder1).isNotNull();
        assertThat(reminder2).isNotNull();

        // 验证可以查询到所有待复习提醒
        List<ReviewReminder> reminders = reviewReminderService.getStudentReminders(testStudentId);
        assertThat(reminders).hasSize(2);
        assertThat(reminders).extracting(ReviewReminder::getStatus)
                .containsOnly(ReviewReminder.Status.PENDING);
    }

    @Test
    @DisplayName("集成测试 - 查询待复习列表 - 验证需求 3.3")
    void testGetStudentReminders() {
        // Given: 创建学习记录和复习提醒
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        StudyRecord record = createStudyRecord(testStudentId, testVocabularyId1, pastTime);
        reviewReminderService.createReminder(
                testStudentId, testVocabularyId1, record.getId(), LocalDateTime.now());

        // When: 查询待复习列表
        List<ReviewReminder> reminders = reviewReminderService.getStudentReminders(testStudentId);

        // Then: 验证查询结果
        assertThat(reminders).isNotEmpty();
        assertThat(reminders).hasSize(1);
        assertThat(reminders.get(0).getStudentId()).isEqualTo(testStudentId);
        assertThat(reminders.get(0).getStatus()).isEqualTo(ReviewReminder.Status.PENDING);
    }

    // ==================== 复习完成流程测试 ====================

    @Test
    @DisplayName("集成测试 - 复习完成流程 - 验证需求 3.6")
    void testReviewCompletionFlow() {
        // Given: 创建学习记录和复习提醒
        LocalDateTime pastTime = LocalDateTime.now().minusDays(7);
        StudyRecord record = createStudyRecord(testStudentId, testVocabularyId1, pastTime);
        ReviewReminder reminder = reviewReminderService.createReminder(
                testStudentId, testVocabularyId1, record.getId(), LocalDateTime.now());

        int originalReviewCount = record.getReviewCount();

        // When: 完成复习
        StudyRecord updatedRecord = reviewReminderService.completeReview(reminder.getId());

        // Then: 验证学习记录已更新
        assertThat(updatedRecord).isNotNull();
        assertThat(updatedRecord.getReviewCount()).isEqualTo(originalReviewCount + 1);
        assertThat(updatedRecord.getLastReviewTime()).isNotNull();
        assertThat(updatedRecord.getNextReviewTime()).isNotNull();
        assertThat(updatedRecord.getNextReviewTime()).isAfter(LocalDateTime.now());

        // 验证复习提醒状态已更新
        ReviewReminder completedReminder = reviewReminderRepository.findById(reminder.getId()).orElse(null);
        assertThat(completedReminder).isNotNull();
        assertThat(completedReminder.getStatus()).isEqualTo(ReviewReminder.Status.COMPLETED);

        // 验证学习记录在数据库中已更新
        StudyRecord dbRecord = studyRecordRepository.findById(record.getId()).orElse(null);
        assertThat(dbRecord).isNotNull();
        assertThat(dbRecord.getReviewCount()).isEqualTo(originalReviewCount + 1);
    }

    @Test
    @DisplayName("集成测试 - 多次复习流程 - 验证需求 3.6")
    void testMultipleReviewCompletions() {
        // Given: 创建学习记录
        LocalDateTime pastTime = LocalDateTime.now().minusDays(7);
        StudyRecord record = createStudyRecord(testStudentId, testVocabularyId1, pastTime);

        // When & Then: 完成第一次复习
        ReviewReminder reminder1 = reviewReminderService.createReminder(
                testStudentId, testVocabularyId1, record.getId(), LocalDateTime.now());
        StudyRecord afterFirstReview = reviewReminderService.completeReview(reminder1.getId());
        assertThat(afterFirstReview.getReviewCount()).isEqualTo(1);
        assertThat(afterFirstReview.getMasteryLevel()).isEqualTo(StudyRecord.MasteryLevel.LEARNING);

        // When & Then: 完成第二次复习
        ReviewReminder reminder2 = reviewReminderService.createReminder(
                testStudentId, testVocabularyId1, record.getId(), LocalDateTime.now());
        StudyRecord afterSecondReview = reviewReminderService.completeReview(reminder2.getId());
        assertThat(afterSecondReview.getReviewCount()).isEqualTo(2);
        assertThat(afterSecondReview.getMasteryLevel()).isEqualTo(StudyRecord.MasteryLevel.LEARNING);

        // When & Then: 完成第三次复习
        ReviewReminder reminder3 = reviewReminderService.createReminder(
                testStudentId, testVocabularyId1, record.getId(), LocalDateTime.now());
        StudyRecord afterThirdReview = reviewReminderService.completeReview(reminder3.getId());
        assertThat(afterThirdReview.getReviewCount()).isEqualTo(3);
        assertThat(afterThirdReview.getMasteryLevel()).isEqualTo(StudyRecord.MasteryLevel.FAMILIAR);
    }

    @Test
    @DisplayName("集成测试 - 复习完成后下次复习时间计算 - 验证需求 3.6")
    void testNextReviewTimeCalculation() {
        // Given: 创建学习记录和复习提醒
        LocalDateTime pastTime = LocalDateTime.now().minusDays(7);
        StudyRecord record = createStudyRecord(testStudentId, testVocabularyId1, pastTime);
        ReviewReminder reminder = reviewReminderService.createReminder(
                testStudentId, testVocabularyId1, record.getId(), LocalDateTime.now());

        LocalDateTime beforeCompletion = LocalDateTime.now();

        // When: 完成复习
        StudyRecord updatedRecord = reviewReminderService.completeReview(reminder.getId());

        // Then: 验证下次复习时间已重新计算
        assertThat(updatedRecord.getNextReviewTime()).isNotNull();
        assertThat(updatedRecord.getNextReviewTime()).isAfter(beforeCompletion);

        // 验证复习间隔符合艾宾浩斯曲线（第一次复习后应该是2天）
        LocalDateTime expectedNextReview = beforeCompletion.plusDays(2);
        assertThat(updatedRecord.getNextReviewTime()).isAfterOrEqualTo(expectedNextReview.minusMinutes(1));
        assertThat(updatedRecord.getNextReviewTime()).isBeforeOrEqualTo(expectedNextReview.plusMinutes(1));
    }

    // ==================== 调度器执行测试 ====================

    @Test
    @DisplayName("集成测试 - 调度器执行 - 验证需求 3.3, 6.2")
    void testSchedulerExecution() {
        // Given: 创建需要复习的学习记录（下次复习时间在当前时间之前）
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        createStudyRecord(testStudentId, testVocabularyId1, pastTime);
        createStudyRecord(testStudentId, testVocabularyId2, pastTime);

        // When: 执行调度器
        reviewScheduler.generateDailyReviewReminders();

        // Then: 验证复习提醒已生成
        List<ReviewReminder> reminders = reviewReminderService.getStudentReminders(testStudentId);
        assertThat(reminders).isNotEmpty();
        assertThat(reminders.size()).isGreaterThanOrEqualTo(2);
        assertThat(reminders).allMatch(r -> r.getStatus() == ReviewReminder.Status.PENDING);
    }

    @Test
    @DisplayName("集成测试 - 调度器不生成未到期的复习提醒 - 验证需求 6.2")
    void testSchedulerDoesNotGenerateForFutureReviews() {
        // Given: 创建学习记录，下次复习时间在未来
        LocalDateTime futureTime = LocalDateTime.now().plusDays(7);
        StudyRecord record = new StudyRecord();
        record.setStudentId(testStudentId);
        record.setVocabularyId(testVocabularyId1);
        record.setStudyTime(LocalDateTime.now());
        record.setReviewCount(0);
        record.setNextReviewTime(futureTime);
        record.setMasteryLevel(StudyRecord.MasteryLevel.NEW);
        studyRecordRepository.save(record);

        int reminderCountBefore = reviewReminderRepository.findAll().size();

        // When: 执行调度器
        reviewScheduler.generateDailyReviewReminders();

        // Then: 验证没有生成新的复习提醒
        int reminderCountAfter = reviewReminderRepository.findAll().size();
        assertThat(reminderCountAfter).isEqualTo(reminderCountBefore);
    }

    @Test
    @DisplayName("集成测试 - 调度器处理空数据 - 验证需求 6.2")
    void testSchedulerWithNoRecords() {
        // Given: 没有需要复习的记录
        studyRecordRepository.deleteAll();

        // When: 执行调度器
        reviewScheduler.generateDailyReviewReminders();

        // Then: 调度器应该正常执行（不抛出异常）
        List<ReviewReminder> reminders = reviewReminderRepository.findAll();
        assertThat(reminders).isEmpty();
    }

    // ==================== 完整流程集成测试 ====================

    @Test
    @DisplayName("集成测试 - 完整复习流程 - 从学习到复习完成 - 验证需求 3.3, 3.6, 6.2")
    void testCompleteReviewWorkflow() {
        // Step 1: 学生学习一个新单词
        StudyRecord initialRecord = studyRecordService.createStudyRecord(testStudentId, testVocabularyId1);
        assertThat(initialRecord).isNotNull();
        assertThat(initialRecord.getReviewCount()).isEqualTo(0);
        assertThat(initialRecord.getNextReviewTime()).isNotNull();

        // Step 2: 修改下次复习时间为过去（模拟时间流逝）
        initialRecord.setNextReviewTime(LocalDateTime.now().minusDays(1));
        studyRecordRepository.save(initialRecord);

        // Step 3: 调度器生成复习提醒
        reviewScheduler.generateDailyReviewReminders();

        // Step 4: 学生查询待复习列表
        List<ReviewReminder> reminders = reviewReminderService.getStudentReminders(testStudentId);
        assertThat(reminders).isNotEmpty();
        ReviewReminder reminder = reminders.get(0);
        assertThat(reminder.getVocabularyId()).isEqualTo(testVocabularyId1);

        // Step 5: 学生完成复习
        StudyRecord afterReview = reviewReminderService.completeReview(reminder.getId());
        assertThat(afterReview.getReviewCount()).isEqualTo(1);
        assertThat(afterReview.getLastReviewTime()).isNotNull();
        assertThat(afterReview.getNextReviewTime()).isAfter(LocalDateTime.now());

        // Step 6: 验证复习提醒状态已更新
        ReviewReminder completedReminder = reviewReminderRepository.findById(reminder.getId()).orElse(null);
        assertThat(completedReminder).isNotNull();
        assertThat(completedReminder.getStatus()).isEqualTo(ReviewReminder.Status.COMPLETED);

        // Step 7: 验证待复习列表已更新（不再包含已完成的提醒）
        List<ReviewReminder> remainingReminders = reviewReminderService.getStudentReminders(testStudentId);
        assertThat(remainingReminders).doesNotContain(completedReminder);
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试用的学习记录
     */
    private StudyRecord createStudyRecord(Long studentId, Long vocabularyId, LocalDateTime nextReviewTime) {
        StudyRecord record = new StudyRecord();
        record.setStudentId(studentId);
        record.setVocabularyId(vocabularyId);
        record.setStudyTime(LocalDateTime.now().minusDays(7));
        record.setReviewCount(0);
        record.setLastReviewTime(null);
        record.setNextReviewTime(nextReviewTime);
        record.setMasteryLevel(StudyRecord.MasteryLevel.NEW);
        return studyRecordRepository.save(record);
    }
}
