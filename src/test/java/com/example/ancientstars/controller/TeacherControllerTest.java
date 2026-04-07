package com.example.ancientstars.controller;

import com.example.ancientstars.dto.CreateStudentRequest;
import com.example.ancientstars.dto.StudentCreationResult;
import com.example.ancientstars.dto.UpdateStudentRequest;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 教师控制器测试
 */
@WebMvcTest(TeacherController.class)
@AutoConfigureMockMvc
@DisplayName("教师控制器测试")
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User testStudent;

    @BeforeEach
    void setUp() {
        testStudent = new User();
        testStudent.setId(1L);
        testStudent.setUsername("student1");
        testStudent.setRealName("张三");
        testStudent.setEmail("student1@example.com");
        testStudent.setRole(User.Role.STUDENT);
        testStudent.setClassId(1L);
        testStudent.setStatus(User.Status.ACTIVE);
    }

    @Test
    @DisplayName("创建学生账户 - 成功")
    @WithMockUser(roles = "TEACHER")
    void testCreateStudent() throws Exception {
        // 准备测试数据
        CreateStudentRequest request = new CreateStudentRequest(
                "student1", "张三", "student1@example.com", 1L);

        StudentCreationResult result = new StudentCreationResult(testStudent, "Test1234");

        // 模拟行为
        when(userService.createStudent(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(result);

        // 执行测试
        mockMvc.perform(post("/api/teacher/students")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("student1"))
                .andExpect(jsonPath("$.data.realName").value("张三"))
                .andExpect(jsonPath("$.data.initialPassword").value("Test1234"));

        // 验证方法调用
        verify(userService).createStudent("student1", "张三", "student1@example.com", 1L);
    }

    @Test
    @DisplayName("获取学生列表 - 不指定班级")
    @WithMockUser(roles = "TEACHER")
    void testGetStudentsWithoutClassId() throws Exception {
        // 准备测试数据
        List<User> students = Arrays.asList(testStudent);

        // 模拟行为
        when(userService.getStudentsByClass(null)).thenReturn(students);

        // 执行测试
        mockMvc.perform(get("/api/teacher/students")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].username").value("student1"))
                .andExpect(jsonPath("$.data[0].realName").value("张三"));

        // 验证方法调用
        verify(userService).getStudentsByClass(null);
    }

    @Test
    @DisplayName("获取学生列表 - 指定班级")
    @WithMockUser(roles = "TEACHER")
    void testGetStudentsWithClassId() throws Exception {
        // 准备测试数据
        List<User> students = Arrays.asList(testStudent);

        // 模拟行为
        when(userService.getStudentsByClass(1L)).thenReturn(students);

        // 执行测试
        mockMvc.perform(get("/api/teacher/students")
                .param("classId", "1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].classId").value(1));

        // 验证方法调用
        verify(userService).getStudentsByClass(1L);
    }

    @Test
    @DisplayName("获取学生详情")
    @WithMockUser(roles = "TEACHER")
    void testGetStudent() throws Exception {
        // 模拟行为
        when(userService.getUserById(1L)).thenReturn(testStudent);

        // 执行测试
        mockMvc.perform(get("/api/teacher/students/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("student1"))
                .andExpect(jsonPath("$.data.realName").value("张三"));

        // 验证方法调用
        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("更新学生信息")
    @WithMockUser(roles = "TEACHER")
    void testUpdateStudent() throws Exception {
        // 准备测试数据
        UpdateStudentRequest request = new UpdateStudentRequest("李四", "student2@example.com", 2L);

        testStudent.setRealName("李四");
        testStudent.setEmail("student2@example.com");
        testStudent.setClassId(2L);

        // 模拟行为
        when(userService.updateStudent(eq(1L), anyString(), anyString(), anyLong()))
                .thenReturn(testStudent);

        // 执行测试
        mockMvc.perform(put("/api/teacher/students/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.realName").value("李四"))
                .andExpect(jsonPath("$.data.email").value("student2@example.com"))
                .andExpect(jsonPath("$.data.classId").value(2));

        // 验证方法调用
        verify(userService).updateStudent(1L, "李四", "student2@example.com", 2L);
    }

    @Test
    @DisplayName("禁用学生账户")
    @WithMockUser(roles = "TEACHER")
    void testDisableStudent() throws Exception {
        // 执行测试
        mockMvc.perform(post("/api/teacher/students/1/disable")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证方法调用
        verify(userService).disableUser(1L);
    }

    @Test
    @DisplayName("启用学生账户")
    @WithMockUser(roles = "TEACHER")
    void testEnableStudent() throws Exception {
        // 执行测试
        mockMvc.perform(post("/api/teacher/students/1/enable")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证方法调用
        verify(userService).enableUser(1L);
    }

    @Test
    @DisplayName("未授权访问 - 学生角色不能访问")
    @WithMockUser(roles = "STUDENT")
    void testUnauthorizedAccess() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/teacher/students")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
