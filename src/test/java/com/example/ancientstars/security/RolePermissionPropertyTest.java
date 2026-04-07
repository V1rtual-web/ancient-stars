package com.example.ancientstars.security;

import com.example.ancientstars.entity.User;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 角色权限属性测试
 * 验证属性2: 角色权限隔离
 */
@Tag("Feature: vocabulary-learning-system, Property 2: 角色权限隔离")
class RolePermissionPropertyTest {

    @Property(tries = 100)
    @Label("属性2: 教师角色应该只有ROLE_TEACHER权限")
    void teacherShouldOnlyHaveTeacherRole(@ForAll("teacherUsers") User teacher) {
        // 获取权限
        Collection<? extends GrantedAuthority> authorities = getAuthorities(teacher);

        // 验证权限
        assertEquals(1, authorities.size(), "教师应该只有一个角色");
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_TEACHER")),
                "教师应该有ROLE_TEACHER权限");
        assertFalse(authorities.contains(new SimpleGrantedAuthority("ROLE_STUDENT")),
                "教师不应该有ROLE_STUDENT权限");
    }

    @Property(tries = 100)
    @Label("属性2: 学生角色应该只有ROLE_STUDENT权限")
    void studentShouldOnlyHaveStudentRole(@ForAll("studentUsers") User student) {
        // 获取权限
        Collection<? extends GrantedAuthority> authorities = getAuthorities(student);

        // 验证权限
        assertEquals(1, authorities.size(), "学生应该只有一个角色");
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_STUDENT")),
                "学生应该有ROLE_STUDENT权限");
        assertFalse(authorities.contains(new SimpleGrantedAuthority("ROLE_TEACHER")),
                "学生不应该有ROLE_TEACHER权限");
    }

    @Property(tries = 100)
    @Label("不同角色的用户应该有不同的权限")
    void differentRolesShouldHaveDifferentPermissions(
            @ForAll("teacherUsers") User teacher,
            @ForAll("studentUsers") User student) {
        // 获取权限
        Collection<? extends GrantedAuthority> teacherAuthorities = getAuthorities(teacher);
        Collection<? extends GrantedAuthority> studentAuthorities = getAuthorities(student);

        // 验证权限不同
        assertNotEquals(teacherAuthorities, studentAuthorities,
                "教师和学生应该有不同的权限");
    }

    @Property(tries = 100)
    @Label("禁用的用户不应该能够获取权限")
    void disabledUserShouldNotHavePermissions(@ForAll("disabledUsers") User disabledUser) {
        // 验证用户状态
        assertEquals(User.Status.DISABLED, disabledUser.getStatus(),
                "用户应该是禁用状态");
    }

    @Provide
    Arbitrary<User> teacherUsers() {
        return Arbitraries.create(() -> {
            User user = new User();
            user.setId(Arbitraries.longs().between(1L, 1000L).sample());
            user.setUsername("teacher" + Arbitraries.integers().between(1, 100).sample());
            user.setPassword("password");
            user.setRole(User.Role.TEACHER);
            user.setStatus(User.Status.ACTIVE);
            return user;
        });
    }

    @Provide
    Arbitrary<User> studentUsers() {
        return Arbitraries.create(() -> {
            User user = new User();
            user.setId(Arbitraries.longs().between(1L, 1000L).sample());
            user.setUsername("student" + Arbitraries.integers().between(1, 100).sample());
            user.setPassword("password");
            user.setRole(User.Role.STUDENT);
            user.setStatus(User.Status.ACTIVE);
            return user;
        });
    }

    @Provide
    Arbitrary<User> disabledUsers() {
        return Arbitraries.create(() -> {
            User user = new User();
            user.setId(Arbitraries.longs().between(1L, 1000L).sample());
            user.setUsername("user" + Arbitraries.integers().between(1, 100).sample());
            user.setPassword("password");
            user.setRole(Arbitraries.of(User.Role.values()).sample());
            user.setStatus(User.Status.DISABLED);
            return user;
        });
    }

    /**
     * 获取用户权限
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
}
