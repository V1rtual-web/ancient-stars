package com.example.ancientstars.controller;

import com.example.ancientstars.entity.DailyProgress;
import com.example.ancientstars.entity.StudyRecord;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.entity.Vocabulary;
import com.example.ancientstars.repository.DailyProgressRepository;
import com.example.ancientstars.repository.StudyRecordRepository;
import com.example.ancientstars.repository.UserRepository;
import com.example.ancientstars.repository.VocabularyRepository;
import com.example.ancientstars.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 教师进度查询控制器集成测试
 * 验证需求: 5.2, 5.6
 * 
 * 测试重点:
 * 1. 权限控制 - 教师只能查看自己班级的学生
 * 2. 统计数据准确性 - 验证返回的统计数据是否正确
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("教师进度查询控制器集成测试")
class TeacherProgressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private StudyRecordRepository studyRecordRepository;

    @Autowired
    private DailyProgressRepository dailyProgressRepository;

    // 测试数据
    private User teacher1; // 班级1的教师
    private User teacher2; // 班级2的教师
    private User student1; // 班级1的学生
    private User student2; // 班级1的学生
    private User student3; // 班级2的学生
    private Vocabulary vocab1;
    private Vocabulary vocab2;
    private Vocabulary vocab3;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        dailyProgressRepository.deleteAll();
        studyRecordRepository.deleteAll();
        vocabularyRepository.deleteAll();
        userRepository.deleteAll();

        // 创建测试教师
        teacher1 = createUser("teacher1", "Teacher One", User.Role.TEACHER, 1L);
        teacher2 = createUser("teacher2", "Teacher Two", User.Role.TEACHER, 2L);

        // 创建测试学生
        student1 = createUser("student1", "Student One", User.Role.STUDENT, 1L);
        student2 = createUser("student2", "Student Two", User.Role.STUDENT, 1L);
        student3 = createUser("student3", "Student Three", User.Role.STUDENT, 2L);

        // 创建测试词汇
        vocab1 = createVocabulary("apple", "/ˈæpl/", "n. 苹果");
        vocab2 = createVocabulary("banana", "/bəˈnɑːnə/", "n. 香蕉");
        vocab3 = createVocabulary("cherry", "/ˈtʃeri/", "n. 樱桃");
    }

    // ==================== 权限控制测试 ====================

    @Test
    @DisplayName("教师成功查看自己班级学生的进度 - 验证需求 5.2")
    void testTeacherCanViewOwnClassStudent() throws Exception {
        // 准备测试数据：学生1有学习记录
        createStudyRecordAndProgress(student1.getId(), vocab1.getId(), LocalDate.now(), 10, 5);

        // 设置教师1的认证上下文
        authenticateAsTeacher(teacher1);

        // 教师1查看学生1的进度（同班级）
        mockMvc.perform(get("/api/teacher/student-progress/{studentId}", student1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.todayNewWords").value(10))
                .andExpect(jsonPath("$.data.todayReviewWords").value(5));
    }

    @Test
    @DisplayName("教师无法查看其他班级学生的进度 - 验证需求 5.2")
    void testTeacherCannotViewOtherClassStudent() throws Exception {
        // 准备测试数据：学生3属于班级2
        createStudyRecordAndProgress(student3.getId(), vocab1.getId(), LocalDate.now(), 10, 5);

        // 设置教师1的认证上下文（班级1）
        authenticateAsTeacher(teacher1);

        // 教师1尝试查看学生3的进度（不同班级）
        mockMvc.perform(get("/api/teacher/student-progress/{studentId}", student3.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("教师成功查看自己班级的统计 - 验证需求 5.6")
    void testTeacherCanViewOwnClassProgress() throws Exception {
        // 准备测试数据：班级1的学生有学习记录
        createStudyRecordAndProgress(student1.getId(), vocab1.getId(), LocalDate.now(), 15, 8);
        createStudyRecordAndProgress(student2.getId(), vocab2.getId(), LocalDate.now(), 10, 5);

        // 设置教师1的认证上下文
        authenticateAsTeacher(teacher1);

        // 教师1查看班级1的统计
        mockMvc.perform(get("/api/teacher/class-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.classId").value(1))
                .andExpect(jsonPath("$.data.totalStudents").value(2))
                .andExpect(jsonPath("$.data.activeStudents").value(2));
    }

    @Test
    @DisplayName("教师无法查看其他班级的统计 - 验证需求 5.6")
    void testTeacherCannotViewOtherClassProgress() throws Exception {
        // 设置教师1的认证上下文（班级1）
        authenticateAsTeacher(teacher1);

        // 教师1尝试查看班级2的统计
        mockMvc.perform(get("/api/teacher/class-progress")
                .param("classId", "2"))
                .andExpect(status().isForbidden());
    }

    // ==================== 统计数据准确性测试 ====================

    @Test
    @DisplayName("验证学生进度统计数据准确性 - 今日数据 - 验证需求 5.2")
    void testStudentProgressAccuracy_TodayData() throws Exception {
        // 准备测试数据：学生1今天学习了15个新词，复习了8个词
        LocalDate today = LocalDate.now();
        createDailyProgress(student1.getId(), today, 15, 8);

        // 设置教师1的认证上下文
        authenticateAsTeacher(teacher1);

        // 查询学生1的进度
        mockMvc.perform(get("/api/teacher/student-progress/{studentId}", student1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.todayNewWords").value(15))
                .andExpect(jsonPath("$.data.todayReviewWords").value(8));
    }

    @Test
    @DisplayName("验证学生进度统计数据准确性 - 本周数据 - 验证需求 5.2")
    void testStudentProgressAccuracy_WeekData() throws Exception {
        // 准备测试数据：学生1本周每天学习不同数量的单词
        LocalDate today = LocalDate.now();
        createDailyProgress(student1.getId(), today, 15, 8);
        createDailyProgress(student1.getId(), today.minusDays(1), 12, 6);
        createDailyProgress(student1.getId(), today.minusDays(2), 10, 5);
        createDailyProgress(student1.getId(), today.minusDays(3), 8, 4);
        // 本周新学单词总数: 15 + 12 + 10 + 8 = 45

        // 设置教师1的认证上下文
        authenticateAsTeacher(teacher1);

        // 查询学生1的进度
        mockMvc.perform(get("/api/teacher/student-progress/{studentId}", student1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.weekNewWords").value(45));
    }

    @Test
    @DisplayName("验证学生进度统计数据准确性 - 累计数据 - 验证需求 5.2")
    void testStudentProgressAccuracy_TotalData() throws Exception {
        // 准备测试数据：学生1累计学习了多个单词
        createStudyRecord(student1.getId(), vocab1.getId(), LocalDateTime.now());
        createStudyRecord(student1.getId(), vocab2.getId(), LocalDateTime.now());
        createStudyRecord(student1.getId(), vocab3.getId(), LocalDateTime.now());
        // 累计学习单词数: 3

        // 设置教师1的认证上下文
        authenticateAsTeacher(teacher1);

        // 查询学生1的进度
        mockMvc.perform(get("/api/teacher/student-progress/{studentId}", student1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalLearnedWords").value(3));
    }

    @Test
    @DisplayName("验证学生进度统计数据准确性 - 日期范围筛选 - 验证需求 5.2")
    void testStudentProgressAccuracy_DateRange() throws Exception {
        // 准备测试数据：学生1在不同日期有学习记录
        LocalDate today = LocalDate.now();
        createDailyProgress(student1.getId(), today, 15, 8);
        createDailyProgress(student1.getId(), today.minusDays(5), 12, 6);
        createDailyProgress(student1.getId(), today.minusDays(10), 10, 5); // 超出范围

        // 设置教师1的认证上下文
        authenticateAsTeacher(teacher1);

        // 查询学生1最近7天的进度
        mockMvc.perform(get("/api/teacher/student-progress/{studentId}", student1.getId())
                .param("startDate", today.minusDays(7).toString())
                .param("endDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dailyProgress").isArray())
                .andExpect(jsonPath("$.data.dailyProgress", hasSize(2))); // 只有2条记录在范围内
    }

    @Test
    @DisplayName("验证班级统计数据准确性 - 学生总数和活跃学生数 - 验证需求 5.6")
    void testClassProgressAccuracy_StudentCounts() throws Exception {
        // 准备测试数据：班级1有2个学生，其中1个有学习记录
        createDailyProgress(student1.getId(), LocalDate.now(), 15, 8);
        // student2 没有学习记录

        // 设置教师1的认证上下文
        authenticateAsTeacher(teacher1);

        // 查询班级1的统计
        mockMvc.perform(get("/api/teacher/class-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalStudents").value(2))
                .andExpect(jsonPath("$.data.activeStudents").value(1));
    }

    @Test
    @DisplayName("验证班级统计数据准确性 - 平均每日学习量 - 验证需求 5.6")
    void testClassProgressAccuracy_AverageDailyWords() throws Exception {
        // 准备测试数据：班级1的学生学习记录
        LocalDate today = LocalDate.now();
        createDailyProgress(student1.getId(), today, 20, 10); // 学生1: 20个新词
        createDailyProgress(student2.getId(), today, 10, 5); // 学生2: 10个新词
        // 平均每日学习量: (20 + 10) / (2 students * 30 days) = 30 / 60 = 0.5

        // 设置教师1的认证上下文
        authenticateAsTeacher(teacher1);

        // 查询班级1的统计
        mockMvc.perform(get("/api/teacher/class-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.averageDailyWords").value(0.5));
    }

    @Test
    @DisplayName("验证班级统计数据准确性 - 学习最积极的学生排名 - 验证需求 5.6")
    void testClassProgressAccuracy_TopStudents() throws Exception {
        // 准备测试数据：班级1的学生有不同的学习量
        createStudyRecord(student1.getId(), vocab1.getId(), LocalDateTime.now());
        createStudyRecord(student1.getId(), vocab2.getId(), LocalDateTime.now());
        createStudyRecord(student1.getId(), vocab3.getId(), LocalDateTime.now()); // 学生1: 3个词

        createStudyRecord(student2.getId(), vocab1.getId(), LocalDateTime.now());
        createStudyRecord(student2.getId(), vocab2.getId(), LocalDateTime.now()); // 学生2: 2个词

        // 设置教师1的认证上下文
        authenticateAsTeacher(teacher1);

        // 查询班级1的统计
        mockMvc.perform(get("/api/teacher/class-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.topStudents").isArray())
                .andExpect(jsonPath("$.data.topStudents", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.topStudents[0].studentName").value("Student One"))
                .andExpect(jsonPath("$.data.topStudents[0].totalWords").value(3));
    }

    @Test
    @DisplayName("验证班级统计数据准确性 - 日期范围筛选 - 验证需求 5.6")
    void testClassProgressAccuracy_DateRange() throws Exception {
        // 准备测试数据：不同日期的学习记录
        LocalDate today = LocalDate.now();
        createDailyProgress(student1.getId(), today, 15, 8);
        createDailyProgress(student1.getId(), today.minusDays(5), 12, 6);
        createDailyProgress(student1.getId(), today.minusDays(10), 10, 5); // 超出范围

        createDailyProgress(student2.getId(), today, 10, 5);

        // 设置教师1的认证上下文
        authenticateAsTeacher(teacher1);

        // 查询班级1最近7天的统计
        mockMvc.perform(get("/api/teacher/class-progress")
                .param("startDate", today.minusDays(7).toString())
                .param("endDate", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeStudents").value(2)); // 两个学生在范围内都有记录
    }

    @Test
    @DisplayName("验证空数据情况 - 学生无学习记录 - 验证需求 5.2")
    void testStudentProgressAccuracy_NoData() throws Exception {
        // 学生1没有任何学习记录

        // 设置教师1的认证上下文
        authenticateAsTeacher(teacher1);

        // 查询学生1的进度
        mockMvc.perform(get("/api/teacher/student-progress/{studentId}", student1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.todayNewWords").value(0))
                .andExpect(jsonPath("$.data.todayReviewWords").value(0))
                .andExpect(jsonPath("$.data.weekNewWords").value(0))
                .andExpect(jsonPath("$.data.totalLearnedWords").value(0));
    }

    @Test
    @DisplayName("验证空数据情况 - 班级无活跃学生 - 验证需求 5.6")
    void testClassProgressAccuracy_NoActiveStudents() throws Exception {
        // 班级1的学生都没有学习记录

        // 设置教师1的认证上下文
        authenticateAsTeacher(teacher1);

        // 查询班级1的统计
        mockMvc.perform(get("/api/teacher/class-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalStudents").value(2))
                .andExpect(jsonPath("$.data.activeStudents").value(0))
                .andExpect(jsonPath("$.data.averageDailyWords").value(0.0));
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建用户
     */
    private User createUser(String username, String realName, User.Role role, Long classId) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");
        user.setRealName(realName);
        user.setRole(role);
        user.setClassId(classId);
        user.setStatus(User.Status.ACTIVE);
        return userRepository.save(user);
    }

    /**
     * 创建词汇
     */
    private Vocabulary createVocabulary(String word, String phonetic, String translation) {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setWord(word);
        vocabulary.setPhonetic(phonetic);
        vocabulary.setTranslation(translation);
        vocabulary.setDefinition("Definition of " + word);
        vocabulary.setExample("Example sentence with " + word);
        vocabulary.setDifficultyLevel(1);
        return vocabularyRepository.save(vocabulary);
    }

    /**
     * 创建学习记录
     */
    private StudyRecord createStudyRecord(Long studentId, Long vocabularyId, LocalDateTime studyTime) {
        StudyRecord record = new StudyRecord();
        record.setStudentId(studentId);
        record.setVocabularyId(vocabularyId);
        record.setStudyTime(studyTime);
        record.setReviewCount(0);
        record.setMasteryLevel(StudyRecord.MasteryLevel.NEW);
        record.setNextReviewTime(studyTime.plusDays(7));
        return studyRecordRepository.save(record);
    }

    /**
     * 创建每日进度记录
     */
    private DailyProgress createDailyProgress(Long studentId, LocalDate studyDate,
            Integer newWords, Integer reviewWords) {
        DailyProgress progress = new DailyProgress();
        progress.setStudentId(studentId);
        progress.setStudyDate(studyDate);
        progress.setNewWordsCount(newWords);
        progress.setReviewWordsCount(reviewWords);
        progress.setTotalStudyTime(0);
        return dailyProgressRepository.save(progress);
    }

    /**
     * 创建学习记录和每日进度（组合方法）
     */
    private void createStudyRecordAndProgress(Long studentId, Long vocabularyId,
            LocalDate date, Integer newWords, Integer reviewWords) {
        createStudyRecord(studentId, vocabularyId, date.atStartOfDay());
        createDailyProgress(studentId, date, newWords, reviewWords);
    }

    /**
     * 设置教师认证上下文
     */
    private void authenticateAsTeacher(User teacher) {
        CustomUserDetails userDetails = new CustomUserDetails(
                teacher.getId(),
                teacher.getUsername(),
                teacher.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + teacher.getRole().name())));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
