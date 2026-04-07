package com.example.ancientstars.service;

import com.example.ancientstars.dto.LoginRequest;
import com.example.ancientstars.dto.LoginResponse;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.repository.UserRepository;
import com.example.ancientstars.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("teacher1");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.Role.TEACHER);
        testUser.setStatus(User.Status.ACTIVE);
    }

    @Test
    @DisplayName("登录成功 - 返回访问令牌和刷新令牌")
    void testLoginSuccess() {
        // 准备测试数据
        LoginRequest request = new LoginRequest("teacher1", "password123");
        Authentication authentication = mock(Authentication.class);

        // 模拟行为
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken("teacher1")).thenReturn("refresh-token");
        when(userRepository.findByUsername("teacher1")).thenReturn(Optional.of(testUser));

        // 执行测试
        LoginResponse response = userService.login(request);

        // 验证结果
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(1L, response.getUserId());
        assertEquals("teacher1", response.getUsername());
        assertEquals("TEACHER", response.getRole());

        // 验证方法调用
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
        verify(tokenProvider).generateRefreshToken("teacher1");
        verify(userRepository).findByUsername("teacher1");
    }

    @Test
    @DisplayName("登录失败 - 用户名或密码错误")
    void testLoginFailure() {
        // 准备测试数据
        LoginRequest request = new LoginRequest("teacher1", "wrongPassword");

        // 模拟认证失败
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("用户名或密码错误"));

        // 执行测试并验证异常
        assertThrows(BadCredentialsException.class, () -> userService.login(request));

        // 验证方法调用
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider, never()).generateToken((Authentication) any());
    }

    @Test
    @DisplayName("刷新令牌成功")
    void testRefreshTokenSuccess() {
        // 准备测试数据
        String refreshToken = "valid-refresh-token";

        // 模拟行为
        when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(refreshToken)).thenReturn("teacher1");
        when(tokenProvider.generateToken("teacher1")).thenReturn("new-access-token");

        // 执行测试
        String newAccessToken = userService.refreshAccessToken(refreshToken);

        // 验证结果
        assertEquals("new-access-token", newAccessToken);

        // 验证方法调用
        verify(tokenProvider).validateToken(refreshToken);
        verify(tokenProvider).getUsernameFromToken(refreshToken);
        verify(tokenProvider).generateToken("teacher1");
    }

    @Test
    @DisplayName("刷新令牌失败 - 令牌无效")
    void testRefreshTokenFailure() {
        // 准备测试数据
        String invalidToken = "invalid-token";

        // 模拟行为
        when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.refreshAccessToken(invalidToken));

        // 验证方法调用
        verify(tokenProvider).validateToken(invalidToken);
        verify(tokenProvider, never()).getUsernameFromToken(any());
    }

    @Test
    @DisplayName("创建用户成功")
    void testCreateUserSuccess() {
        // 准备测试数据
        User newUser = new User();
        newUser.setUsername("student1");
        newUser.setPassword("password123");
        newUser.setRole(User.Role.STUDENT);

        // 模拟行为
        when(userRepository.existsByUsername("student1")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // 执行测试
        User createdUser = userService.createUser(newUser);

        // 验证结果
        assertNotNull(createdUser);
        assertEquals("student1", createdUser.getUsername());

        // 验证方法调用
        verify(userRepository).existsByUsername("student1");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("创建用户失败 - 用户名已存在")
    void testCreateUserFailure() {
        // 准备测试数据
        User newUser = new User();
        newUser.setUsername("teacher1");
        newUser.setPassword("password123");

        // 模拟行为
        when(userRepository.existsByUsername("teacher1")).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.createUser(newUser));

        // 验证方法调用
        verify(userRepository).existsByUsername("teacher1");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("禁用用户成功")
    void testDisableUser() {
        // 模拟行为
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 执行测试
        userService.disableUser(1L);

        // 验证结果
        assertEquals(User.Status.DISABLED, testUser.getStatus());

        // 验证方法调用
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("启用用户成功")
    void testEnableUser() {
        // 设置用户为禁用状态
        testUser.setStatus(User.Status.DISABLED);

        // 模拟行为
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 执行测试
        userService.enableUser(1L);

        // 验证结果
        assertEquals(User.Status.ACTIVE, testUser.getStatus());

        // 验证方法调用
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("生成初始密码 - 长度为8位")
    void testGenerateInitialPassword() {
        // 执行测试
        String password = userService.generateInitialPassword();

        // 验证结果
        assertNotNull(password);
        assertEquals(8, password.length());
        assertTrue(password.matches("[A-Za-z0-9]{8}"));
    }

    @Test
    @DisplayName("创建学生账户成功 - 返回学生信息和初始密码")
    void testCreateStudentSuccess() {
        // 准备测试数据
        User student = new User();
        student.setId(2L);
        student.setUsername("student1");
        student.setRealName("张三");
        student.setEmail("student1@example.com");
        student.setRole(User.Role.STUDENT);
        student.setClassId(1L);
        student.setStatus(User.Status.ACTIVE);

        // 模拟行为
        when(userRepository.existsByUsername("student1")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(student);

        // 执行测试
        var result = userService.createStudent("student1", "张三", "student1@example.com", 1L);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getStudent());
        assertNotNull(result.getInitialPassword());
        assertEquals("student1", result.getStudent().getUsername());
        assertEquals("张三", result.getStudent().getRealName());
        assertEquals(8, result.getInitialPassword().length());

        // 验证方法调用
        verify(userRepository).existsByUsername("student1");
        verify(passwordEncoder).encode(any(String.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("创建学生账户失败 - 用户名已存在")
    void testCreateStudentFailure() {
        // 模拟行为
        when(userRepository.existsByUsername("student1")).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(BusinessException.class,
                () -> userService.createStudent("student1", "张三", "student1@example.com", 1L));

        // 验证方法调用
        verify(userRepository).existsByUsername("student1");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新学生信息成功")
    void testUpdateStudentSuccess() {
        // 准备测试数据
        User student = new User();
        student.setId(2L);
        student.setUsername("student1");
        student.setRealName("张三");
        student.setEmail("student1@example.com");
        student.setRole(User.Role.STUDENT);
        student.setClassId(1L);

        // 模拟行为
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenReturn(student);

        // 执行测试
        User updatedStudent = userService.updateStudent(2L, "李四", "student2@example.com", 2L);

        // 验证结果
        assertNotNull(updatedStudent);
        assertEquals("李四", updatedStudent.getRealName());
        assertEquals("student2@example.com", updatedStudent.getEmail());
        assertEquals(2L, updatedStudent.getClassId());

        // 验证方法调用
        verify(userRepository).findById(2L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("更新学生信息失败 - 用户不是学生角色")
    void testUpdateStudentFailureNotStudent() {
        // 模拟行为
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser)); // testUser是教师

        // 执行测试并验证异常
        assertThrows(BusinessException.class,
                () -> userService.updateStudent(1L, "新名字", "new@example.com", 1L));

        // 验证方法调用
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("按班级查询学生列表 - 指定班级ID")
    void testGetStudentsByClassWithClassId() {
        // 准备测试数据
        User student1 = new User();
        student1.setId(2L);
        student1.setUsername("student1");
        student1.setRole(User.Role.STUDENT);
        student1.setClassId(1L);

        User student2 = new User();
        student2.setId(3L);
        student2.setUsername("student2");
        student2.setRole(User.Role.STUDENT);
        student2.setClassId(1L);

        // 模拟行为
        when(userRepository.findByRoleAndClassId(User.Role.STUDENT, 1L))
                .thenReturn(java.util.Arrays.asList(student1, student2));

        // 执行测试
        java.util.List<User> students = userService.getStudentsByClass(1L);

        // 验证结果
        assertNotNull(students);
        assertEquals(2, students.size());
        assertEquals("student1", students.get(0).getUsername());
        assertEquals("student2", students.get(1).getUsername());

        // 验证方法调用
        verify(userRepository).findByRoleAndClassId(User.Role.STUDENT, 1L);
    }

    @Test
    @DisplayName("按班级查询学生列表 - 不指定班级ID返回所有学生")
    void testGetStudentsByClassWithoutClassId() {
        // 准备测试数据
        User student1 = new User();
        student1.setId(2L);
        student1.setUsername("student1");
        student1.setRole(User.Role.STUDENT);

        // 模拟行为
        when(userRepository.findByRole(User.Role.STUDENT))
                .thenReturn(java.util.Collections.singletonList(student1));

        // 执行测试
        java.util.List<User> students = userService.getStudentsByClass(null);

        // 验证结果
        assertNotNull(students);
        assertEquals(1, students.size());

        // 验证方法调用
        verify(userRepository).findByRole(User.Role.STUDENT);
    }

    @Test
    @DisplayName("获取所有学生")
    void testGetAllStudents() {
        // 准备测试数据
        User student1 = new User();
        student1.setId(2L);
        student1.setUsername("student1");
        student1.setRole(User.Role.STUDENT);

        // 模拟行为
        when(userRepository.findByRole(User.Role.STUDENT))
                .thenReturn(java.util.Collections.singletonList(student1));

        // 执行测试
        java.util.List<User> students = userService.getAllStudents();

        // 验证结果
        assertNotNull(students);
        assertEquals(1, students.size());

        // 验证方法调用
        verify(userRepository).findByRole(User.Role.STUDENT);
    }

    @Test
    @DisplayName("根据ID获取用户成功")
    void testGetUserByIdSuccess() {
        // 模拟行为
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 执行测试
        User user = userService.getUserById(1L);

        // 验证结果
        assertNotNull(user);
        assertEquals("teacher1", user.getUsername());

        // 验证方法调用
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("根据ID获取用户失败 - 用户不存在")
    void testGetUserByIdFailure() {
        // 模拟行为
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.getUserById(999L));

        // 验证方法调用
        verify(userRepository).findById(999L);
    }
}
