package com.example.ancientstars.controller;

import com.example.ancientstars.dto.StudentProgressDTO;
import com.example.ancientstars.service.DailyProgressService;
import com.example.ancientstars.service.TeacherAuthorizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 教师进度查询控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("教师进度查询控制器测试")
class TeacherProgressControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private DailyProgressService dailyProgressService;

        @MockBean
        private TeacherAuthorizationService teacherAuthorizationService;

        @Test
        @WithMockUser(username = "teacher", roles = { "TEACHER" })
        @DisplayName("成功查询学生进度 - 验证需求 5.2, 5.3")
        void testGetStudentProgress_Success() throws Exception {
                Long studentId = 100L;
                StudentProgressDTO mockProgress = StudentProgressDTO.builder()
                                .todayNewWords(15)
                                .todayReviewWords(8)
                                .weekNewWords(85)
                                .totalLearnedWords(320)
                                .dailyProgress(new ArrayList<>())
                                .build();

                doNothing().when(teacherAuthorizationService).verifyTeacherCanViewStudent(any(), eq(studentId));
                when(dailyProgressService.getStudentProgress(eq(studentId), any(), any())).thenReturn(mockProgress);

                mockMvc.perform(get("/api/teacher/student-progress/{studentId}", studentId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.todayNewWords").value(15))
                                .andExpect(jsonPath("$.data.totalLearnedWords").value(320));
        }

        @Test
        @WithMockUser(username = "teacher", roles = { "TEACHER" })
        @DisplayName("查询学生进度 - 带日期范围参数 - 验证需求 5.4")
        void testGetStudentProgress_WithDateRange() throws Exception {
                Long studentId = 100L;
                StudentProgressDTO mockProgress = StudentProgressDTO.builder()
                                .todayNewWords(15)
                                .weekNewWords(85)
                                .totalLearnedWords(320)
                                .dailyProgress(new ArrayList<>())
                                .build();

                doNothing().when(teacherAuthorizationService).verifyTeacherCanViewStudent(any(), eq(studentId));
                when(dailyProgressService.getStudentProgress(eq(studentId), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(mockProgress);

                mockMvc.perform(get("/api/teacher/student-progress/{studentId}", studentId)
                                .param("startDate", "2026-03-01")
                                .param("endDate", "2026-03-31"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));

                verify(dailyProgressService).getStudentProgress(eq(studentId), any(LocalDate.class),
                                any(LocalDate.class));
        }

        @Test
        @WithMockUser(username = "teacher", roles = { "TEACHER" })
        @DisplayName("教师无权查看学生 - 验证需求 5.2")
        void testGetStudentProgress_AccessDenied() throws Exception {
                Long studentId = 100L;

                doThrow(new AccessDeniedException("您无权查看该学生的数据"))
                                .when(teacherAuthorizationService).verifyTeacherCanViewStudent(any(), eq(studentId));

                mockMvc.perform(get("/api/teacher/student-progress/{studentId}", studentId))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("未认证访问 - 验证需求 8.5")
        void testGetStudentProgress_Unauthorized() throws Exception {
                mockMvc.perform(get("/api/teacher/student-progress/{studentId}", 100L))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "student", roles = { "STUDENT" })
        @DisplayName("学生角色访问 - 验证需求 8.5")
        void testGetStudentProgress_StudentRoleForbidden() throws Exception {
                mockMvc.perform(get("/api/teacher/student-progress/{studentId}", 100L))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "teacher", roles = { "TEACHER" })
        @DisplayName("学生不存在 - 验证需求 8.8")
        void testGetStudentProgress_StudentNotFound() throws Exception {
                Long studentId = 99999L;

                doThrow(new IllegalArgumentException("学生不存在: " + studentId))
                                .when(teacherAuthorizationService).verifyTeacherCanViewStudent(any(), eq(studentId));

                mockMvc.perform(get("/api/teacher/student-progress/{studentId}", studentId))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(username = "teacher", roles = { "TEACHER" })
        @DisplayName("成功查询班级学习统计 - 验证需求 5.6")
        void testGetClassProgress_Success() throws Exception {
                Long classId = 1L;
                com.example.ancientstars.dto.ClassProgressDTO mockProgress = com.example.ancientstars.dto.ClassProgressDTO
                                .builder()
                                .classId(classId)
                                .totalStudents(30)
                                .activeStudents(25)
                                .averageDailyWords(10.5)
                                .topStudents(new ArrayList<>())
                                .build();

                when(teacherAuthorizationService.getTeacherClassId(any())).thenReturn(classId);
                when(dailyProgressService.getClassProgress(eq(classId), any(), any())).thenReturn(mockProgress);

                mockMvc.perform(get("/api/teacher/class-progress"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.totalStudents").value(30))
                                .andExpect(jsonPath("$.data.activeStudents").value(25))
                                .andExpect(jsonPath("$.data.averageDailyWords").value(10.5));
        }

        @Test
        @WithMockUser(username = "teacher", roles = { "TEACHER" })
        @DisplayName("查询班级学习统计 - 指定班级ID - 验证需求 5.6")
        void testGetClassProgress_WithClassId() throws Exception {
                Long classId = 1L;
                com.example.ancientstars.dto.ClassProgressDTO mockProgress = com.example.ancientstars.dto.ClassProgressDTO
                                .builder()
                                .classId(classId)
                                .totalStudents(30)
                                .activeStudents(25)
                                .averageDailyWords(10.5)
                                .topStudents(new ArrayList<>())
                                .build();

                doNothing().when(teacherAuthorizationService).verifyTeacherCanViewClass(any(), eq(classId));
                when(dailyProgressService.getClassProgress(eq(classId), any(), any())).thenReturn(mockProgress);

                mockMvc.perform(get("/api/teacher/class-progress")
                                .param("classId", classId.toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.classId").value(classId));
        }

        @Test
        @WithMockUser(username = "teacher", roles = { "TEACHER" })
        @DisplayName("查询班级学习统计 - 带日期范围 - 验证需求 5.6")
        void testGetClassProgress_WithDateRange() throws Exception {
                Long classId = 1L;
                com.example.ancientstars.dto.ClassProgressDTO mockProgress = com.example.ancientstars.dto.ClassProgressDTO
                                .builder()
                                .classId(classId)
                                .totalStudents(30)
                                .activeStudents(25)
                                .averageDailyWords(10.5)
                                .topStudents(new ArrayList<>())
                                .build();

                when(teacherAuthorizationService.getTeacherClassId(any())).thenReturn(classId);
                when(dailyProgressService.getClassProgress(eq(classId), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(mockProgress);

                mockMvc.perform(get("/api/teacher/class-progress")
                                .param("startDate", "2026-03-01")
                                .param("endDate", "2026-03-31"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));

                verify(dailyProgressService).getClassProgress(eq(classId), any(LocalDate.class), any(LocalDate.class));
        }

        @Test
        @WithMockUser(username = "teacher", roles = { "TEACHER" })
        @DisplayName("教师无权查看指定班级 - 验证需求 5.6")
        void testGetClassProgress_AccessDenied() throws Exception {
                Long classId = 2L;

                doThrow(new AccessDeniedException("您无权查看该班级的数据"))
                                .when(teacherAuthorizationService).verifyTeacherCanViewClass(any(), eq(classId));

                mockMvc.perform(get("/api/teacher/class-progress")
                                .param("classId", classId.toString()))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("未认证访问班级统计 - 验证需求 8.5")
        void testGetClassProgress_Unauthorized() throws Exception {
                mockMvc.perform(get("/api/teacher/class-progress"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "student", roles = { "STUDENT" })
        @DisplayName("学生角色访问班级统计 - 验证需求 8.5")
        void testGetClassProgress_StudentRoleForbidden() throws Exception {
                mockMvc.perform(get("/api/teacher/class-progress"))
                                .andExpect(status().isForbidden());
        }
}
