package com.example.ancientstars.controller;

import com.example.ancientstars.dto.PageResponse;
import com.example.ancientstars.dto.StudentVocabularyDTO;
import com.example.ancientstars.security.CustomUserDetails;
import com.example.ancientstars.service.StudentVocabularyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 学生词汇浏览控制器测试
 */
@WebMvcTest(StudentVocabularyController.class)
@AutoConfigureMockMvc
@DisplayName("学生词汇浏览控制器测试")
class StudentVocabularyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentVocabularyService studentVocabularyService;

    private CustomUserDetails studentUser;
    private List<StudentVocabularyDTO> vocabularies;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        studentUser = new CustomUserDetails(
                10L,
                "student1",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")));

        // 创建测试词汇数据
        StudentVocabularyDTO vocab1 = new StudentVocabularyDTO();
        vocab1.setId(1L);
        vocab1.setWord("apple");
        vocab1.setPhonetic("/ˈæpl/");
        vocab1.setTranslation("n. 苹果");
        vocab1.setExample("I like apples.");
        vocab1.setIsLearned(false);

        StudentVocabularyDTO vocab2 = new StudentVocabularyDTO();
        vocab2.setId(2L);
        vocab2.setWord("banana");
        vocab2.setPhonetic("/bəˈnɑːnə/");
        vocab2.setTranslation("n. 香蕉");
        vocab2.setExample("I eat a banana.");
        vocab2.setIsLearned(true);

        vocabularies = Arrays.asList(vocab1, vocab2);
    }

    @Test
    @DisplayName("获取词汇列表 - 默认参数")
    @WithMockUser(roles = "STUDENT")
    void testGetVocabulariesWithDefaultParams() throws Exception {
        // 准备测试数据
        PageResponse<StudentVocabularyDTO> response = PageResponse.of(vocabularies, 2L, 1, 20);

        // 模拟行为
        when(studentVocabularyService.getVocabulariesForStudent(
                eq(10L), eq(1), eq(20), isNull(), isNull()))
                .thenReturn(response);

        // 执行测试
        mockMvc.perform(get("/api/student/vocabularies")
                .with(csrf())
                .with(user(studentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].word").value("apple"))
                .andExpect(jsonPath("$.data.items[0].isLearned").value(false))
                .andExpect(jsonPath("$.data.items[1].word").value("banana"))
                .andExpect(jsonPath("$.data.items[1].isLearned").value(true))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20));

        // 验证方法调用
        verify(studentVocabularyService).getVocabulariesForStudent(10L, 1, 20, null, null);
    }

    @Test
    @DisplayName("获取词汇列表 - 指定页码和大小")
    @WithMockUser(roles = "STUDENT")
    void testGetVocabulariesWithPagination() throws Exception {
        // 准备测试数据
        PageResponse<StudentVocabularyDTO> response = PageResponse.of(vocabularies, 100L, 2, 10);

        // 模拟行为
        when(studentVocabularyService.getVocabulariesForStudent(
                eq(10L), eq(2), eq(10), isNull(), isNull()))
                .thenReturn(response);

        // 执行测试
        mockMvc.perform(get("/api/student/vocabularies")
                .param("page", "2")
                .param("size", "10")
                .with(csrf())
                .with(user(studentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.total").value(100));

        // 验证方法调用
        verify(studentVocabularyService).getVocabulariesForStudent(10L, 2, 10, null, null);
    }

    @Test
    @DisplayName("获取词汇列表 - 按词汇表筛选")
    @WithMockUser(roles = "STUDENT")
    void testGetVocabulariesByWordList() throws Exception {
        // 准备测试数据
        PageResponse<StudentVocabularyDTO> response = PageResponse.of(vocabularies, 2L, 1, 20);

        // 模拟行为
        when(studentVocabularyService.getVocabulariesForStudent(
                eq(10L), eq(1), eq(20), eq(5L), isNull()))
                .thenReturn(response);

        // 执行测试
        mockMvc.perform(get("/api/student/vocabularies")
                .param("wordListId", "5")
                .with(csrf())
                .with(user(studentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());

        // 验证方法调用
        verify(studentVocabularyService).getVocabulariesForStudent(10L, 1, 20, 5L, null);
    }

    @Test
    @DisplayName("获取词汇列表 - 关键词搜索")
    @WithMockUser(roles = "STUDENT")
    void testGetVocabulariesByKeyword() throws Exception {
        // 准备测试数据
        PageResponse<StudentVocabularyDTO> response = PageResponse.of(
                Collections.singletonList(vocabularies.get(0)), 1L, 1, 20);

        // 模拟行为
        when(studentVocabularyService.getVocabulariesForStudent(
                eq(10L), eq(1), eq(20), isNull(), eq("apple")))
                .thenReturn(response);

        // 执行测试
        mockMvc.perform(get("/api/student/vocabularies")
                .param("keyword", "apple")
                .with(csrf())
                .with(user(studentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].word").value("apple"))
                .andExpect(jsonPath("$.data.total").value(1));

        // 验证方法调用
        verify(studentVocabularyService).getVocabulariesForStudent(10L, 1, 20, null, "apple");
    }

    @Test
    @DisplayName("获取词汇列表 - 同时使用词汇表和关键词筛选")
    @WithMockUser(roles = "STUDENT")
    void testGetVocabulariesByWordListAndKeyword() throws Exception {
        // 准备测试数据
        PageResponse<StudentVocabularyDTO> response = PageResponse.of(
                Collections.singletonList(vocabularies.get(0)), 1L, 1, 20);

        // 模拟行为
        when(studentVocabularyService.getVocabulariesForStudent(
                eq(10L), eq(1), eq(20), eq(5L), eq("apple")))
                .thenReturn(response);

        // 执行测试
        mockMvc.perform(get("/api/student/vocabularies")
                .param("wordListId", "5")
                .param("keyword", "apple")
                .with(csrf())
                .with(user(studentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].word").value("apple"));

        // 验证方法调用
        verify(studentVocabularyService).getVocabulariesForStudent(10L, 1, 20, 5L, "apple");
    }

    @Test
    @DisplayName("未授权访问 - 教师角色不能访问学生接口")
    @WithMockUser(roles = "TEACHER")
    void testUnauthorizedAccessByTeacher() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/student/vocabularies")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("未认证访问 - 需要登录")
    void testUnauthenticatedAccess() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/student/vocabularies")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
