package com.example.ancientstars.scheduler;

import com.example.ancientstars.entity.ReviewReminder;
import com.example.ancientstars.entity.StudyRecord;
import com.example.ancientstars.repository.StudyRecordRepository;
import com.example.ancientstars.service.ReviewReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ReviewScheduler 单元测试
 * 
 * <p>
 * 测试需求：
 * </p>
 * <ul>
 * <li>需求 6.1: 验证调度器能够正确执行</li>
 * <li>需求 6.2: 验证能够查询需要复习的学习记录</li>
 * <li>需求 6.3: 验证能够为每条记录生成复习提醒</li>
 * <li>需求 6.4: 验证错误处理和日志记录</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("复习调度器测试")
class ReviewSchedulerTest {

    @Mock
    private StudyRecordRepository studyRecordRepository;

    @Mock
    private ReviewReminderService reviewReminderService;

    @InjectMocks
    private ReviewScheduler reviewScheduler;

    private List<StudyRecord> mockStudyRecords;
    private ReviewReminder mockReminder;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        mockStudyRecords = new ArrayList<>();

        StudyRecord record1 = new StudyRecord();
        record1.setId(1L);
        record1.setStudentId(100L);
        record1.setVocabularyId(200L);
        record1.setNextReviewTime(LocalDateTime.now().minusDays(1));
        mockStudyRecords.add(record1);

        StudyRecord record2 = new StudyRecord();
        record2.setId(2L);
        record2.setStudentId(101L);
        record2.setVocabularyId(201L);
        record2.setNextReviewTime(LocalDateTime.now().minusDays(2));
        mockStudyRecords.add(record2);

        mockReminder = new ReviewReminder();
        mockReminder.setId(1L);
        mockReminder.setStatus(ReviewReminder.Status.PENDING);
    }

    @Test
    @DisplayName("应该成功生成复习提醒 - 有需要复习的记录")
    void shouldGenerateReviewRemindersSuccessfully() {
        // Given: 有需要复习的学习记录
        when(studyRecordRepository.findRecordsNeedReview(any(LocalDateTime.class)))
                .thenReturn(mockStudyRecords);
        when(reviewReminderService.createReminder(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(mockReminder);

        // When: 执行调度任务
        reviewScheduler.generateDailyReviewReminders();

        // Then: 应该查询需要复习的记录
        verify(studyRecordRepository, times(1)).findRecordsNeedReview(any(LocalDateTime.class));

        // Then: 应该为每条记录创建复习提醒
        verify(reviewReminderService, times(2)).createReminder(
                anyLong(), anyLong(), anyLong(), any(LocalDateTime.class));

        // 验证第一条记录的参数
        verify(reviewReminderService).createReminder(
                eq(100L), eq(200L), eq(1L), any(LocalDateTime.class));

        // 验证第二条记录的参数
        verify(reviewReminderService).createReminder(
                eq(101L), eq(201L), eq(2L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("应该正常处理 - 没有需要复习的记录")
    void shouldHandleNoRecordsToReview() {
        // Given: 没有需要复习的记录
        when(studyRecordRepository.findRecordsNeedReview(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When: 执行调度任务
        reviewScheduler.generateDailyReviewReminders();

        // Then: 应该查询记录
        verify(studyRecordRepository, times(1)).findRecordsNeedReview(any(LocalDateTime.class));

        // Then: 不应该创建任何提醒
        verify(reviewReminderService, never()).createReminder(
                anyLong(), anyLong(), anyLong(), any(LocalDateTime.class)
        );
    }

@Test
    @DisplayName("应该继续处理其他记录 - 当某条记录创建提醒失败时")
    void shouldContinueProcessingWhenOneReminderFails() {
        // Given: 有需要复习的记录，但第一条记录创建提醒会失败
        when(studyRecordRepository.findRecordsNeedReview(any(LocalDateTime.class)))
                .thenReturn(mockStudyRecords);
        
        // 第一次调用抛出异常，第二次调用成功
        when(reviewReminderService.createReminder(eq(100L), eq(200L), eq(1L), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));
        when(reviewReminderService.createReminder(eq(101L), eq(201L), eq(2L), any(LocalDateTime.class)))
                .thenReturn(mockReminder);

        // When: 执行调度任务
        reviewScheduler.generateDailyReviewReminders();

        // Then: 应该尝试为所有记录创建提醒
        verify(reviewReminderService, times(2)).createReminder(
                anyLong(), anyLong(), anyLong(), any(LocalDateTime.class)
        );

        // Then: 第二条记录应该成功创建
        verify(reviewReminderService).createReminder(
                eq(101L), eq(201L), eq(2L), any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("应该捕获异常并记录日志 - 当查询记录失败时")
    void shouldCatchExceptionWhenQueryFails() {
        // Given: 查询记录时抛出异常
        when(studyRecordRepository.findRecordsNeedReview(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // When: 执行调度任务（不应该抛出异常）
        reviewScheduler.generateDailyReviewReminders();

        // Then: 应该尝试查询记录
        verify(studyRecordRepository, times(1)).findRecordsNeedReview(any(LocalDateTime.class));

        // Then: 不应该创建任何提醒
        verify(reviewReminderService, never()).createReminder(
                anyLong(), anyLong(), anyLong(), any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("应该处理大量记录 - 性能测试")
    void shouldHandleLargeNumberOfRecords() {
        // Given: 有大量需要复习的记录（模拟10000条）
        List<StudyRecord> largeRecordList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            StudyRecord record = new StudyRecord();
            record.setId((long) i);
            record.setStudentId((long) (i + 1000));
            record.setVocabularyId((long) (i + 2000));
            record.setNextReviewTime(LocalDateTime.now().minusDays(1));
            largeRecordList.add(record);
        }

        when(studyRecordRepository.findRecordsNeedReview(any(LocalDateTime.class)))
                .thenReturn(largeRecordList);
        when(reviewReminderService.createReminder(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(mockReminder);

        // When: 执行调度任务
        long startTime = System.currentTimeMillis();
        reviewScheduler.generateDailyReviewReminders();
        long duration = System.currentTimeMillis() - startTime;

        // Then: 应该为所有记录创建提醒
        verify(reviewReminderService, times(10000)).createReminder(
                anyLong(), anyLong(), anyLong(), any(LocalDateTime.class)
        );

        // Then: 执行时间应该在合理范围内（考虑到是mock，应该很快）
        System.out.println("处理10000条记录耗时: " + duration + " ms");
    }

    @Test
    @DisplayName("应该使用正确的时间参数查询记录")
    void shouldUseCorrectTimeParameterForQuery() {
        // Given
        when(studyRecordRepository.findRecordsNeedReview(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When: 执行调度任务
        LocalDateTime beforeExecution = LocalDateTime.now();
        reviewScheduler.generateDailyReviewReminders();
        LocalDateTime afterExecution = LocalDateTime.now();

        // Then: 应该使用当前时间查询（验证时间参数在合理范围内）
        verify(studyRecordRepository).findRecordsNeedReview(
                argThat(time -> !time.isBefore(beforeExecution) && !time.isAfter(afterExecution))
        );
    }
}
