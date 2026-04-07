package com.example.ancientstars.controller;

import com.example.ancientstars.dto.CreateStudyRecordRequest;
import com.example.ancientstars.entity.*;
import com.example.ancientstars.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 学生端 API 集成测试
 * 验证需求: 8.6, 8.7, 8.8
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("学生端 API 集成测试")
class StudentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private StudyRecordRepository studyRecordRepository;

    @Autowired
    private ReviewReminderRepository reviewReminderRepository;

    @Autowired
    private DailyProgressRepository dailyProgressRepository;

    private Vocabulary testVocabulary;
    private Long testStudentId = 1L;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        reviewReminderRepository.deleteAll();
        studyRecordRepository.deleteAll();
        dailyProgressRepository.deleteAll();
        vocabularyRepository.deleteAll();

        // 创建测试词汇
        testVocabulary = new Vocabulary();
        testVocabulary.setWord("test");
        testVocabulary.setPhonetic("/test/");
        testVocabulary.setDefinition("A test word");
        testVocabulary.setTranslation("测试");
        testVocabulary.setExample("This is a test.");
        testVocabulary.setDifficultyLevel(1);
        testVocabulary = vocabularyRepository.save(testVocabulary);
    }

    // ==================== 词汇浏览接口测试 ====================

    @Test
    @WithMockUser(username = "student", roles = { "STUDENT" })
    @DisplayName("GET /api/student/vocabularies - 成功获取词汇列表 - 验证需求 8.6")
    void testGetVocabularies_Success() throws Exception {
        mockMvc.perform(get("/api/student/vocabularies")
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20));
    }

    @Test
    @DisplayName("GET /api/student/vocabularies - 未认证访问 - 验证需求 8.7")
    void testGetVocabularies_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/student/vocabularies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "teacher", roles = { "TEACHER" })
    @DisplayName("GET /api/student/vocabularies - 教师角色访问 - 验证需求 8.7")
    void testGetVocabularies_Forbidden() throws Exception {
        mockMvc.perform(get("/api/student/vocabularies"))
                .andExpect(status().isForbidden());
    }

    // ==================== 学习记录接口测试 ====================

    @Test
    @WithMockUser(username = "student", roles = { "STUDENT" }, authorities = { "ROLE_STUDENT" })
    @DisplayName("POST /api/student/study-records - 成功创建学习记录 - 验证需求 8.6")
    void testCreateStudyRecord_Success() throws Exception {
        CreateStudyRecordRequest request = new CreateStudyRecordRequest(testVocabulary.getId());

        mockMvc.perform(post("/api/student/study-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.vocabularyId").value(testVocabulary.getId()))
                .andExpect(jsonPath("$.data.reviewCount").value(0))
                .andExpect(jsonPath("$.data.nextReviewTime").exists());
    }

    @Test
    @WithMockUser(username = "student", roles = { "STUDENT" }, authorities = { "ROLE_STUDENT" })
    @DisplayName("POST /api/student/study-records - 请求参数验证失败 - 验证需求 8.8")
    void testCreateStudyRecord_ValidationError() throws Exception {
        CreateStudyRecordRequest request = new CreateStudyRecordRequest(null);

        mockMvc.perform(post("/api/student/study-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/student/study-records - 未认证访问 - 验证需求 8.7")
    void testCreateStudyRecord_Unauthorized() throws Exception {
        CreateStudyRecordRequest request = new CreateStudyRecordRequest(testVocabulary.getId());

        mockMvc.perform(post("/api/student/study-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 复习提醒接口测试 ====================

    @Test
    @WithMockUser(username = "student", roles = { "STUDENT" })
    @DisplayName("GET /api/student/review-reminders - 成功获取待复习列表 - 验证需求 8.6")
    void testGetReviewReminders_Success() throws Exception {
        // 创建测试数据
        StudyRecord studyRecord = createTestStudyRecord();
        createTestReviewReminder(studyRecord);

        mockMvc.perform(get("/api/student/review-reminders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/student/review-reminders - 未认证访问 - 验证需求 8.7")
    void testGetReviewReminders_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/student/review-reminders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student", roles = { "STUDENT" }, authorities = { "ROLE_STUDENT" })
    @DisplayName("POST /api/student/review-reminders/{id}/complete - 成功完成复习 - 验证需求 8.6")
    void testCompleteReview_Success() throws Exception {
        // 创建测试数据
        StudyRecord studyRecord = createTestStudyRecord();
        ReviewReminder reminder = createTestReviewReminder(studyRecord);

        mockMvc.perform(post("/api/student/review-reminders/{id}/complete", reminder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("复习完成"))
                .andExpect(jsonPath("$.data.nextReviewTime").exists());
    }

    @Test
    @WithMockUser(username = "student", roles = { "STUDENT" }, authorities = { "ROLE_STUDENT" })
    @DisplayName("POST /api/student/review-reminders/{id}/complete - 提醒不存在 - 验证需求 8.8")
    void testCompleteReview_NotFound() throws Exception {
        mockMvc.perform(post("/api/student/review-reminders/{id}/complete", 99999L))
                .andExpect(status().isInternalServerError());
    }

    // ==================== 学习进度接口测试 ====================

    @Test
    @WithMockUser(username = "student", roles = { "STUDENT" })
    @DisplayName("GET /api/student/progress - 成功获取学习进度 - 验证需求 8.6")
    void testGetProgress_Success() throws Exception {
        mockMvc.perform(get("/api/student/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.todayNewWords").isNumber())
                .andExpect(jsonPath("$.data.todayReviewWords").isNumber())
                .andExpect(jsonPath("$.data.weekNewWords").isNumber())
                .andExpect(jsonPath("$.data.totalLearnedWords").isNumber())
                .andExpect(jsonPath("$.data.dailyProgress").isArray());
    }

    @Test
    @WithMockUser(username = "student", roles = { "STUDENT" })
    @DisplayName("GET /api/student/progress - 带日期范围参数 - 验证需求 8.6")
    void testGetProgress_WithDateRange() throws Exception {
        mockMvc.perform(get("/api/student/progress")
                .param("startDate", "2026-03-01")
                .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /api/student/progress - 未认证访问 - 验证需求 8.7")
    void testGetProgress_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/student/progress"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 统一响应格式测试 ====================

    @Test
    @WithMockUser(username = "student", roles = { "STUDENT" })
    @DisplayName("所有接口返回统一JSON格式 - 验证需求 8.6")
    void testUnifiedResponseFormat() throws Exception {
        // 测试词汇列表接口
        mockMvc.perform(get("/api/student/vocabularies"))
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        // 测试复习提醒接口
        mockMvc.perform(get("/api/student/review-reminders"))
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        // 测试进度接口
        mockMvc.perform(get("/api/student/progress"))
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== 辅助方法 ====================

    private StudyRecord createTestStudyRecord() {
        StudyRecord record = new StudyRecord();
        record.setStudentId(testStudentId);
        record.setVocabularyId(testVocabulary.getId());
        record.setStudyTime(LocalDateTime.now().minusDays(7));
        record.setReviewCount(1);
        record.setLastReviewTime(LocalDateTime.now().minusDays(1));
        record.setNextReviewTime(LocalDateTime.now());
        record.setMasteryLevel(StudyRecord.MasteryLevel.LEARNING);
        return studyRecordRepository.save(record);
    }

    private ReviewReminder createTestReviewReminder(StudyRecord studyRecord) {
        ReviewReminder reminder = new ReviewReminder();
        reminder.setStudentId(testStudentId);
        reminder.setVocabularyId(testVocabulary.getId());
        reminder.setStudyRecordId(studyRecord.getId());
        reminder.setRemindTime(LocalDateTime.now());
        reminder.setStatus(ReviewReminder.Status.PENDING);
        return reviewReminderRepository.save(reminder);
    }
}
