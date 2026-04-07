package com.example.ancientstars.service;

import com.example.ancientstars.dto.LearningRecordDTO;
import com.example.ancientstars.entity.LearningRecord;
import com.example.ancientstars.entity.LearningRecord.MasteryLevel;
import com.example.ancientstars.entity.Task;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.entity.Vocabulary;
import com.example.ancientstars.repository.LearningRecordRepository;
import com.example.ancientstars.repository.TaskRepository;
import com.example.ancientstars.repository.UserRepository;
import com.example.ancientstars.repository.VocabularyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 学习记录服务单元测试
 * 验证需求: 5.1, 5.4
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("学习记录服务测试")
class LearningRecordServiceTest {

    @Mock
    private LearningRecordRepository learningRecordRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LearningRecordService learningRecordService;

    private User testStudent;
    private Vocabulary testVocabulary;
    private Task testTask;
    private LearningRecord testRecord;

    @BeforeEach
    void setUp() {
        // 创建测试学生
        testStudent = new User();
        testStudent.setId(1L);
        testStudent.setUsername("student1");
        testStudent.setRole(User.Role.STUDENT);

        // 创建测试词汇
        testVocabulary = new Vocabulary();
        testVocabulary.setId(1L);
        testVocabulary.setWord("hello");
        testVocabulary.setPhonetic("/həˈloʊ/");
        testVocabulary.setDefinition("A greeting");
        testVocabulary.setTranslation("你好");
        testVocabulary.setExample("Hello, world!");

        // 创建测试任务
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");

        // 创建测试学习记录
        testRecord = new LearningRecord();
        testRecord.setId(1L);
        testRecord.setStudentId(1L);
        testRecord.setVocabularyId(1L);
        testRecord.setTaskId(1L);
        testRecord.setMasteryLevel(MasteryLevel.LEARNING);
        testRecord.setReviewCount(1);
        testRecord.setLastReviewedAt(LocalDateTime.now());
        testRecord.setCreatedAt(LocalDateTime.now());
        testRecord.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== 标记掌握度测试 ====================

    @Test
    @DisplayName("标记新词汇掌握度 - 创建新记录 - 验证需求 5.1")
    void testMarkMasteryCreateNewRecord() {
        // 模拟行为
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(testVocabulary));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(learningRecordRepository.findByStudentIdAndVocabularyIdAndTaskId(1L, 1L, 1L))
                .thenReturn(Optional.empty());
        when(learningRecordRepository.save(any(LearningRecord.class))).thenReturn(testRecord);

        // 执行测试
        LearningRecord result = learningRecordService.markMastery(1L, 1L, 1L, MasteryLevel.LEARNING);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getStudentId());
        assertEquals(1L, result.getVocabularyId());
        assertEquals(1L, result.getTaskId());
        assertEquals(MasteryLevel.LEARNING, result.getMasteryLevel());

        // 验证方法调用
        verify(userRepository).findById(1L);
        verify(vocabularyRepository).findById(1L);
        verify(taskRepository).findById(1L);
        verify(learningRecordRepository).findByStudentIdAndVocabularyIdAndTaskId(1L, 1L, 1L);
        verify(learningRecordRepository).save(any(LearningRecord.class));
    }

    @Test
    @DisplayName("标记已有词汇掌握度 - 更新现有记录 - 验证需求 5.1")
    void testMarkMasteryUpdateExistingRecord() {
        // 模拟行为
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(testVocabulary));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(learningRecordRepository.findByStudentIdAndVocabularyIdAndTaskId(1L, 1L, 1L))
                .thenReturn(Optional.of(testRecord));
        when(learningRecordRepository.save(any(LearningRecord.class))).thenReturn(testRecord);

        // 执行测试
        LearningRecord result = learningRecordService.markMastery(1L, 1L, 1L, MasteryLevel.MASTERED);

        // 验证结果
        assertNotNull(result);
        assertEquals(MasteryLevel.MASTERED, result.getMasteryLevel());
        assertEquals(2, result.getReviewCount()); // 复习次数应该增加

        // 验证方法调用
        verify(learningRecordRepository).findByStudentIdAndVocabularyIdAndTaskId(1L, 1L, 1L);
        verify(learningRecordRepository).save(any(LearningRecord.class));
    }

    @Test
    @DisplayName("标记掌握度 - 学生不存在 - 抛出异常")
    void testMarkMasteryStudentNotFound() {
        // 模拟行为
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            learningRecordService.markMastery(1L, 1L, 1L, MasteryLevel.LEARNING);
        });

        assertTrue(exception.getMessage().contains("Student not found"));
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("标记掌握度 - 词汇不存在 - 抛出异常")
    void testMarkMasteryVocabularyNotFound() {
        // 模拟行为
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            learningRecordService.markMastery(1L, 1L, 1L, MasteryLevel.LEARNING);
        });

        assertTrue(exception.getMessage().contains("Vocabulary not found"));
        verify(vocabularyRepository).findById(1L);
    }

// ==================== 查询学习记录测试 ====================

@Test
    @DisplayName("获取学生学习记录（分页）- 验证需求 5.4")
    void testGetStudentLearningRecords() {
        // 准备测试数据
        List<LearningRecord> records = Arrays.asList(testRecord);
        Page<LearningRecord> page = new PageImpl<>(records);
        Pageable pageable = PageRequest.of(0, 20);

        // 模拟行为
        when(learningRecordRepository.findByStudentId(1L, pageable)).thenReturn(page);
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(testVocabulary));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // 执行测试
        Page<LearningRecordDTO> result = learningRecordService.getStudentLearningRecords(1L, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("hello", result.getContent().get(0).getWord());

        // 验证方法调用
        verify(learningRecordRepository).findByStudentId(1L, pageable);
    }

    @Test
    @DisplayName("获取学生任务学习记录 - 验证需求 5.4")
    void testGetStudentTaskRecords() {
        // 准备测试数据
        List<LearningRecord> records = Arrays.asList(testRecord);

        // 模拟行为
        when(learningRecordRepository.findByStudentIdAndTaskId(1L, 1L)).thenReturn(records);
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(testVocabulary));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // 执行测试
        List<LearningRecordDTO> result = learningRecordService.getStudentTaskRecords(1L, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("hello", result.get(0).getWord());
        assertEquals("Test Task", result.get(0).getTaskTitle());

        // 验证方法调用
        verify(learningRecordRepository).findByStudentIdAndTaskId(1L, 1L);
    }

    @Test
    @DisplayName("按掌握度查询学习记录 - 验证需求 5.4")
    void testGetStudentRecordsByMastery() {
        // 准备测试数据
        testRecord.setMasteryLevel(MasteryLevel.MASTERED);
        List<LearningRecord> records = Arrays.asList(testRecord);

        // 模拟行为
        when(learningRecordRepository.findByStudentIdAndMasteryLevel(1L, MasteryLevel.MASTERED))
                .thenReturn(records);
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(testVocabulary));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // 执行测试
        List<LearningRecordDTO> result = learningRecordService.getStudentRecordsByMastery(1L, MasteryLevel.MASTERED);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(MasteryLevel.MASTERED, result.get(0).getMasteryLevel());

        // 验证方法调用
        verify(learningRecordRepository).findByStudentIdAndMasteryLevel(1L, MasteryLevel.MASTERED);
    }

    // ==================== 统计测试 ====================

    @Test
    @DisplayName("统计已掌握词汇数量 - 验证需求 5.4")
    void testCountMasteredVocabulary() {
        // 模拟行为
        when(learningRecordRepository.countByStudentIdAndMasteryLevel(1L, MasteryLevel.MASTERED))
                .thenReturn(10L);

        // 执行测试
        long count = learningRecordService.countMasteredVocabulary(1L);

        // 验证结果
        assertEquals(10L, count);

        // 验证方法调用
        verify(learningRecordRepository).countByStudentIdAndMasteryLevel(1L, MasteryLevel.MASTERED);
    }

    @Test
    @DisplayName("统计任务已掌握词汇数量 - 验证需求 5.4")
    void testCountTaskMasteredVocabulary() {
        // 模拟行为
        when(learningRecordRepository.countByStudentIdAndTaskIdAndMasteryLevel(1L, 1L, MasteryLevel.MASTERED))
                .thenReturn(5L);

        // 执行测试
        long count = learningRecordService.countTaskMasteredVocabulary(1L, 1L);

        // 验证结果
        assertEquals(5L, count);

        // 验证方法调用
        verify(learningRecordRepository).countByStudentIdAndTaskIdAndMasteryLevel(1L, 1L, MasteryLevel.MASTERED);
    }

    // ==================== 删除测试 ====================

    @Test
    @DisplayName("删除学习记录 - 验证需求 5.4")
    void testDeleteLearningRecord() {
        // 模拟行为
        doNothing().when(learningRecordRepository).deleteById(1L);

        // 执行测试
        learningRecordService.deleteLearningRecord(1L);

        // 验证方法调用
        verify(learningRecordRepository).deleteById(1L);
    }

    @Test
    @DisplayName("获取学习记录详情 - 验证需求 5.4")
    void testGetLearningRecordById() {
        // 模拟行为
        when(learningRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(testVocabulary));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // 执行测试
        LearningRecordDTO result = learningRecordService.getLearningRecordById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("hello", result.getWord());
        assertEquals("Test Task", result.getTaskTitle());

        // 验证方法调用
        verify(learningRecordRepository).findById(1L);
    }

    @Test
    @DisplayName("获取学习记录详情 - 记录不存在 - 抛出异常")
    void testGetLearningRecordByIdNotFound() {
        // 模拟行为
        when(learningRecordRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            learningRecordService.getLearningRecordById(1L);
        });

        assertTrue(exception.getMessage().contains("Learning record not found"));
        verify(learningRecordRepository).findById(1L);
    }
}
