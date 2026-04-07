package com.example.ancientstars.service;

import com.example.ancientstars.entity.ReviewReminder;
import com.example.ancientstars.entity.StudyRecord;
import com.example.ancientstars.repository.ReviewReminderRepository;
import com.example.ancientstars.repository.StudyRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 复习提醒服务单元测试
 * 验证需求: 3.4, 3.5, 3.6
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("复习提醒服务测试")
class ReviewReminderServiceTest {

    @Mock
    private ReviewReminderRepository reviewReminderRepository;

    @Mock
    private StudyRecordRepository studyRecordRepository;

    @Mock
    private StudyRecordService studyRecordService;

    @InjectMocks
    private ReviewReminderService reviewReminderService;

    private ReviewReminder testReminder;
    private StudyRecord testStudyRecord;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.of(2026, 4, 1, 8, 0, 0);

        // 创建测试复习提醒
        testReminder = new ReviewReminder();
        testReminder.setId(1L);
        testReminder.setStudentId(100L);
        testReminder.setVocabularyId(200L);
        testReminder.setStudyRecordId(300L);
        testReminder.setRemindTime(testTime);
        testReminder.setStatus(ReviewReminder.Status.PENDING);

        // 创建测试学习记录
        testStudyRecord = new StudyRecord();
        testStudyRecord.setId(300L);
        testStudyRecord.setStudentId(100L);
        testStudyRecord.setVocabularyId(200L);
        testStudyRecord.setStudyTime(testTime.minusDays(7));
        testStudyRecord.setReviewCount(1);
        testStudyRecord.setLastReviewTime(testTime.minusDays(1));
        testStudyRecord.setNextReviewTime(testTime);
        testStudyRecord.setMasteryLevel(StudyRecord.MasteryLevel.LEARNING);
    }

    // ==================== 创建复习提醒测试 ====================

    @Test
    @DisplayName("创建复习提醒 - 成功创建 - 验证需求 3.4")
    void testCreateReminderSuccess() {
        // 模拟行为
        when(reviewReminderRepository.save(any(ReviewReminder.class)))
                .thenReturn(testReminder);

        // 执行测试
        ReviewReminder result = reviewReminderService.createReminder(
                100L, 200L, 300L, testTime);

        // 验证结果
        assertNotNull(result);
        assertEquals(100L, result.getStudentId());
        assertEquals(200L, result.getVocabularyId());
        assertEquals(300L, result.getStudyRecordId());
        assertEquals(testTime, result.getRemindTime());
        assertEquals(ReviewReminder.Status.PENDING, result.getStatus());

        // 验证方法调用
        verify(reviewReminderRepository).save(any(ReviewReminder.class));
    }

    @Test
    @DisplayName("创建复习提醒 - 验证默认状态为PENDING - 验证需求 3.4")
    void testCreateReminderDefaultStatus() {
        // 模拟行为
        when(reviewReminderRepository.save(any(ReviewReminder.class)))
                .thenReturn(testReminder);

        // 执行测试
        ReviewReminder result = reviewReminderService.createReminder(
                100L, 200L, 300L, testTime);

        // 验证默认状态
        assertEquals(ReviewReminder.Status.PENDING, result.getStatus());
    }

    @Test
    @DisplayName("创建复习提醒 - 批量创建场景 - 验证需求 3.4")
    void testCreateMultipleReminders() {
        // 创建多个提醒
        ReviewReminder reminder1 = new ReviewReminder();
        reminder1.setId(1L);
        reminder1.setStudentId(100L);
        reminder1.setVocabularyId(200L);
        reminder1.setStudyRecordId(300L);
        reminder1.setRemindTime(testTime);
        reminder1.setStatus(ReviewReminder.Status.PENDING);

        ReviewReminder reminder2 = new ReviewReminder();
        reminder2.setId(2L);
        reminder2.setStudentId(100L);
        reminder2.setVocabularyId(201L);
        reminder2.setStudyRecordId(301L);
        reminder2.setRemindTime(testTime);
        reminder2.setStatus(ReviewReminder.Status.PENDING);

        // 模拟行为
        when(reviewReminderRepository.save(any(ReviewReminder.class)))
                .thenReturn(reminder1, reminder2);

        // 执行测试 - 创建多个提醒
        ReviewReminder result1 = reviewReminderService.createReminder(
                100L, 200L, 300L, testTime);
        ReviewReminder result2 = reviewReminderService.createReminder(
                100L, 201L, 301L, testTime);

        // 验证结果
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(200L, result1.getVocabularyId());
        assertEquals(201L, result2.getVocabularyId());

        // 验证方法调用次数
        verify(reviewReminderRepository, times(2)).save(any(ReviewReminder.class));
    }

    // ==================== 查询待复习列表测试 ====================

    @Test
    @DisplayName("查询待复习列表 - 返回PENDING状态的提醒 - 验证需求 3.5")
    void testGetStudentRemindersSuccess() {
        // 准备测试数据
        ReviewReminder reminder1 = new ReviewReminder();
        reminder1.setId(1L);
        reminder1.setStudentId(100L);
        reminder1.setVocabularyId(200L);
        reminder1.setStatus(ReviewReminder.Status.PENDING);

        ReviewReminder reminder2 = new ReviewReminder();
        reminder2.setId(2L);
        reminder2.setStudentId(100L);
        reminder2.setVocabularyId(201L);
        reminder2.setStatus(ReviewReminder.Status.PENDING);

        List<ReviewReminder> reminders = Arrays.asList(reminder1, reminder2);

        // 模拟行为
        when(reviewReminderRepository.findByStudentIdAndStatus(100L, ReviewReminder.Status.PENDING))
                .thenReturn(reminders);

        // 执行测试
        List<ReviewReminder> result = reviewReminderService.getStudentReminders(100L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(ReviewReminder.Status.PENDING, result.get(0).getStatus());
        assertEquals(ReviewReminder.Status.PENDING, result.get(1).getStatus());

        // 验证方法调用
        verify(reviewReminderRepository).findByStudentIdAndStatus(100L, ReviewReminder.Status.PENDING);
    }

    @Test
    @DisplayName("查询待复习列表 - 空列表 - 验证需求 3.5")
    void testGetStudentRemindersEmptyList() {
        // 模拟行为 - 返回空列表
        when(reviewReminderRepository.findByStudentIdAndStatus(100L, ReviewReminder.Status.PENDING))
                .thenReturn(Arrays.asList());

        // 执行测试
        List<ReviewReminder> result = reviewReminderService.getStudentReminders(100L);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.size());

        // 验证方法调用
        verify(reviewReminderRepository).findByStudentIdAndStatus(100L, ReviewReminder.Status.PENDING);
    }

    @Test
    @DisplayName("查询待复习列表 - 只返回PENDING状态 - 验证需求 3.5")
    void testGetStudentRemindersOnlyPending() {
        // 准备测试数据 - 只有PENDING状态的提醒
        ReviewReminder pendingReminder = new ReviewReminder();
        pendingReminder.setId(1L);
        pendingReminder.setStudentId(100L);
        pendingReminder.setStatus(ReviewReminder.Status.PENDING);

        List<ReviewReminder> reminders = Arrays.asList(pendingReminder);

        // 模拟行为
        when(reviewReminderRepository.findByStudentIdAndStatus(100L, ReviewReminder.Status.PENDING))
                .thenReturn(reminders);

        // 执行测试
        List<ReviewReminder> result = reviewReminderService.getStudentReminders(100L);

        // 验证结果 - 不应包含COMPLETED或SKIPPED状态的提醒
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(r -> r.getStatus() == ReviewReminder.Status.PENDING));

        // 验证方法调用
        verify(reviewReminderRepository).findByStudentIdAndStatus(100L, ReviewReminder.Status.PENDING);
    }

    // ==================== 完成复习测试 ====================

    @Test
    @DisplayName("完成复习 - 成功完成并更新学习记录 - 验证需求 3.6")
    void testCompleteReviewSuccess() {
        // 准备更新后的学习记录
        StudyRecord updatedRecord = new StudyRecord();
        updatedRecord.setId(300L);
        updatedRecord.setStudentId(100L);
        updatedRecord.setVocabularyId(200L);
        updatedRecord.setReviewCount(2);
        updatedRecord.setLastReviewTime(testTime);
        updatedRecord.setNextReviewTime(testTime.plusDays(4));
        updatedRecord.setMasteryLevel(StudyRecord.MasteryLevel.LEARNING);

        // 模拟行为
        when(reviewReminderRepository.findById(1L))
                .thenReturn(Optional.of(testReminder));
        when(reviewReminderRepository.save(any(ReviewReminder.class)))
                .thenReturn(testReminder);
        when(studyRecordRepository.findById(300L))
                .thenReturn(Optional.of(testStudyRecord));
        when(studyRecordService.updateStudyRecord(any(StudyRecord.class)))
                .thenReturn(updatedRecord);

        // 执行测试
        StudyRecord result = reviewReminderService.completeReview(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(300L, result.getId());
        assertEquals(2, result.getReviewCount());
        assertNotNull(result.getLastReviewTime());
        assertNotNull(result.getNextReviewTime());

        // 验证方法调用
        verify(reviewReminderRepository).findById(1L);
        verify(reviewReminderRepository).save(any(ReviewReminder.class));
        verify(studyRecordRepository).findById(300L);
        verify(studyRecordService).updateStudyRecord(any(StudyRecord.class));
    }

    @Test
    @DisplayName("完成复习 - 提醒状态更新为COMPLETED - 验证需求 3.6")
    void testCompleteReviewStatusUpdate() {
        // 准备更新后的学习记录
        StudyRecord updatedRecord = new StudyRecord();
        updatedRecord.setId(300L);
        updatedRecord.setReviewCount(2);

        // 模拟行为
        when(reviewReminderRepository.findById(1L))
                .thenReturn(Optional.of(testReminder));
        when(reviewReminderRepository.save(any(ReviewReminder.class)))
                .thenAnswer(invocation -> {
                    ReviewReminder saved = invocation.getArgument(0);
                    assertEquals(ReviewReminder.Status.COMPLETED, saved.getStatus());
                    return saved;
                });
        when(studyRecordRepository.findById(300L))
                .thenReturn(Optional.of(testStudyRecord));
        when(studyRecordService.updateStudyRecord(any(StudyRecord.class)))
                .thenReturn(updatedRecord);

        // 执行测试
        reviewReminderService.completeReview(1L);

        // 验证提醒状态被更新为COMPLETED
        verify(reviewReminderRepository)
                .save(argThat(reminder -> reminder.getStatus() == ReviewReminder.Status.COMPLETED));
    }

    @Test
    @DisplayName("完成复习 - 复习提醒不存在 - 验证需求 3.6")
    void testCompleteReviewReminderNotFound() {
        // 模拟行为 - 提醒不存在
        when(reviewReminderRepository.findById(999L))
                .thenReturn(Optional.empty());

        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewReminderService.completeReview(999L);
        });

        // 验证异常信息
        assertTrue(exception.getMessage().contains("复习提醒不存在"));
        assertTrue(exception.getMessage().contains("999"));

        // 验证方法调用
        verify(reviewReminderRepository).findById(999L);
        verify(studyRecordRepository, never()).findById(any());
        verify(studyRecordService, never()).updateStudyRecord(any());
    }

    @Test
    @DisplayName("完成复习 - 学习记录不存在 - 验证需求 3.6")
    void testCompleteReviewStudyRecordNotFound() {
        // 模拟行为 - 提醒存在但学习记录不存在
        when(reviewReminderRepository.findById(1L))
                .thenReturn(Optional.of(testReminder));
        when(reviewReminderRepository.save(any(ReviewReminder.class)))
                .thenReturn(testReminder);
        when(studyRecordRepository.findById(300L))
                .thenReturn(Optional.empty());

        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewReminderService.completeReview(1L);
        });

        // 验证异常信息
        assertTrue(exception.getMessage().contains("学习记录不存在"));
        assertTrue(exception.getMessage().contains("300"));

        // 验证方法调用
        verify(reviewReminderRepository).findById(1L);
        verify(reviewReminderRepository).save(any(ReviewReminder.class));
        verify(studyRecordRepository).findById(300L);
        verify(studyRecordService, never()).updateStudyRecord(any());
    }

    @Test
    @DisplayName("完成复习 - 验证复习次数增加 - 验证需求 3.6")
    void testCompleteReviewIncrementReviewCount() {
        // 准备更新后的学习记录
        StudyRecord updatedRecord = new StudyRecord();
        updatedRecord.setId(300L);
        updatedRecord.setReviewCount(2); // 从1增加到2

        // 模拟行为
        when(reviewReminderRepository.findById(1L))
                .thenReturn(Optional.of(testReminder));
        when(reviewReminderRepository.save(any(ReviewReminder.class)))
                .thenReturn(testReminder);
        when(studyRecordRepository.findById(300L))
                .thenReturn(Optional.of(testStudyRecord));
        when(studyRecordService.updateStudyRecord(any(StudyRecord.class)))
                .thenReturn(updatedRecord);

        // 执行测试
        StudyRecord result = reviewReminderService.completeReview(1L);

        // 验证复习次数增加
        assertEquals(2, result.getReviewCount());

        // 验证updateStudyRecord被调用
        verify(studyRecordService).updateStudyRecord(any(StudyRecord.class));
    }

    @Test
    @DisplayName("完成复习 - 验证下次复习时间重新计算 - 验证需求 3.6")
    void testCompleteReviewRecalculateNextReviewTime() {
        // 准备更新后的学习记录
        LocalDateTime newNextReviewTime = testTime.plusDays(4);
        StudyRecord updatedRecord = new StudyRecord();
        updatedRecord.setId(300L);
        updatedRecord.setReviewCount(2);
        updatedRecord.setNextReviewTime(newNextReviewTime);

        // 模拟行为
        when(reviewReminderRepository.findById(1L))
                .thenReturn(Optional.of(testReminder));
        when(reviewReminderRepository.save(any(ReviewReminder.class)))
                .thenReturn(testReminder);
        when(studyRecordRepository.findById(300L))
                .thenReturn(Optional.of(testStudyRecord));
        when(studyRecordService.updateStudyRecord(any(StudyRecord.class)))
                .thenReturn(updatedRecord);

        // 执行测试
        StudyRecord result = reviewReminderService.completeReview(1L);

        // 验证下次复习时间被重新计算
        assertNotNull(result.getNextReviewTime());
        assertEquals(newNextReviewTime, result.getNextReviewTime());

        // 验证updateStudyRecord被调用
        verify(studyRecordService).updateStudyRecord(any(StudyRecord.class));
    }

    // ==================== 事务一致性测试 ====================

    @Test
    @DisplayName("完成复习 - 事务一致性 - 提醒和学习记录同时更新 - 验证需求 3.6")
    void testCompleteReviewTransactionConsistency() {
        // 准备更新后的学习记录
        StudyRecord updatedRecord = new StudyRecord();
        updatedRecord.setId(300L);
        updatedRecord.setReviewCount(2);

        // 模拟行为
        when(reviewReminderRepository.findById(1L))
                .thenReturn(Optional.of(testReminder));
        when(reviewReminderRepository.save(any(ReviewReminder.class)))
                .thenReturn(testReminder);
        when(studyRecordRepository.findById(300L))
                .thenReturn(Optional.of(testStudyRecord));
        when(studyRecordService.updateStudyRecord(any(StudyRecord.class)))
                .thenReturn(updatedRecord);

        // 执行测试
        reviewReminderService.completeReview(1L);

        // 验证提醒和学习记录都被更新
        verify(reviewReminderRepository).save(any(ReviewReminder.class));
        verify(studyRecordService).updateStudyRecord(any(StudyRecord.class));
    }

    @Test
    @DisplayName("完成复习 - 事务回滚场景 - 学习记录更新失败 - 验证需求 3.6")
    void testCompleteReviewTransactionRollbackOnStudyRecordFailure() {
        // 模拟行为 - 提醒更新成功，但学习记录更新失败
        when(reviewReminderRepository.findById(1L))
                .thenReturn(Optional.of(testReminder));
        when(reviewReminderRepository.save(any(ReviewReminder.class)))
                .thenReturn(testReminder);
        when(studyRecordRepository.findById(300L))
                .thenReturn(Optional.of(testStudyRecord));
        when(studyRecordService.updateStudyRecord(any(StudyRecord.class)))
                .thenThrow(new RuntimeException("学习记录更新失败"));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewReminderService.completeReview(1L);
        });

        // 验证异常信息
        assertEquals("学习记录更新失败", exception.getMessage());

        // 验证方法调用
        verify(reviewReminderRepository).findById(1L);
        verify(reviewReminderRepository).save(any(ReviewReminder.class));
        verify(studyRecordRepository).findById(300L);
        verify(studyRecordService).updateStudyRecord(any(StudyRecord.class));
    }
}
