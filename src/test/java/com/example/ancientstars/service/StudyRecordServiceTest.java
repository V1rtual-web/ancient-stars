package com.example.ancientstars.service;

import com.example.ancientstars.algorithm.EbbinghausAlgorithm;
import com.example.ancientstars.entity.DailyProgress;
import com.example.ancientstars.entity.StudyRecord;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 学习记录服务单元测试
 * 验证需求: 2.1, 2.2, 2.3, 7.1, 7.2
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("学习记录服务测试")
class StudyRecordServiceTest {

        @Mock
        private StudyRecordRepository studyRecordRepository;

        @Mock
        private DailyProgressRepository dailyProgressRepository;

        @Mock
        private DailyProgressService dailyProgressService;

        @Mock
        private EbbinghausAlgorithm ebbinghausAlgorithm;

        @InjectMocks
        private StudyRecordService studyRecordService;

        private StudyRecord testRecord;
        private DailyProgress testProgress;
        private LocalDateTime testTime;

        @BeforeEach
        void setUp() {
                testTime = LocalDateTime.of(2026, 4, 1, 10, 30, 0);

                // 创建测试学习记录
                testRecord = new StudyRecord();
                testRecord.setId(1L);
                testRecord.setStudentId(100L);
                testRecord.setVocabularyId(200L);
                testRecord.setStudyTime(testTime);
                testRecord.setReviewCount(0);
                testRecord.setLastReviewTime(null);
                testRecord.setNextReviewTime(testTime.plusDays(7));
                testRecord.setMasteryLevel(StudyRecord.MasteryLevel.NEW);

                // 创建测试每日进度
                testProgress = new DailyProgress();
                testProgress.setId(1L);
                testProgress.setStudentId(100L);
                testProgress.setStudyDate(LocalDate.of(2026, 4, 1));
                testProgress.setNewWordsCount(5);
                testProgress.setReviewWordsCount(3);
                testProgress.setTotalStudyTime(0);
        }

        // ==================== 创建学习记录测试 ====================

        @Test
        @DisplayName("创建新学习记录 - 验证需求 2.1")
        void testCreateStudyRecordNewRecord() {
                // 模拟行为
                when(studyRecordRepository.findByStudentIdAndVocabularyId(100L, 200L))
                                .thenReturn(Optional.empty());
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(0)))
                                .thenReturn(testTime.plusDays(7));
                when(studyRecordRepository.save(any(StudyRecord.class)))
                                .thenReturn(testRecord);

                // 执行测试
                StudyRecord result = studyRecordService.createStudyRecord(100L, 200L);

                // 验证结果
                assertNotNull(result);
                assertEquals(100L, result.getStudentId());
                assertEquals(200L, result.getVocabularyId());
                assertEquals(0, result.getReviewCount());
                assertEquals(StudyRecord.MasteryLevel.NEW, result.getMasteryLevel());
                assertNotNull(result.getNextReviewTime());

                // 验证方法调用
                verify(studyRecordRepository).findByStudentIdAndVocabularyId(100L, 200L);
                verify(ebbinghausAlgorithm).calculateNextReviewTime(any(LocalDateTime.class), eq(0));
                verify(studyRecordRepository).save(any(StudyRecord.class));
                verify(dailyProgressService).updateDailyProgress(100L, LocalDate.now(), true);
        }

        @Test
        @DisplayName("创建学习记录 - 幂等性检查 - 更新现有记录 - 验证需求 2.2, 2.3")
        void testCreateStudyRecordIdempotency() {
                // 准备现有记录
                StudyRecord existingRecord = new StudyRecord();
                existingRecord.setId(1L);
                existingRecord.setStudentId(100L);
                existingRecord.setVocabularyId(200L);
                existingRecord.setStudyTime(testTime.minusDays(7));
                existingRecord.setReviewCount(1);
                existingRecord.setLastReviewTime(testTime.minusDays(1));
                existingRecord.setNextReviewTime(testTime);
                existingRecord.setMasteryLevel(StudyRecord.MasteryLevel.LEARNING);

                // 模拟行为
                when(studyRecordRepository.findByStudentIdAndVocabularyId(100L, 200L))
                                .thenReturn(Optional.of(existingRecord));
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(2)))
                                .thenReturn(testTime.plusDays(4));
                when(studyRecordRepository.save(any(StudyRecord.class)))
                                .thenReturn(existingRecord);

                // 执行测试
                StudyRecord result = studyRecordService.createStudyRecord(100L, 200L);

                // 验证结果 - 应该更新现有记录而不是创建新记录
                assertNotNull(result);
                assertEquals(1L, result.getId());
                assertEquals(2, result.getReviewCount()); // 复习次数应该增加
                assertNotNull(result.getLastReviewTime());

                // 验证方法调用 - 应该调用更新而不是创建
                verify(studyRecordRepository).findByStudentIdAndVocabularyId(100L, 200L);
                verify(studyRecordRepository).save(any(StudyRecord.class));
                verify(ebbinghausAlgorithm).calculateNextReviewTime(any(LocalDateTime.class), eq(2));
                verify(dailyProgressService).updateDailyProgress(100L, LocalDate.now(), false);
        }

        // ==================== 更新学习记录测试 ====================

        @Test
        @DisplayName("更新学习记录 - 增加复习次数 - 验证需求 2.2")
        void testUpdateStudyRecord() {
                // 准备测试数据
                StudyRecord recordToUpdate = new StudyRecord();
                recordToUpdate.setId(1L);
                recordToUpdate.setStudentId(100L);
                recordToUpdate.setVocabularyId(200L);
                recordToUpdate.setStudyTime(testTime.minusDays(7));
                recordToUpdate.setReviewCount(1);
                recordToUpdate.setLastReviewTime(testTime.minusDays(2));
                recordToUpdate.setNextReviewTime(testTime);
                recordToUpdate.setMasteryLevel(StudyRecord.MasteryLevel.LEARNING);

                // 模拟行为
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(2)))
                                .thenReturn(testTime.plusDays(4));
                when(studyRecordRepository.save(any(StudyRecord.class)))
                                .thenReturn(recordToUpdate);

                // 执行测试
                StudyRecord result = studyRecordService.updateStudyRecord(recordToUpdate);

                // 验证结果
                assertNotNull(result);
                assertEquals(2, result.getReviewCount()); // 复习次数应该从1增加到2
                assertNotNull(result.getLastReviewTime());
                assertNotNull(result.getNextReviewTime());
                assertEquals(StudyRecord.MasteryLevel.LEARNING, result.getMasteryLevel());

                // 验证方法调用
                verify(ebbinghausAlgorithm).calculateNextReviewTime(any(LocalDateTime.class), eq(2));
                verify(studyRecordRepository).save(any(StudyRecord.class));
                verify(dailyProgressService).updateDailyProgress(100L, LocalDate.now(), false);
        }

        @Test
        @DisplayName("更新学习记录 - 掌握程度变化 - 验证需求 2.2")
        void testUpdateStudyRecordMasteryLevelProgression() {
                // 测试不同复习次数对应的掌握程度
                StudyRecord record = new StudyRecord();
                record.setId(1L);
                record.setStudentId(100L);
                record.setVocabularyId(200L);
                record.setStudyTime(testTime);

                // 模拟行为
                when(studyRecordRepository.save(any(StudyRecord.class))).thenReturn(record);

                // 测试复习次数 0 -> 1 (NEW -> LEARNING)
                record.setReviewCount(0);
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(1)))
                                .thenReturn(testTime.plusDays(2));
                StudyRecord result1 = studyRecordService.updateStudyRecord(record);
                assertEquals(StudyRecord.MasteryLevel.LEARNING, result1.getMasteryLevel());

                // 测试复习次数 2 -> 3 (LEARNING -> FAMILIAR)
                record.setReviewCount(2);
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(3)))
                                .thenReturn(testTime.plusDays(7));
                StudyRecord result2 = studyRecordService.updateStudyRecord(record);
                assertEquals(StudyRecord.MasteryLevel.FAMILIAR, result2.getMasteryLevel());

                // 测试复习次数 4 -> 5 (FAMILIAR -> MASTERED)
                record.setReviewCount(4);
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(5)))
                                .thenReturn(testTime.plusDays(30));
                StudyRecord result3 = studyRecordService.updateStudyRecord(record);
                assertEquals(StudyRecord.MasteryLevel.MASTERED, result3.getMasteryLevel());
        }

        // ==================== 每日进度更新测试 ====================

        @Test
        @DisplayName("更新每日进度 - 新学单词 - 验证需求 7.2")
        void testUpdateDailyProgressNewWord() {
                // 模拟行为 - 不存在今日进度记录
                when(studyRecordRepository.findByStudentIdAndVocabularyId(100L, 200L))
                                .thenReturn(Optional.empty());
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(0)))
                                .thenReturn(testTime.plusDays(7));
                when(studyRecordRepository.save(any(StudyRecord.class)))
                                .thenReturn(testRecord);

                // 执行测试
                studyRecordService.createStudyRecord(100L, 200L);

                // 验证每日进度服务被调用
                verify(dailyProgressService).updateDailyProgress(100L, LocalDate.now(), true);
        }

        @Test
        @DisplayName("更新每日进度 - 复习单词 - 验证需求 7.2")
        void testUpdateDailyProgressReviewWord() {
                // 准备学习记录
                StudyRecord recordToUpdate = new StudyRecord();
                recordToUpdate.setId(1L);
                recordToUpdate.setStudentId(100L);
                recordToUpdate.setVocabularyId(200L);
                recordToUpdate.setReviewCount(1);

                // 模拟行为
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(2)))
                                .thenReturn(testTime.plusDays(4));
                when(studyRecordRepository.save(any(StudyRecord.class)))
                                .thenReturn(recordToUpdate);

                // 执行测试
                studyRecordService.updateStudyRecord(recordToUpdate);

                // 验证每日进度服务被调用
                verify(dailyProgressService).updateDailyProgress(100L, LocalDate.now(), false);
        }

        @Test
        @DisplayName("更新每日进度 - 累加统计 - 验证需求 7.2")
        void testUpdateDailyProgressAccumulation() {
                // 模拟行为
                when(studyRecordRepository.findByStudentIdAndVocabularyId(100L, 200L))
                                .thenReturn(Optional.empty());
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(0)))
                                .thenReturn(testTime.plusDays(7));
                when(studyRecordRepository.save(any(StudyRecord.class)))
                                .thenReturn(testRecord);

                // 执行测试
                studyRecordService.createStudyRecord(100L, 200L);

                // 验证每日进度服务被调用
                verify(dailyProgressService).updateDailyProgress(100L, LocalDate.now(), true);
        }

        // ==================== 事务一致性测试 ====================

        @Test
        @DisplayName("创建学习记录 - 集成艾宾浩斯算法 - 验证需求 2.3, 7.1")
        void testCreateStudyRecordIntegrationWithAlgorithm() {
                // 模拟行为
                when(studyRecordRepository.findByStudentIdAndVocabularyId(100L, 200L))
                                .thenReturn(Optional.empty());
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(0)))
                                .thenReturn(testTime.plusDays(7));
                when(studyRecordRepository.save(any(StudyRecord.class)))
                                .thenReturn(testRecord);

                // 执行测试
                StudyRecord result = studyRecordService.createStudyRecord(100L, 200L);

                // 验证艾宾浩斯算法被正确调用
                verify(ebbinghausAlgorithm).calculateNextReviewTime(any(LocalDateTime.class), eq(0));
                assertNotNull(result.getNextReviewTime());
        }

        @Test
        @DisplayName("更新学习记录 - 集成艾宾浩斯算法 - 验证需求 2.3, 7.1")
        void testUpdateStudyRecordIntegrationWithAlgorithm() {
                // 准备测试数据
                StudyRecord recordToUpdate = new StudyRecord();
                recordToUpdate.setId(1L);
                recordToUpdate.setStudentId(100L);
                recordToUpdate.setVocabularyId(200L);
                recordToUpdate.setReviewCount(2);

                // 模拟行为
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(3)))
                                .thenReturn(testTime.plusDays(7));
                when(studyRecordRepository.save(any(StudyRecord.class)))
                                .thenReturn(recordToUpdate);

                // 执行测试
                StudyRecord result = studyRecordService.updateStudyRecord(recordToUpdate);

                // 验证艾宾浩斯算法被正确调用
                verify(ebbinghausAlgorithm).calculateNextReviewTime(any(LocalDateTime.class), eq(3));
                assertEquals(3, result.getReviewCount());
                assertNotNull(result.getNextReviewTime());
        }

        // ==================== 事务回滚测试 ====================

        @Test
        @DisplayName("创建学习记录 - 事务回滚场景 - 每日进度保存失败 - 验证需求 7.2, 7.3")
        void testCreateStudyRecordTransactionRollbackOnDailyProgressFailure() {
                // 模拟行为 - 学习记录保存成功，但每日进度保存失败
                when(studyRecordRepository.findByStudentIdAndVocabularyId(100L, 200L))
                                .thenReturn(Optional.empty());
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(0)))
                                .thenReturn(testTime.plusDays(7));
                when(studyRecordRepository.save(any(StudyRecord.class)))
                                .thenReturn(testRecord);
                doThrow(new RuntimeException("数据库连接失败"))
                                .when(dailyProgressService).updateDailyProgress(100L, LocalDate.now(), true);

                // 执行测试并验证异常
                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        studyRecordService.createStudyRecord(100L, 200L);
                });

                // 验证异常信息
                assertEquals("数据库连接失败", exception.getMessage());

                // 验证学习记录保存被调用
                verify(studyRecordRepository).save(any(StudyRecord.class));
                // 验证每日进度服务被调用（但失败）
                verify(dailyProgressService).updateDailyProgress(100L, LocalDate.now(), true);
        }

        @Test
        @DisplayName("创建学习记录 - 事务回滚场景 - 学习记录保存失败 - 验证需求 7.2, 7.3")
        void testCreateStudyRecordTransactionRollbackOnStudyRecordFailure() {
                // 模拟行为 - 学习记录保存失败
                when(studyRecordRepository.findByStudentIdAndVocabularyId(100L, 200L))
                                .thenReturn(Optional.empty());
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(0)))
                                .thenReturn(testTime.plusDays(7));
                when(studyRecordRepository.save(any(StudyRecord.class)))
                                .thenThrow(new RuntimeException("学习记录保存失败"));

                // 执行测试并验证异常
                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        studyRecordService.createStudyRecord(100L, 200L);
                });

                // 验证异常信息
                assertEquals("学习记录保存失败", exception.getMessage());

                // 验证学习记录保存被调用
                verify(studyRecordRepository).save(any(StudyRecord.class));
                // 验证每日进度服务未被调用（因为学习记录保存失败）
                verify(dailyProgressService, never()).updateDailyProgress(any(), any(), anyBoolean());
        }

        @Test
        @DisplayName("更新学习记录 - 事务回滚场景 - 每日进度更新失败 - 验证需求 7.2, 7.3")
        void testUpdateStudyRecordTransactionRollbackOnDailyProgressFailure() {
                // 准备测试数据
                StudyRecord recordToUpdate = new StudyRecord();
                recordToUpdate.setId(1L);
                recordToUpdate.setStudentId(100L);
                recordToUpdate.setVocabularyId(200L);
                recordToUpdate.setReviewCount(1);

                // 模拟行为 - 学习记录更新成功，但每日进度更新失败
                when(ebbinghausAlgorithm.calculateNextReviewTime(any(LocalDateTime.class), eq(2)))
                                .thenReturn(testTime.plusDays(4));
                when(studyRecordRepository.save(any(StudyRecord.class)))
                                .thenReturn(recordToUpdate);
                doThrow(new RuntimeException("每日进度更新失败"))
                                .when(dailyProgressService).updateDailyProgress(100L, LocalDate.now(), false);

                // 执行测试并验证异常
                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                        studyRecordService.updateStudyRecord(recordToUpdate);
                });

                // 验证异常信息
                assertEquals("每日进度更新失败", exception.getMessage());

                // 验证学习记录保存被调用
                verify(studyRecordRepository).save(any(StudyRecord.class));
                // 验证每日进度服务被调用（但失败）
                verify(dailyProgressService).updateDailyProgress(100L, LocalDate.now(), false);
        }
}
