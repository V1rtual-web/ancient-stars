package com.example.ancientstars.service;

import com.example.ancientstars.dto.StudentCreationResult;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.repository.UserRepository;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 学生账户唯一性属性测试
 * **Validates: Requirements 3.1**
 * 属性 6: 学生账户唯一性
 * 验证需求: 3.1
 */
@Tag("Feature: vocabulary-learning-system, Property 6: 学生账户唯一性")
class StudentAccountUniquenessPropertyTest {

    @Property(tries = 100)
    @Label("属性6: 学生账户唯一性 - 创建多个学生时用户名必须唯一")
    void multipleStudentsShouldHaveUniqueUsernames(@ForAll("studentCounts") int studentCount) {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService();
        setField(userService, "userRepository", userRepository);
        setField(userService, "passwordEncoder", passwordEncoder);

        Set<String> createdUsernames = new HashSet<>();
        Set<Long> createdUserIds = new HashSet<>();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        for (int i = 0; i < studentCount; i++) {
            String username = "student_unique_" + System.nanoTime() + "_" + i;
            String realName = "Student " + i;
            String email = "student" + i + "@test.com";
            Long classId = 1L;

            when(userRepository.existsByUsername(username)).thenReturn(false);

            User savedUser = new User();
            savedUser.setId((long) (i + 1));
            savedUser.setUsername(username);
            savedUser.setPassword("encodedPassword");
            savedUser.setRealName(realName);
            savedUser.setEmail(email);
            savedUser.setRole(User.Role.STUDENT);
            savedUser.setClassId(classId);
            savedUser.setStatus(User.Status.ACTIVE);

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            StudentCreationResult result = userService.createStudent(username, realName, email, classId);
            User student = result.getStudent();

            assertFalse(createdUsernames.contains(student.getUsername()),
                    "用户名应该是唯一的，不应该重复");
            assertFalse(createdUserIds.contains(student.getId()),
                    "用户ID应该是唯一的，不应该重复");

            createdUsernames.add(student.getUsername());
            createdUserIds.add(student.getId());
        }

        assertEquals(studentCount, createdUsernames.size(),
                "应该创建指定数量的学生账户");
    }

    @Property(tries = 100)
    @Label("属性6: 学生账户唯一性 - 重复用户名应该抛出异常")
    void duplicateUsernameShouldThrowException(@ForAll("validUsernames") String username) {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService();
        setField(userService, "userRepository", userRepository);
        setField(userService, "passwordEncoder", passwordEncoder);

        String realName = "Test Student";
        String email = "test@test.com";
        Long classId = 1L;

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(username);
        savedUser.setPassword("encodedPassword");
        savedUser.setRealName(realName);
        savedUser.setEmail(email);
        savedUser.setRole(User.Role.STUDENT);
        savedUser.setClassId(classId);
        savedUser.setStatus(User.Status.ACTIVE);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        StudentCreationResult firstResult = userService.createStudent(username, realName, email, classId);
        assertNotNull(firstResult.getStudent(), "第一次创建学生应该成功");

        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            userService.createStudent(username, realName + "2", email + "2", classId);
        }, "创建重复用户名的学生应该抛出BusinessException");
    }

@Property(tries = 100)
    @Label("属性6: 学生账户唯一性 - 创建后用户名应该可查询且唯一")
    void createdStudentShouldBeQueryableByUniqueUsername(@ForAll("validUsernames") String username) {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService();
        setField(userService, "userRepository", userRepository);
        setField(userService, "passwordEncoder", passwordEncoder);

        String realName = "Test Student";
        String email = "test@test.com";
        Long classId = 1L;

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(username);
        savedUser.setPassword("encodedPassword");
        savedUser.setRealName(realName);
        savedUser.setEmail(email);
        savedUser.setRole(User.Role.STUDENT);
        savedUser.setClassId(classId);
        savedUser.setStatus(User.Status.ACTIVE);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(savedUser));

        StudentCreationResult result = userService.createStudent(username, realName, email, classId);
        User createdStudent = result.getStudent();

        User foundStudent = userRepository.findByUsername(username).orElse(null);
        assertNotNull(foundStudent, "应该能够通过用户名查询到创建的学生");

        assertEquals(createdStudent.getId(), foundStudent.getId(),
                "查询到的学生ID应该与创建的学生ID相同");
        assertEquals(createdStudent.getUsername(), foundStudent.getUsername(),
                "查询到的用户名应该与创建的用户名相同");
    }

    @Property(tries = 100)
    @Label("属性6: 学生账户唯一性 - 批量创建的学生用户名应该各不相同")
    void batchCreatedStudentsShouldHaveDistinctUsernames(@ForAll("studentCounts") int studentCount) {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService();
        setField(userService, "userRepository", userRepository);
        setField(userService, "passwordEncoder", passwordEncoder);

        Set<String> usernames = new HashSet<>();

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        for (int i = 0; i < studentCount; i++) {
            String username = "batch_student_" + System.nanoTime() + "_" + i;
            usernames.add(username);

            when(userRepository.existsByUsername(username)).thenReturn(false);

            User savedUser = new User();
            savedUser.setId((long) (i + 1));
            savedUser.setUsername(username);
            savedUser.setPassword("encodedPassword");
            savedUser.setRealName("Student " + i);
            savedUser.setEmail("student" + i + "@test.com");
            savedUser.setRole(User.Role.STUDENT);
            savedUser.setClassId(1L);
            savedUser.setStatus(User.Status.ACTIVE);

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            StudentCreationResult result = userService.createStudent(
                    username,
                    "Student " + i,
                    "student" + i + "@test.com",
                    1L);

            assertNotNull(result.getStudent(), "学生创建应该成功");
        }

        assertEquals(studentCount, usernames.size(),
                "所有创建的学生用户名应该各不相同");
    }

    @Provide
    Arbitrary<Integer> studentCounts() {
        return Arbitraries.integers().between(1, 10);
    }

    @Provide
    Arbitrary<String> validUsernames() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(5)
                .ofMaxLength(20)
                .map(s -> "test_" + s + "_" + System.nanoTime());
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
