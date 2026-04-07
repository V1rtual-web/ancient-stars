package com.example.ancientstars.service;

import com.example.ancientstars.dto.*;
import com.example.ancientstars.entity.*;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试记录服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class TestRecordServiceTest {

    @Mock
    private TestRecordRepository testRecordRepository;

    @Mock
    private TestDetailRepository testDetailRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private WordListVocabularyRepository wordListVocabularyRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @InjectMocks
    private TestRecordService testRecordService;

    private Task task;
    private List<Vocabulary> vocabularies;
    private List<WordListVocabulary> wordListVocabularies;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        task = new Task();
        task.setId(1L);
        task.setTitle("测试任务");
        task.setWordListId(1L);

        // 创建词汇
        vocabularies = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Vocabulary vocab = new Vocabulary();
            vocab.setId((long) i);
            vocab.setWord("word" + i);
            vocab.setTranslation("释义" + i);
            vocab.setPhonetic("/wɜːd" + i + "/");
            vocabularies.add(vocab);
        }

        // 创建词汇表关联
        wordListVocabularies = new ArrayList<>();
        for (int i = 0; i < vocabularies.size(); i++) {
            WordListVocabulary wlv = new WordListVocabulary();
            wlv.setId((long) (i + 1));
            wlv.setWordListId(1L);
            wlv.setVocabularyId(vocabularies.get(i).getId());
            wlv.setSortOrder(i);
            wordListVocabularies.add(wlv);
        }
    }

    @Test
    void testGenerateTest_Success() {
        // 准备请求
        TestGenerateRequest request = new TestGenerateRequest();
        request.setTaskId(1L);
        request.setQuestionCount(3);
        request.setTestType(TestRecord.TestType.CHOICE);

        // Mock行为
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(wordListVocabularyRepository.findByWordListIdOrderBySortOrder(1L))
                .thenReturn(wordListVocabularies);
        when(vocabularyRepository.findAllById(anyList())).thenReturn(vocabularies);

        // 执行测试
        List<TestQuestionDTO> questions = testRecordService.generateTest(request);

        // 验证结果
        assertNotNull(questions);
        assertEquals(3, questions.size());
        
        // 验证每个题目都有必要的信息
        for (TestQuestionDTO question : questions) {
            assertNotNull(question.getVocabularyId());
            assertNotNull(question.getQuestionText());
            assertNotNull(question.getCorrectAnswer());
            assertEquals(TestRecord.TestType.CHOICE, question.getQuestionType());
            
            // 选择题应该有4个选项
            assertNotNull(question.getOptions());
            assertEquals(4, question.getOptions().size());
        }

        // 验证调用
        verify(taskRepository).findById(1L);
        verify(wordListVocabularyRepository).findByWordListIdOrderBySortOrder(1L);
        verify(vocabularyRepository).findAllById(anyList());
    }

@Test
    void testGenerateTest_TaskNotFound() {
        // 准备请求
        TestGenerateRequest request = new TestGenerateRequest();
        request.setTaskId(999L);
        request.setQuestionCount(3);
        request.setTestType(TestRecord.TestType.CHOICE);

        // Mock行为
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testRecordService.generateTest(request);
        });

        assertEquals("任务不存在", exception.getMessage());
    }

    @Test
    void testGenerateTest_EmptyWordList() {
        // 准备请求
        TestGenerateRequest request = new TestGenerateRequest();
        request.setTaskId(1L);
        request.setQuestionCount(3);
        request.setTestType(TestRecord.TestType.CHOICE);

        // Mock行为
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(wordListVocabularyRepository.findByWordListIdOrderBySortOrder(1L))
                .thenReturn(Collections.emptyList());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testRecordService.generateTest(request);
        });

        assertEquals("该任务的词汇表为空", exception.getMessage());
    }

    @Test
    void testSubmitTest_Success() {
        // 准备请求
        TestSubmitRequest request = new TestSubmitRequest();
        request.setStudentId(1L);
        request.setTaskId(1L);
        request.setDurationSeconds(300);

        List<TestSubmitRequest.AnswerDTO> answers = new ArrayList<>();
        // 2个正确答案，1个错误答案
        answers.add(new TestSubmitRequest.AnswerDTO(1L, "word1"));
        answers.add(new TestSubmitRequest.AnswerDTO(2L, "word2"));
        answers.add(new TestSubmitRequest.AnswerDTO(3L, "wrong"));
        request.setAnswers(answers);

        // Mock行为
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(wordListVocabularyRepository.findByWordListIdOrderBySortOrder(1L))
                .thenReturn(wordListVocabularies);
        when(vocabularyRepository.findAllById(anyList())).thenReturn(vocabularies);

        TestRecord savedRecord = new TestRecord();
        savedRecord.setId(1L);
        savedRecord.setStudentId(1L);
        savedRecord.setTaskId(1L);
        savedRecord.setTotalQuestions(3);
        savedRecord.setCorrectAnswers(2);
        savedRecord.setScore(new BigDecimal("66.67"));
        savedRecord.setTestType(TestRecord.TestType.MIXED);
        savedRecord.setDurationSeconds(300);

        when(testRecordRepository.save(any(TestRecord.class))).thenReturn(savedRecord);
        when(testDetailRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // 执行测试
        TestResultDTO result = testRecordService.submitTest(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getTestRecordId());
        assertEquals(3, result.getTotalQuestions());
        assertEquals(2, result.getCorrectAnswers());
        assertEquals(new BigDecimal("66.67"), result.getScore());
        assertEquals(new BigDecimal("66.67"), result.getAccuracy());
        assertEquals(1, result.getWrongQuestions().size());

        // 验证调用
        verify(testRecordRepository, times(2)).save(any(TestRecord.class));
        verify(testDetailRepository).saveAll(anyList());
    }

    @Test
    void testSubmitTest_AllCorrect() {
        // 准备请求 - 所有答案都正确
        TestSubmitRequest request = new TestSubmitRequest();
        request.setStudentId(1L);
        request.setTaskId(1L);
        request.setDurationSeconds(300);

        List<TestSubmitRequest.AnswerDTO> answers = new ArrayList<>();
        answers.add(new TestSubmitRequest.AnswerDTO(1L, "word1"));
        answers.add(new TestSubmitRequest.AnswerDTO(2L, "word2"));
        request.setAnswers(answers);

        // Mock行为
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(wordListVocabularyRepository.findByWordListIdOrderBySortOrder(1L))
                .thenReturn(wordListVocabularies);
        when(vocabularyRepository.findAllById(anyList())).thenReturn(vocabularies);

        TestRecord savedRecord = new TestRecord();
        savedRecord.setId(1L);
        savedRecord.setStudentId(1L);
        savedRecord.setTaskId(1L);
        savedRecord.setTotalQuestions(2);
        savedRecord.setCorrectAnswers(2);
        savedRecord.setScore(new BigDecimal("100.00"));
        savedRecord.setTestType(TestRecord.TestType.MIXED);

        when(testRecordRepository.save(any(TestRecord.class))).thenReturn(savedRecord);
        when(testDetailRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // 执行测试
        TestResultDTO result = testRecordService.submitTest(request);

        // 验证结果 - 满分
        assertEquals(new BigDecimal("100.00"), result.getScore());
        assertEquals(new BigDecimal("100.00"), result.getAccuracy());
        assertEquals(0, result.getWrongQuestions().size());
    }

    @Test
    void testGetTestHistory_Success() {
        // 准备数据
        TestRecord record1 = new TestRecord();
        record1.setId(1L);
        record1.setStudentId(1L);
        record1.setTaskId(1L);
        record1.setScore(new BigDecimal("80.00"));
        record1.setTotalQuestions(10);
        record1.setCorrectAnswers(8);
        record1.setTestType(TestRecord.TestType.CHOICE);

        TestRecord record2 = new TestRecord();
        record2.setId(2L);
        record2.setStudentId(1L);
        record2.setTaskId(1L);
        record2.setScore(new BigDecimal("90.00"));
        record2.setTotalQuestions(10);
        record2.setCorrectAnswers(9);
        record2.setTestType(TestRecord.TestType.FILL_BLANK);

        List<TestRecord> records = Arrays.asList(record1, record2);
        Page<TestRecord> page = new PageImpl<>(records, PageRequest.of(0, 10), 2);

        // Mock行为
        when(testRecordRepository.findByStudentIdOrderByCreatedAtDesc(eq(1L), any()))
                .thenReturn(page);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // 执行测试
        Page<TestHistoryDTO> result = testRecordService.getTestHistory(1L, 0, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());

        TestHistoryDTO dto1 = result.getContent().get(0);
        assertEquals(1L, dto1.getId());
        assertEquals(new BigDecimal("80.00"), dto1.getScore());
        assertEquals(10, dto1.getTotalQuestions());
        assertEquals(8, dto1.getCorrectAnswers());

        // 验证调用
        verify(testRecordRepository).findByStudentIdOrderByCreatedAtDesc(eq(1L), any());
    }

    @Test
    void testGetTestDetails_Success() {
        // 准备数据
        TestRecord testRecord = new TestRecord();
        testRecord.setId(1L);
        testRecord.setStudentId(1L);
        testRecord.setTaskId(1L);
        testRecord.setScore(new BigDecimal("75.00"));
        testRecord.setTotalQuestions(4);
        testRecord.setCorrectAnswers(3);

        TestDetail wrongDetail = new TestDetail();
        wrongDetail.setId(1L);
        wrongDetail.setTestRecordId(1L);
        wrongDetail.setVocabularyId(1L);
        wrongDetail.setQuestionText("单词: word1");
        wrongDetail.setStudentAnswer("wrong");
        wrongDetail.setCorrectAnswer("word1 / 释义1");
        wrongDetail.setIsCorrect(false);

        // Mock行为
        when(testRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
        when(testDetailRepository.findByTestRecordIdAndIsCorrectFalse(1L))
                .thenReturn(Collections.singletonList(wrongDetail));
        when(vocabularyRepository.findById(1L)).thenReturn(Optional.of(vocabularies.get(0)));

        // 执行测试
        TestResultDTO result = testRecordService.getTestDetails(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getTestRecordId());
        assertEquals(new BigDecimal("75.00"), result.getScore());
        assertEquals(4, result.getTotalQuestions());
        assertEquals(3, result.getCorrectAnswers());
        assertEquals(1, result.getWrongQuestions().size());

        TestResultDTO.WrongQuestionDTO wrongQuestion = result.getWrongQuestions().get(0);
        assertEquals(1L, wrongQuestion.getVocabularyId());
        assertEquals("word1", wrongQuestion.getWord());
        assertEquals("wrong", wrongQuestion.getStudentAnswer());

        // 验证调用
        verify(testRecordRepository).findById(1L);
        verify(testDetailRepository).findByTestRecordIdAndIsCorrectFalse(1L);
    }

    @Test
    void testGetTestDetails_NotFound() {
        // Mock行为
        when(testRecordRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testRecordService.getTestDetails(999L);
        });

        assertEquals("测试记录不存在", exception.getMessage());
    }
}
