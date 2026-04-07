package com.example.ancientstars.service;

import com.example.ancientstars.dto.StudentProgressDTO;
import com.example.ancientstars.entity.DailyProgress;
import com.example.ancientstars.repository.DailyProgressRepository;
import com.example.ancientstars.repository.StudyRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 每日进度服务单元测试
 * 验证需求: 4.1, 4.2, 4.3, 7.2
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("每日进度服务测试")
class DailyProgressServiceTest {

    @Mock
    private DailyProgressRepository dailyProgressRepository;

    @Mock
    private StudyRecordRepository studyRecordRepository;

    @InjectMocks
    private DailyProgressService dailyProgressService;

    private DailyProgress testProgress;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2026, 4, 1);

        // 创建测试每日进度
        testProgress = new DailyProgress();
        testProgress.setId(1L);
        testProgress.setStudentId(100L);
        testProgress.setStudyDate(testDate);
        testProgress.setNewWordsCount(5);
        testProgress.setReviewWordsCount(3);
        testProgress.setTotalStudyTime(0);
    }

    // ==================== 更新每日进度测试 ====================

    @Test
    @DisplayName("更新每日进度 - 新学单词 - 创建新记录 - 验证需求 4.1, 4.2")
    void testUpdateDailyProgressNewWordCreateNew() {
        // 模拟行为 - 不存在今日进度记录
        when(dailyProgressRepository.findByStudentIdAndStudyDate(100L, testDate))
                .thenReturn(Optional.empty());

        ArgumentCaptor<DailyProgress> progressCaptor = ArgumentCaptor.forClass(DailyProgress.class);
        when(dailyProgressRepository.save(progressCaptor.capture()))
                .thenReturn(testProgress);

        // 执行测试
        dailyProgressService.updateDailyProgress(100L, testDate, true);

        // 验证保存的进度
        DailyProgress savedProgress = progressCaptor.getValue();
        assertNotNull(savedProgress);
        assertEquals(100L, savedProgress.getStudentId());
        assertEquals(testDate, savedProgress.getStudyDate());
        assertEquals(1, savedProgress.getNewWordsCount()); // 新学单词数应该为1
        assertEquals(0, savedProgress.getReviewWordsCount());

        // 验证方法调用
        verify(dailyProgressRepository).findByStudentIdAndStudyDate(100L, testDate);
        verify(dailyProgressRepository).save(any(DailyProgress.class));
    }

    @Test
    @DisplayName("更新每日进度 - 新学单词 - 增量更新现有记录 - 验证需求 4.1, 4.2")
    void testUpdateDailyProgressNewWordIncrement() {
        // 准备现有进度记录
        DailyProgress existingProgress = new DailyProgress();
        existingProgress.setId(1L);
        existingProgress.setStudentId(100L);
        existingProgress.setStudyDate(testDate);
        existingProgress.setNewWordsCount(10);
        existingProgress.setReviewWordsCount(5);
        existingProgress.setTotalStudyTime(0);

        // 模拟行为
        when(dailyProgressRepository.findByStudentIdAndStudyDate(100L, testDate))
                .thenReturn(Optional.of(existingProgress));

        ArgumentCaptor<DailyProgress> progressCaptor = ArgumentCaptor.forClass(DailyProgress.class);
        when(dailyProgressRepository.save(progressCaptor.capture()))
                .thenReturn(existingProgress);

        // 执行测试
        dailyProgressService.updateDailyProgress(100L, testDate, true);

        // 验证增量更新
        DailyProgress savedProgress = progressCaptor.getValue();
        assertEquals(11, savedProgress.getNewWordsCount()); // 从10增加到11
        assertEquals(5, savedProgress.getReviewWordsCount()); // 保持不变

        // 验证方法调用
        verify(dailyProgressRepository).findByStudentIdAndStudyDate(100L, testDate);
        verify(dailyProgressRepository).save(any(DailyProgress.class));
    }

    @Test
    @DisplayName("更新每日进度 - 复习单词 - 增量更新 - 验证需求 4.1, 4.2")
    void testUpdateDailyProgressReviewWord() {
        // 准备现有进度记录
        DailyProgress existingProgress = new DailyProgress();
        existingProgress.setId(1L);
        existingProgress.setStudentId(100L);
        existingProgress.setStudyDate(testDate);
        existingProgress.setNewWordsCount(5);
        existingProgress.setReviewWordsCount(3);
        existingProgress.setTotalStudyTime(0);

        // 模拟行为
        when(dailyProgressRepository.findByStudentIdAndStudyDate(100L, testDate))
                .thenReturn(Optional.of(existingProgress));

        ArgumentCaptor<DailyProgress> progressCaptor = ArgumentCaptor.forClass(DailyProgress.class);
        when(dailyProgressRepository.save(progressCaptor.capture()))
                .thenReturn(existingProgress);

        // 执行测试
        dailyProgressService.updateDailyProgress(100L, testDate, false);

        // 验证增量更新
        DailyProgress savedProgress = progressCaptor.getValue();
        assertEquals(5, savedProgress.getNewWordsCount()); // 保持不变
        assertEquals(4, savedProgress.getReviewWordsCount()); // 从3增加到4

        // 验证方法调用
        verify(dailyProgressRepository).findByStudentIdAndStudyDate(100L, testDate);
        verify(dailyProgressRepository).save(any(DailyProgress.class));
    }

    @Test
    @DisplayName("更新每日进度 - 多次增量更新 - 验证需求 4.2")
    void testUpdateDailyProgressMultipleIncrements() {
        // 准备现有进度记录
        DailyProgress existingProgress = new DailyProgress();
        existingProgress.setId(1L);
        existingProgress.setStudentId(100L);
        existingProgress.setStudyDate(testDate);
        existingProgress.setNewWordsCount(0);
        existingProgress.setReviewWordsCount(0);
        existingProgress.setTotalStudyTime(0);

        // 模拟行为
        when(dailyProgressRepository.findByStudentIdAndStudyDate(100L, testDate))
                .thenReturn(Optional.of(existingProgress));
        when(dailyProgressRepository.save(any(DailyProgress.class)))
                .thenReturn(existingProgress);

        // 执行多次更新
        dailyProgressService.updateDailyProgress(100L, testDate, true); // 新学 +1
        dailyProgressService.updateDailyProgress(100L, testDate, true); // 新学 +1
        dailyProgressService.updateDailyProgress(100L, testDate, false); // 复习 +1

        // 验证最终结果
        assertEquals(2, existingProgress.getNewWordsCount());
        assertEquals(1, existingProgress.getReviewWordsCount());

        // 验证方法调用次数
        verify(dailyProgressRepository, times(3)).findByStudentIdAndStudyDate(100L, testDate);
        verify(dailyProgressRepository, times(3)).save(any(DailyProgress.class));
    }

    // ==================== 查询学生进度测试 ====================

    @Test
    @DisplayName("查询学生进度 - 返回今日、本周和累计统计 - 验证需求 4.3")
    void testGetStudentProgress() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        // 准备今日进度
        DailyProgress todayProgress = new DailyProgress();
        todayProgress.setStudentId(100L);
        todayProgress.setStudyDate(today);
        todayProgress.setNewWordsCount(15);
        todayProgress.setReviewWordsCount(8);

        // 准备本周进度
        List<DailyProgress> weekProgress = Arrays.asList(
                createProgress(100L, today.minusDays(6), 10, 5),
                createProgress(100L, today.minusDays(5), 12, 6),
                createProgress(100L, today.minusDays(4), 8, 4),
                createProgress(100L, today.minusDays(3), 15, 7),
                createProgress(100L, today.minusDays(2), 20, 10),
                createProgress(100L, today.minusDays(1), 18, 9),
                todayProgress);

        // 准备最近30天进度
        LocalDate defaultStartDate = today.minusDays(29);
        List<DailyProgress> monthProgress = Arrays.asList(
                createProgress(100L, today.minusDays(2), 20, 10),
                createProgress(100L, today.minusDays(1), 18, 9),
                todayProgress);

        // 模拟行为
        when(dailyProgressRepository.findByStudentIdAndStudyDate(100L, today))
                .thenReturn(Optional.of(todayProgress));
        when(dailyProgressRepository.findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(100L, weekStart, today))
                .thenReturn(weekProgress);
        when(dailyProgressRepository.findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(100L, defaultStartDate,
                today))
                .thenReturn(monthProgress);
        when(studyRecordRepository.countByStudentId(100L))
                .thenReturn(320L);

        // 执行测试
        StudentProgressDTO result = dailyProgressService.getStudentProgress(100L);

        // 验证结果
        assertNotNull(result);
        assertEquals(15, result.getTodayNewWords());
        assertEquals(8, result.getTodayReviewWords());
        assertEquals(98, result.getWeekNewWords()); // 10+12+8+15+20+18+15
        assertEquals(320, result.getTotalLearnedWords());
        assertNotNull(result.getDailyProgress());
        assertEquals(3, result.getDailyProgress().size());

        // 验证方法调用
        verify(dailyProgressRepository).findByStudentIdAndStudyDate(100L, today);
        verify(dailyProgressRepository).findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(100L, weekStart, today);
        verify(dailyProgressRepository).findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(100L, defaultStartDate,
                today);
        verify(studyRecordRepository).countByStudentId(100L);
    }

    @Test
    @DisplayName("查询学生进度 - 指定日期范围 - 验证需求 4.3")
    void testGetStudentProgressWithDateRange() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        LocalDate startDate = today.minusDays(10);
        LocalDate endDate = today.minusDays(5);

        // 准备今日进度
        DailyProgress todayProgress = new DailyProgress();
        todayProgress.setStudentId(100L);
        todayProgress.setStudyDate(today);
        todayProgress.setNewWordsCount(15);
        todayProgress.setReviewWordsCount(8);

        // 准备本周进度
        List<DailyProgress> weekProgress = Arrays.asList(todayProgress);

        // 准备指定日期范围进度
        List<DailyProgress> customRangeProgress = Arrays.asList(
                createProgress(100L, today.minusDays(10), 10, 5),
                createProgress(100L, today.minusDays(8), 12, 6),
                createProgress(100L, today.minusDays(5), 8, 4));

        // 模拟行为
        when(dailyProgressRepository.findByStudentIdAndStudyDate(100L, today))
                .thenReturn(Optional.of(todayProgress));
        when(dailyProgressRepository.findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(100L, weekStart, today))
                .thenReturn(weekProgress);
        when(dailyProgressRepository.findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(100L, startDate, endDate))
                .thenReturn(customRangeProgress);
        when(studyRecordRepository.countByStudentId(100L))
                .thenReturn(320L);

        // 执行测试
        StudentProgressDTO result = dailyProgressService.getStudentProgress(100L, startDate, endDate);

        // 验证结果
        assertNotNull(result);
        assertEquals(15, result.getTodayNewWords());
        assertEquals(8, result.getTodayReviewWords());
        assertEquals(15, result.getWeekNewWords());
        assertEquals(320, result.getTotalLearnedWords());
        assertNotNull(result.getDailyProgress());
        assertEquals(3, result.getDailyProgress().size());

        // 验证每日进度详情
        StudentProgressDTO.DailyProgressDetail firstDetail = result.getDailyProgress().get(0);
        assertEquals(today.minusDays(10), firstDetail.getDate());
        assertEquals(10, firstDetail.getNewWords());
        assertEquals(5, firstDetail.getReviewWords());

        // 验证方法调用
        verify(dailyProgressRepository).findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(100L, startDate, endDate);
    }

    @Test
    @DisplayName("查询学生进度 - 无今日进度 - 验证需求 4.3")
    void testGetStudentProgressNoTodayProgress() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        LocalDate defaultStartDate = today.minusDays(29);

        // 模拟行为 - 无今日进度
        when(dailyProgressRepository.findByStudentIdAndStudyDate(100L, today))
                .thenReturn(Optional.empty());
        when(dailyProgressRepository.findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(100L, weekStart, today))
                .thenReturn(Arrays.asList());
        when(dailyProgressRepository.findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(100L, defaultStartDate,
                today))
                .thenReturn(Arrays.asList());
        when(studyRecordRepository.countByStudentId(100L))
                .thenReturn(0L);

        // 执行测试
        StudentProgressDTO result = dailyProgressService.getStudentProgress(100L);

        // 验证结果 - 应该返回0值
        assertNotNull(result);
        assertEquals(0, result.getTodayNewWords());
        assertEquals(0, result.getTodayReviewWords());
        assertEquals(0, result.getWeekNewWords());
        assertEquals(0, result.getTotalLearnedWords());
        assertNotNull(result.getDailyProgress());
        assertEquals(0, result.getDailyProgress().size());
    }

    @Test
    @DisplayName("查询学生进度 - 验证每日进度详情格式 - 验证需求 4.3")
    void testGetStudentProgressDailyProgressDetailFormat() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        LocalDate defaultStartDate = today.minusDays(29);

        // 准备今日进度
        DailyProgress todayProgress = new DailyProgress();
        todayProgress.setStudentId(100L);
        todayProgress.setStudyDate(today);
        todayProgress.setNewWordsCount(15);
        todayProgress.setReviewWordsCount(8);

        // 准备最近30天进度
        List<DailyProgress> monthProgress = Arrays.asList(
                createProgress(100L, today.minusDays(2), 20, 10),
                createProgress(100L, today.minusDays(1), 18, 9),
                todayProgress);

        // 模拟行为
        when(dailyProgressRepository.findByStudentIdAndStudyDate(100L, today))
                .thenReturn(Optional.of(todayProgress));
        when(dailyProgressRepository.findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(100L, weekStart, today))
                .thenReturn(Arrays.asList(todayProgress));
        when(dailyProgressRepository.findByStudentIdAndStudyDateBetweenOrderByStudyDateAsc(100L, defaultStartDate,
                today))
                .thenReturn(monthProgress);
        when(studyRecordRepository.countByStudentId(100L))
                .thenReturn(320L);

        // 执行测试
        StudentProgressDTO result = dailyProgressService.getStudentProgress(100L);

        // 验证每日进度详情
        assertNotNull(result.getDailyProgress());
        assertEquals(3, result.getDailyProgress().size());

        // 验证第一条详情
        StudentProgressDTO.DailyProgressDetail detail1 = result.getDailyProgress().get(0);
        assertEquals(today.minusDays(2), detail1.getDate());
        assertEquals(20, detail1.getNewWords());
        assertEquals(10, detail1.getReviewWords());

        // 验证最后一条详情
        StudentProgressDTO.DailyProgressDetail detail3 = result.getDailyProgress().get(2);
        assertEquals(today, detail3.getDate());
        assertEquals(15, detail3.getNewWords());
        assertEquals(8, detail3.getReviewWords());
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试用的每日进度对象
     */
    private DailyProgress createProgress(Long studentId, LocalDate date, int newWords, int reviewWords) {
        DailyProgress progress = new DailyProgress();
        progress.setStudentId(studentId);
        progress.setStudyDate(date);
        progress.setNewWordsCount(newWords);
        progress.setReviewWordsCount(reviewWords);
        progress.setTotalStudyTime(0);
        return progress;
    }
}
