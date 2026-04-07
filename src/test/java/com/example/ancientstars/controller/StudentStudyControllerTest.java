package com.example.ancientstars.controller;

import com.example.ancientstars.dto.ReviewReminderDTO;
import com.example.ancientstars.dto.StudentProgressDTO;
import com.example.ancientstars.dto.VocabularyDTO;
import com.example.ancientstars.entity.StudyRecord;
import com.example.ancientstars.security.CustomUserDetails;
import com.example.ancientstars.service.ReviewReminderService;
import com.example.ancientstars.service.StudyRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 学生学习记录控制器测试
 */
@WebMvcTest(StudentStudyController.class)
@AutoConfigureMockMvc
@DisplayName("学生学习记录控制器测试")
class StudentStudyControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private StudyRecordService studyRecordService;

        @MockBean
        private ReviewReminderService reviewReminderService;

        @MockBean
        private com.example.ancientstars.service.DailyProgressService dailyProgressService;

        @MockBean
        private com.example.ancientstars.security.JwtTokenProvider jwtTokenProvider;

        @MockBean
        private com.example.ancientstars.security.CustomUserDetailsService customUserDetailsService;

        private CustomUserDetails studentUser;
        private StudyRecord studyRecord;

        @BeforeEach
        void setUp() {
                // 创建测试用户
                studentUser = new CustomUserDetails(
                                10L,
                                "student1",
                                "password",
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")));

                // 创建测试学习记录
                studyRecord = new StudyRecord();
                studyRecord.setId(1L);
                studyRecord.setStudentId(10L);
                studyRecord.setVocabularyId(100L);
                studyRecord.setStudyTime(LocalDateTime.of(2026, 4, 1, 10, 30, 0));
                studyRecord.setReviewCount(0);
                studyRecord.setLastReviewTime(null);
                studyRecord.setNextReviewTime(LocalDateTime.of(2026, 4, 8, 10, 30, 0));
                studyRecord.setMasteryLevel(StudyRecord.MasteryLevel.NEW);
                studyRecord.setCreatedAt(LocalDateTime.of(2026, 4, 1, 10, 30, 0));
                studyRecord.setUpdatedAt(LocalDateTime.of(2026, 4, 1, 10, 30, 0));
        }

        @Test
        @DisplayName("创建学习记录 - 成功")
        @WithMockUser(roles = "STUDENT")
        void testCreateStudyRecordSuccess() throws Exception {
                // 准备请求数据
                Map<String, Object> request = new HashMap<>();
                request.put("vocabularyId", 100L);

                // 模拟服务层行为
                when(studyRecordService.createStudyRecord(eq(10L), eq(100L)))
                                .thenReturn(studyRecord);

                // 执行测试
                mockMvc.perform(post("/api/student/study-records")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())
                                .with(user(studentUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.studentId").value(10))
                                .andExpect(jsonPath("$.data.vocabularyId").value(100))
                                .andExpect(jsonPath("$.data.reviewCount").value(0))
                                .andExpect(jsonPath("$.data.masteryLevel").value("NEW"))
                                .andExpect(jsonPath("$.data.studyTime").exists())
                                .andExpect(jsonPath("$.data.nextReviewTime").exists());

                // 验证服务层方法被调用
                verify(studyRecordService).createStudyRecord(10L, 100L);
        }

        @Test
        @DisplayName("创建学习记录 - 幂等性测试（重复标记）")
        @WithMockUser(roles = "STUDENT")
        void testCreateStudyRecordIdempotency() throws Exception {
                // 准备请求数据
                Map<String, Object> request = new HashMap<>();
                request.put("vocabularyId", 100L);

                // 模拟服务层返回已更新的记录（复习次数增加）
                StudyRecord updatedRecord = new StudyRecord();
                updatedRecord.setId(1L);
                updatedRecord.setStudentId(10L);
                updatedRecord.setVocabularyId(100L);
                updatedRecord.setStudyTime(LocalDateTime.of(2026, 4, 1, 10, 30, 0));
                updatedRecord.setReviewCount(1);
                updatedRecord.setLastReviewTime(LocalDateTime.of(2026, 4, 2, 14, 0, 0));
                updatedRecord.setNextReviewTime(LocalDateTime.of(2026, 4, 4, 14, 0, 0));
                updatedRecord.setMasteryLevel(StudyRecord.MasteryLevel.LEARNING);

                when(studyRecordService.createStudyRecord(eq(10L), eq(100L)))
                                .thenReturn(updatedRecord);

                // 执行测试
                mockMvc.perform(post("/api/student/study-records")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())
                                .with(user(studentUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.reviewCount").value(1))
                                .andExpect(jsonPath("$.data.masteryLevel").value("LEARNING"));

                // 验证服务层方法被调用
                verify(studyRecordService).createStudyRecord(10L, 100L);
        }

        @Test
        @DisplayName("创建学习记录 - 缺少词汇ID")
        @WithMockUser(roles = "STUDENT")
        void testCreateStudyRecordMissingVocabularyId() throws Exception {
                // 准备请求数据（缺少vocabularyId）
                Map<String, Object> request = new HashMap<>();

                // 执行测试
                mockMvc.perform(post("/api/student/study-records")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())
                                .with(user(studentUser)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("创建学习记录 - 词汇ID为null")
        @WithMockUser(roles = "STUDENT")
        void testCreateStudyRecordNullVocabularyId() throws Exception {
                // 准备请求数据（vocabularyId为null）
                Map<String, Object> request = new HashMap<>();
                request.put("vocabularyId", null);

                // 执行测试
                mockMvc.perform(post("/api/student/study-records")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())
                                .with(user(studentUser)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("创建学习记录 - 无效的JSON格式")
        @WithMockUser(roles = "STUDENT")
        void testCreateStudyRecordInvalidJson() throws Exception {
                // 执行测试
                mockMvc.perform(post("/api/student/study-records")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json}")
                                .with(csrf())
                                .with(user(studentUser)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("创建学习记录 - 未授权（教师角色）")
        @WithMockUser(roles = "TEACHER")
        void testCreateStudyRecordUnauthorizedTeacher() throws Exception {
                // 准备请求数据
                Map<String, Object> request = new HashMap<>();
                request.put("vocabularyId", 100L);

                // 执行测试
                mockMvc.perform(post("/api/student/study-records")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("创建学习记录 - 未认证")
        void testCreateStudyRecordUnauthenticated() throws Exception {
                // 准备请求数据
                Map<String, Object> request = new HashMap<>();
                request.put("vocabularyId", 100L);

                // 执行测试
                mockMvc.perform(post("/api/student/study-records")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("创建学习记录 - 验证统一JSON响应格式")
        @WithMockUser(roles = "STUDENT")
        void testCreateStudyRecordResponseFormat() throws Exception {
                // 准备请求数据
                Map<String, Object> request = new HashMap<>();
                request.put("vocabularyId", 100L);

                // 模拟服务层行为
                when(studyRecordService.createStudyRecord(eq(10L), eq(100L)))
                                .thenReturn(studyRecord);

                // 执行测试并验证响应格式
                mockMvc.perform(post("/api/student/study-records")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())
                                .with(user(studentUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").exists())
                                .andExpect(jsonPath("$.data").exists())
                                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("获取待复习词汇列表 - 成功")
        @WithMockUser(roles = "STUDENT")
        void testGetReviewRemindersSuccess() throws Exception {
                // 准备测试数据
                VocabularyDTO vocabulary = new VocabularyDTO();
                vocabulary.setId(100L);
                vocabulary.setWord("apple");
                vocabulary.setPhonetic("/ˈæpl/");
                vocabulary.setTranslation("n. 苹果");

                ReviewReminderDTO reminder = new ReviewReminderDTO();
                reminder.setId(1L);
                reminder.setVocabulary(vocabulary);
                reminder.setRemindTime(LocalDateTime.of(2026, 4, 8, 8, 0, 0));
                reminder.setDaysSinceLastStudy(7L);

                List<ReviewReminderDTO> reminders = new ArrayList<>();
                reminders.add(reminder);

                // 模拟服务层行为
                when(reviewReminderService.getStudentRemindersWithDetails(eq(10L)))
                                .thenReturn(reminders);

                // 执行测试
                mockMvc.perform(get("/api/student/review-reminders")
                                .with(csrf())
                                .with(user(studentUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].id").value(1))
                                .andExpect(jsonPath("$.data[0].vocabulary.word").value("apple"))
                                .andExpect(jsonPath("$.data[0].daysSinceLastStudy").value(7));

                // 验证服务层方法被调用
                verify(reviewReminderService).getStudentRemindersWithDetails(10L);
        }

        @Test
        @DisplayName("获取待复习词汇列表 - 空列表")
        @WithMockUser(roles = "STUDENT")
        void testGetReviewRemindersEmpty() throws Exception {
                // 模拟服务层返回空列表
                when(reviewReminderService.getStudentRemindersWithDetails(eq(10L)))
                                .thenReturn(new ArrayList<>());

                // 执行测试
                mockMvc.perform(get("/api/student/review-reminders")
                                .with(csrf())
                                .with(user(studentUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data").isEmpty());

                // 验证服务层方法被调用
                verify(reviewReminderService).getStudentRemindersWithDetails(10L);
        }

        @Test
        @DisplayName("完成复习 - 成功")
        @WithMockUser(roles = "STUDENT")
        void testCompleteReviewSuccess() throws Exception {
                // 准备测试数据
                StudyRecord updatedRecord = new StudyRecord();
                updatedRecord.setId(1L);
                updatedRecord.setStudentId(10L);
                updatedRecord.setVocabularyId(100L);
                updatedRecord.setReviewCount(1);
                updatedRecord.setNextReviewTime(LocalDateTime.of(2026, 4, 10, 8, 0, 0));

                // 模拟服务层行为
                when(reviewReminderService.completeReview(eq(1L)))
                                .thenReturn(updatedRecord);

                // 执行测试
                mockMvc.perform(post("/api/student/review-reminders/1/complete")
                                .with(csrf())
                                .with(user(studentUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.message").value("复习完成"))
                                .andExpect(jsonPath("$.data.nextReviewTime").exists());

                // 验证服务层方法被调用
                verify(reviewReminderService).completeReview(1L);
        }

        @Test
        @DisplayName("完成复习 - 未授权（教师角色）")
        @WithMockUser(roles = "TEACHER")
        void testCompleteReviewUnauthorizedTeacher() throws Exception {
                // 执行测试
                mockMvc.perform(post("/api/student/review-reminders/1/complete")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("完成复习 - 未认证")
        void testCompleteReviewUnauthenticated() throws Exception {
                // 执行测试
                mockMvc.perform(post("/api/student/review-reminders/1/complete")
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("获取学生进度统计 - 成功（无日期参数）")
        @WithMockUser(roles = "STUDENT")
        void testGetStudentProgressSuccess() throws Exception {
                // 准备测试数据
                List<StudentProgressDTO.DailyProgressDetail> dailyProgress = new ArrayList<>();
                dailyProgress.add(StudentProgressDTO.DailyProgressDetail.builder()
                                .date(LocalDate.of(2026, 4, 1))
                                .newWords(15)
                                .reviewWords(8)
                                .build());
                dailyProgress.add(StudentProgressDTO.DailyProgressDetail.builder()
                                .date(LocalDate.of(2026, 3, 31))
                                .newWords(12)
                                .reviewWords(5)
                                .build());

                StudentProgressDTO progressDTO = StudentProgressDTO.builder()
                                .todayNewWords(15)
                                .todayReviewWords(8)
                                .weekNewWords(85)
                                .totalLearnedWords(320)
                                .dailyProgress(dailyProgress)
                                .build();

                // 模拟服务层行为
                when(dailyProgressService.getStudentProgress(eq(10L), eq(null), eq(null)))
                                .thenReturn(progressDTO);

                // 执行测试
                mockMvc.perform(get("/api/student/progress")
                                .with(csrf())
                                .with(user(studentUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.todayNewWords").value(15))
                                .andExpect(jsonPath("$.data.todayReviewWords").value(8))
                                .andExpect(jsonPath("$.data.weekNewWords").value(85))
                                .andExpect(jsonPath("$.data.totalLearnedWords").value(320))
                                .andExpect(jsonPath("$.data.dailyProgress").isArray())
                                .andExpect(jsonPath("$.data.dailyProgress[0].date").value("2026-04-01"))
                                .andExpect(jsonPath("$.data.dailyProgress[0].newWords").value(15))
                                .andExpect(jsonPath("$.data.dailyProgress[0].reviewWords").value(8));

                // 验证服务层方法被调用
                verify(dailyProgressService).getStudentProgress(10L, null, null);
        }

        @Test
        @DisplayName("获取学生进度统计 - 成功（带日期范围参数）")
        @WithMockUser(roles = "STUDENT")
        void testGetStudentProgressWithDateRange() throws Exception {
                // 准备测试数据
                LocalDate startDate = LocalDate.of(2026, 3, 1);
                LocalDate endDate = LocalDate.of(2026, 3, 31);

                List<StudentProgressDTO.DailyProgressDetail> dailyProgress = new ArrayList<>();
                dailyProgress.add(StudentProgressDTO.DailyProgressDetail.builder()
                                .date(LocalDate.of(2026, 3, 15))
                                .newWords(10)
                                .reviewWords(5)
                                .build());

                StudentProgressDTO progressDTO = StudentProgressDTO.builder()
                                .todayNewWords(0)
                                .todayReviewWords(0)
                                .weekNewWords(0)
                                .totalLearnedWords(320)
                                .dailyProgress(dailyProgress)
                                .build();

                // 模拟服务层行为
                when(dailyProgressService.getStudentProgress(eq(10L), eq(startDate), eq(endDate)))
                                .thenReturn(progressDTO);

                // 执行测试
                mockMvc.perform(get("/api/student/progress")
                                .param("startDate", "2026-03-01")
                                .param("endDate", "2026-03-31")
                                .with(csrf())
                                .with(user(studentUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.totalLearnedWords").value(320))
                                .andExpect(jsonPath("$.data.dailyProgress").isArray())
                                .andExpect(jsonPath("$.data.dailyProgress[0].date").value("2026-03-15"));

                // 验证服务层方法被调用
                verify(dailyProgressService).getStudentProgress(10L, startDate, endDate);
        }

        @Test
        @DisplayName("获取学生进度统计 - 空数据")
        @WithMockUser(roles = "STUDENT")
        void testGetStudentProgressEmpty() throws Exception {
                // 准备测试数据（空进度）
                StudentProgressDTO progressDTO = StudentProgressDTO.builder()
                                .todayNewWords(0)
                                .todayReviewWords(0)
                                .weekNewWords(0)
                                .totalLearnedWords(0)
                                .dailyProgress(new ArrayList<>())
                                .build();

                // 模拟服务层行为
                when(dailyProgressService.getStudentProgress(eq(10L), eq(null), eq(null)))
                                .thenReturn(progressDTO);

                // 执行测试
                mockMvc.perform(get("/api/student/progress")
                                .with(csrf())
                                .with(user(studentUser)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.todayNewWords").value(0))
                                .andExpect(jsonPath("$.data.todayReviewWords").value(0))
                                .andExpect(jsonPath("$.data.weekNewWords").value(0))
                                .andExpect(jsonPath("$.data.totalLearnedWords").value(0))
                                .andExpect(jsonPath("$.data.dailyProgress").isEmpty());

                // 验证服务层方法被调用
                verify(dailyProgressService).getStudentProgress(10L, null, null);
        }

        @Test
        @DisplayName("获取学生进度统计 - 未授权（教师角色）")
        @WithMockUser(roles = "TEACHER")
        void testGetStudentProgressUnauthorizedTeacher() throws Exception {
                // 执行测试
                mockMvc.perform(get("/api/student/progress")
                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("获取学生进度统计 - 未认证")
        void testGetStudentProgressUnauthenticated() throws Exception {
                // 执行测试
                mockMvc.perform(get("/api/student/progress")
                                .with(csrf()))
                                .andExpect(status().isUnauthorized());
        }
}
