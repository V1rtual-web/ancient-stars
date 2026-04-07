package com.example.ancientstars.controller;

import com.example.ancientstars.dto.*;
import com.example.ancientstars.entity.*;
import com.example.ancientstars.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试控制器集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private WordListRepository wordListRepository;

    @Autowired
    private WordListVocabularyRepository wordListVocabularyRepository;

    @Autowired
    private TaskRepository taskRepository;

    private User student;
    private Task task;
    private List<Vocabulary> vocabularies;

    @BeforeEach
    void setUp() {
        // 创建测试学生
        student = new User();
        student.setUsername("teststudent");
        student.setPassword("password");
        student.setRealName("测试学生");
        student.setRole(User.Role.STUDENT);
        student.setStatus(User.Status.ACTIVE);
        student = userRepository.save(student);

        // 创建测试词汇
        vocabularies = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Vocabulary vocab = new Vocabulary();
            vocab.setWord("testword" + i);
            vocab.setTranslation("测试释义" + i);
            vocab.setPhonetic("/test" + i + "/");
            vocab.setDeleted(false);
            vocabularies.add(vocabularyRepository.save(vocab));
        }

        // 创建词汇表
        WordList wordList = new WordList();
        wordList.setName("测试词汇表");
        wordList.setDescription("用于测试");
        wordList.setCreatorId(1L);
        wordList = wordListRepository.save(wordList);

        // 关联词汇到词汇表
        for (int i = 0; i < vocabularies.size(); i++) {
            WordListVocabulary wlv = new WordListVocabulary();
            wlv.setWordListId(wordList.getId());
            wlv.setVocabularyId(vocabularies.get(i).getId());
            wlv.setSortOrder(i);
            wordListVocabularyRepository.save(wlv);
        }

        // 创建测试任务
        task = new Task();
        task.setTitle("测试任务");
        task.setDescription("用于测试");
        task.setWordListId(wordList.getId());
        task.setCreatedBy(1L);
        task.setStatus(Task.TaskStatus.ACTIVE);
        task = taskRepository.save(task);
    }

    @Test
    @WithMockUser(username = "teststudent", roles = { "STUDENT" })
    void testGenerateTest() throws Exception {
        TestGenerateRequest request = new TestGenerateRequest();
        request.setTaskId(task.getId());
        request.setQuestionCount(3);
        request.setTestType(TestRecord.TestType.CHOICE);

        mockMvc.perform(post("/api/test/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @WithMockUser(username = "teststudent", roles = { "STUDENT" })
    void testSubmitTest() throws Exception {
        TestSubmitRequest request = new TestSubmitRequest();
        request.setStudentId(student.getId());
        request.setTaskId(task.getId());
        request.setDurationSeconds(300);

        List<TestSubmitRequest.AnswerDTO> answers = new ArrayList<>();
        answers.add(new TestSubmitRequest.AnswerDTO(vocabularies.get(0).getId(), "testword1"));
        answers.add(new TestSubmitRequest.AnswerDTO(vocabularies.get(1).getId(), "testword2"));
        answers.add(new TestSubmitRequest.AnswerDTO(vocabularies.get(2).getId(), "wrong"));
        request.setAnswers(answers);

        mockMvc.perform(post("/api/test/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalQuestions").value(3))
                .andExpect(jsonPath("$.data.correctAnswers").value(2))
                .andExpect(jsonPath("$.data.wrongQuestions").isArray())
                .andExpect(jsonPath("$.data.wrongQuestions.length()").value(1));
    }

    @Test
    @WithMockUser(username = "teststudent", roles = { "STUDENT" })
    void testGetTestHistory() throws Exception {
        mockMvc.perform(get("/api/test/history/" + student.getId())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
