package com.example.ancientstars.repository;

import com.example.ancientstars.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepository 测试类
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindUser() {
        // 创建测试用户
        User user = new User();
        user.setUsername("testteacher");
        user.setPassword("password123");
        user.setRealName("测试教师");
        user.setEmail("test@example.com");
        user.setRole(User.Role.TEACHER);
        user.setStatus(User.Status.ACTIVE);

        // 保存用户
        User savedUser = userRepository.save(user);

        // 验证保存成功
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testteacher");
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void testFindByUsername() {
        // 创建并保存用户
        User user = new User();
        user.setUsername("teacher01");
        user.setPassword("password123");
        user.setRealName("张老师");
        user.setRole(User.Role.TEACHER);
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        // 根据用户名查找
        Optional<User> found = userRepository.findByUsername("teacher01");

        // 验证查找结果
        assertThat(found).isPresent();
        assertThat(found.get().getRealName()).isEqualTo("张老师");
    }

    @Test
    void testExistsByUsername() {
        // 创建并保存用户
        User user = new User();
        user.setUsername("admin");
        user.setPassword("password123");
        user.setRole(User.Role.TEACHER);
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        // 验证用户名存在
        assertThat(userRepository.existsByUsername("admin")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    void testFindByEmail() {
        // 创建并保存用户
        User user = new User();
        user.setUsername("teacher02");
        user.setPassword("password123");
        user.setEmail("teacher02@example.com");
        user.setRole(User.Role.TEACHER);
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        // 根据邮箱查找
        Optional<User> found = userRepository.findByEmail("teacher02@example.com");

        // 验证查找结果
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("teacher02");
    }

    @Test
    void testUsernameUniqueness() {
        // 创建第一个用户
        User user1 = new User();
        user1.setUsername("duplicate");
        user1.setPassword("password123");
        user1.setRole(User.Role.TEACHER);
        user1.setStatus(User.Status.ACTIVE);
        userRepository.save(user1);

        // 尝试创建相同用户名的用户
        User user2 = new User();
        user2.setUsername("duplicate");
        user2.setPassword("password456");
        user2.setRole(User.Role.STUDENT);
        user2.setStatus(User.Status.ACTIVE);

        // 验证会抛出异常（由于唯一约束）
        try {
            userRepository.save(user2);
            userRepository.flush();
            // 如果没有抛出异常，测试失败
            assertThat(false).isTrue();
        } catch (Exception e) {
            // 预期会抛出异常
            assertThat(e).isNotNull();
        }
    }
}
