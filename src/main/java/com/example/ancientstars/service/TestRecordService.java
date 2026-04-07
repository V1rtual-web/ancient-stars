package com.example.ancientstars.service;

import com.example.ancientstars.dto.*;
import com.example.ancientstars.entity.*;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试记录服务类
 */
@Service
@RequiredArgsConstructor
public class TestRecordService {

    private final TestRecordRepository testRecordRepository;
    private final TestDetailRepository testDetailRepository;
    private final TaskRepository taskRepository;
    private final WordListVocabularyRepository wordListVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;

    /**
     * 生成测试题目
     */
    public List<TestQuestionDTO> generateTest(TestGenerateRequest request) {
        // 验证任务是否存在
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new BusinessException("任务不存在"));

        // 获取任务关联的词汇表中的所有词汇
        List<WordListVocabulary> wordListVocabularies = wordListVocabularyRepository
                .findByWordListIdOrderBySortOrder(task.getWordListId());

        if (wordListVocabularies.isEmpty()) {
            throw new BusinessException("该任务的词汇表为空");
        }

        // 获取词汇详情
        List<Long> vocabularyIds = wordListVocabularies.stream()
                .map(WordListVocabulary::getVocabularyId)
                .collect(Collectors.toList());

        List<Vocabulary> vocabularies = vocabularyRepository.findAllById(vocabularyIds);

        if (vocabularies.isEmpty()) {
            throw new BusinessException("未找到有效的词汇");
        }

        // 随机打乱词汇顺序
        List<Vocabulary> shuffledVocabularies = new ArrayList<>(vocabularies);
        Collections.shuffle(shuffledVocabularies);

        // 限制题目数量
        int questionCount = Math.min(request.getQuestionCount(), shuffledVocabularies.size());
        List<Vocabulary> selectedVocabularies = shuffledVocabularies.subList(0, questionCount);

        // 根据题型生成题目
        List<TestQuestionDTO> questions = new ArrayList<>();
        for (Vocabulary vocab : selectedVocabularies) {
            TestQuestionDTO question = generateQuestion(vocab, request.getTestType(), vocabularies);
            questions.add(question);
        }

        return questions;
    }

    /**
     * 根据题型生成单个题目
     */
    private TestQuestionDTO generateQuestion(Vocabulary vocab, TestRecord.TestType testType, List<Vocabulary> allVocabularies) {
        TestQuestionDTO question = new TestQuestionDTO();
        question.setVocabularyId(vocab.getId());
        question.setQuestionType(testType);

        switch (testType) {
            case CHOICE:
                // 选择题：给出单词，选择正确的释义
                question.setQuestionText("请选择单词 \"" + vocab.getWord() + "\" 的正确释义：");
                question.setCorrectAnswer(vocab.getTranslation());
                question.setOptions(generateChoiceOptions(vocab, allVocabularies));
                break;

            case FILL_BLANK:
                // 填空题：给出释义，填写单词
                question.setQuestionText("请根据释义填写单词：" + vocab.getTranslation());
                question.setCorrectAnswer(vocab.getWord().toLowerCase());
                break;

            case TRANSLATION:
                // 翻译题：英译汉或汉译英（随机）
                if (new Random().nextBoolean()) {
                    // 英译汉
                    question.setQuestionText("请翻译单词：" + vocab.getWord());
                    question.setCorrectAnswer(vocab.getTranslation());
                } else {
                    // 汉译英
                    question.setQuestionText("请翻译：" + vocab.getTranslation());
                    question.setCorrectAnswer(vocab.getWord().toLowerCase());
                }
                break;

            default:
                throw new BusinessException("不支持的题型");
        }

        return question;
    }

    /**
     * 生成选择题的选项（4个选项）
     */
    private List<String> generateChoiceOptions(Vocabulary correctVocab, List<Vocabulary> allVocabularies) {
        List<String> options = new ArrayList<>();
        options.add(correctVocab.getTranslation());

        // 从其他词汇中随机选择3个作为干扰项
        List<Vocabulary> otherVocabularies = allVocabularies.stream()
                .filter(v -> !v.getId().equals(correctVocab.getId()))
                .collect(Collectors.toList());

        Collections.shuffle(otherVocabularies);

        int count = Math.min(3, otherVocabularies.size());
        for (int i = 0; i < count; i++) {
            options.add(otherVocabularies.get(i).getTranslation());
        }

        // 如果干扰项不足3个，补充一些通用选项
        while (options.size() < 4) {
            options.add("其他释义 " + options.size());
        }

        // 随机打乱选项顺序
        Collections.shuffle(options);

        return options;
    }

    /**
     * 提交测试并评分
     */
    @Transactional
    public TestResultDTO submitTest(TestSubmitRequest request) {
        // 验证任务是否存在
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new BusinessException("任务不存在"));

        // 获取任务关联的词汇
        List<WordListVocabulary> wordListVocabularies = wordListVocabularyRepository
                .findByWordListIdOrderBySortOrder(task.getWordListId());

        List<Long> vocabularyIds = wordListVocabularies.stream()
                .map(WordListVocabulary::getVocabularyId)
                .collect(Collectors.toList());

        List<Vocabulary> vocabularies = vocabularyRepository.findAllById(vocabularyIds);
        Map<Long, Vocabulary> vocabularyMap = vocabularies.stream()
                .collect(Collectors.toMap(Vocabulary::getId, v -> v));

        // 评分
        int totalQuestions = request.getAnswers().size();
        int correctAnswers = 0;
        List<TestDetail> testDetails = new ArrayList<>();
        List<TestResultDTO.WrongQuestionDTO> wrongQuestions = new ArrayList<>();

        // 先创建测试记录（需要ID来关联测试详情）
        TestRecord testRecord = new TestRecord();
        testRecord.setStudentId(request.getStudentId());
        testRecord.setTaskId(request.getTaskId());
        testRecord.setTotalQuestions(totalQuestions);
        testRecord.setDurationSeconds(request.getDurationSeconds());
        testRecord.setTestType(TestRecord.TestType.MIXED); // 默认混合题型
        testRecord.setCorrectAnswers(0); // 先设置为0，后面更新
        testRecord.setScore(BigDecimal.ZERO); // 先设置为0，后面更新

        testRecord = testRecordRepository.save(testRecord);

        // 评判每道题
        for (TestSubmitRequest.AnswerDTO answer : request.getAnswers()) {
            Vocabulary vocab = vocabularyMap.get(answer.getVocabularyId());
            if (vocab == null) {
                continue;
            }

            // 判断答案是否正确（忽略大小写和首尾空格）
            String studentAnswer = answer.getAnswer() != null ? answer.getAnswer().trim().toLowerCase() : "";
            String correctAnswer = vocab.getWord().trim().toLowerCase();
            String correctTranslation = vocab.getTranslation().trim();

            boolean isCorrect = studentAnswer.equals(correctAnswer) || studentAnswer.equals(correctTranslation.toLowerCase());

            if (isCorrect) {
                correctAnswers++;
            } else {
                // 记录错题
                TestResultDTO.WrongQuestionDTO wrongQuestion = new TestResultDTO.WrongQuestionDTO();
                wrongQuestion.setVocabularyId(vocab.getId());
                wrongQuestion.setWord(vocab.getWord());
                wrongQuestion.setQuestionText("单词: " + vocab.getWord() + " - 释义: " + vocab.getTranslation());
                wrongQuestion.setStudentAnswer(answer.getAnswer());
                wrongQuestion.setCorrectAnswer(vocab.getWord() + " / " + vocab.getTranslation());
                wrongQuestions.add(wrongQuestion);
            }

            // 保存测试详情
            TestDetail testDetail = new TestDetail();
            testDetail.setTestRecordId(testRecord.getId());
            testDetail.setVocabularyId(vocab.getId());
            testDetail.setQuestionText("单词: " + vocab.getWord());
            testDetail.setStudentAnswer(answer.getAnswer());
            testDetail.setCorrectAnswer(vocab.getWord() + " / " + vocab.getTranslation());
            testDetail.setIsCorrect(isCorrect);
            testDetails.add(testDetail);
        }

        // 批量保存测试详情
        testDetailRepository.saveAll(testDetails);

        // 计算分数：score = (correct_answers / total_questions) × 100
        BigDecimal score = BigDecimal.valueOf(correctAnswers)
                .divide(BigDecimal.valueOf(totalQuestions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // 计算正确率
        BigDecimal accuracy = BigDecimal.valueOf(correctAnswers)
                .divide(BigDecimal.valueOf(totalQuestions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // 更新测试记录
        testRecord.setCorrectAnswers(correctAnswers);
        testRecord.setScore(score);
        testRecordRepository.save(testRecord);

        // 构建返回结果
        TestResultDTO result = new TestResultDTO();
        result.setTestRecordId(testRecord.getId());
        result.setScore(score);
        result.setTotalQuestions(totalQuestions);
        result.setCorrectAnswers(correctAnswers);
        result.setAccuracy(accuracy);
        result.setWrongQuestions(wrongQuestions);

        return result;
    }

    /**
     * 查询历史成绩
     */
    public Page<TestHistoryDTO> getTestHistory(Long studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TestRecord> testRecords = testRecordRepository.findByStudentIdOrderByCreatedAtDesc(studentId, pageable);

        return testRecords.map(record -> {
            TestHistoryDTO dto = new TestHistoryDTO();
            dto.setId(record.getId());
            dto.setTaskId(record.getTaskId());

            // 获取任务标题
            if (record.getTaskId() != null) {
                taskRepository.findById(record.getTaskId())
                        .ifPresent(task -> dto.setTaskTitle(task.getTitle()));
            }

            dto.setScore(record.getScore());
            dto.setTotalQuestions(record.getTotalQuestions());
            dto.setCorrectAnswers(record.getCorrectAnswers());

            // 计算正确率
            BigDecimal accuracy = BigDecimal.valueOf(record.getCorrectAnswers())
                    .divide(BigDecimal.valueOf(record.getTotalQuestions()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            dto.setAccuracy(accuracy);

            dto.setDurationSeconds(record.getDurationSeconds());
            dto.setTestType(record.getTestType());
            dto.setCreatedAt(record.getCreatedAt());

            return dto;
        });
    }

    /**
     * 查询测试详情（包括错题）
     */
    public TestResultDTO getTestDetails(Long testId) {
        // 查询测试记录
        TestRecord testRecord = testRecordRepository.findById(testId)
                .orElseThrow(() -> new BusinessException("测试记录不存在"));

        // 查询错题
        List<TestDetail> wrongDetails = testDetailRepository.findByTestRecordIdAndIsCorrectFalse(testId);

        List<TestResultDTO.WrongQuestionDTO> wrongQuestions = wrongDetails.stream()
                .map(detail -> {
                    TestResultDTO.WrongQuestionDTO wrongQuestion = new TestResultDTO.WrongQuestionDTO();
                    wrongQuestion.setVocabularyId(detail.getVocabularyId());

                    // 获取词汇信息
                    vocabularyRepository.findById(detail.getVocabularyId())
                            .ifPresent(vocab -> wrongQuestion.setWord(vocab.getWord()));

                    wrongQuestion.setQuestionText(detail.getQuestionText());
                    wrongQuestion.setStudentAnswer(detail.getStudentAnswer());
                    wrongQuestion.setCorrectAnswer(detail.getCorrectAnswer());

                    return wrongQuestion;
                })
                .collect(Collectors.toList());

        // 计算正确率
        BigDecimal accuracy = BigDecimal.valueOf(testRecord.getCorrectAnswers())
                .divide(BigDecimal.valueOf(testRecord.getTotalQuestions()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // 构建返回结果
        TestResultDTO result = new TestResultDTO();
        result.setTestRecordId(testRecord.getId());
        result.setScore(testRecord.getScore());
        result.setTotalQuestions(testRecord.getTotalQuestions());
        result.setCorrectAnswers(testRecord.getCorrectAnswers());
        result.setAccuracy(accuracy);
        result.setWrongQuestions(wrongQuestions);

        return result;
    }
}
