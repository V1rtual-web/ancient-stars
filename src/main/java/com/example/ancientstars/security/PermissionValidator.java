package com.example.ancientstars.security;

import com.example.ancientstars.common.ErrorCode;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 权限验证工具类
 * 用于验证用户是否有权限访问特定资源
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionValidator {

    private final UserRepository userRepository;

    /**
     * 获取当前登录用户
     * 
     * @return 当前用户
     * @throws BusinessException 如果用户未登录或用户不存在
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在"));
    }

    /**
     * 获取当前登录用户ID
     * 
     * @return 当前用户ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * 验证学生只能访问自己的数据
     * 
     * @param studentId 要访问的学生ID
     * @throws BusinessException 如果当前用户不是该学生
     */
    public void validateStudentAccess(Long studentId) {
        User currentUser = getCurrentUser();

        // 验证用户角色是学生
        if (currentUser.getRole() != User.Role.STUDENT) {
            log.warn("非学生用户尝试访问学生数据 - 用户ID: {}, 角色: {}",
                    currentUser.getId(), currentUser.getRole());
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该资源");
        }

        // 验证学生只能访问自己的数据
        if (!currentUser.getId().equals(studentId)) {
            log.warn("学生尝试访问其他学生数据 - 当前用户ID: {}, 目标学生ID: {}",
                    currentUser.getId(), studentId);
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能访问自己的数据");
        }

        log.debug("学生权限验证通过 - 学生ID: {}", studentId);
    }

    /**
     * 验证教师只能查看自己班级的学生
     * 
     * @param studentId 要查看的学生ID
     * @throws BusinessException 如果教师无权查看该学生
     */
    public void validateTeacherAccessToStudent(Long studentId) {
        User currentUser = getCurrentUser();

        // 验证用户角色是教师
        if (currentUser.getRole() != User.Role.TEACHER) {
            log.warn("非教师用户尝试访问教师功能 - 用户ID: {}, 角色: {}",
                    currentUser.getId(), currentUser.getRole());
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该资源");
        }

        // 查询目标学生
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "学生不存在"));

        // 验证学生是否在教师的班级中
        if (student.getClassId() == null || !student.getClassId().equals(currentUser.getClassId())) {
            log.warn("教师尝试访问其他班级学生 - 教师ID: {}, 教师班级: {}, 学生ID: {}, 学生班级: {}",
                    currentUser.getId(), currentUser.getClassId(), studentId, student.getClassId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能查看自己班级的学生");
        }

        log.debug("教师权限验证通过 - 教师ID: {}, 学生ID: {}", currentUser.getId(), studentId);
    }

    /**
     * 验证教师只能查看自己的班级
     * 
     * @param classId 要查看的班级ID
     * @throws BusinessException 如果教师无权查看该班级
     */
    public void validateTeacherAccessToClass(Long classId) {
        User currentUser = getCurrentUser();

        // 验证用户角色是教师
        if (currentUser.getRole() != User.Role.TEACHER) {
            log.warn("非教师用户尝试访问教师功能 - 用户ID: {}, 角色: {}",
                    currentUser.getId(), currentUser.getRole());
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该资源");
        }

        // 验证班级ID是否匹配
        if (currentUser.getClassId() == null || !currentUser.getClassId().equals(classId)) {
            log.warn("教师尝试访问其他班级 - 教师ID: {}, 教师班级: {}, 目标班级: {}",
                    currentUser.getId(), currentUser.getClassId(), classId);
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能查看自己的班级");
        }

        log.debug("教师班级权限验证通过 - 教师ID: {}, 班级ID: {}", currentUser.getId(), classId);
    }

    /**
     * 验证用户是否为学生角色
     * 
     * @throws BusinessException 如果用户不是学生
     */
    public void validateStudentRole() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != User.Role.STUDENT) {
            log.warn("非学生用户尝试访问学生功能 - 用户ID: {}, 角色: {}",
                    currentUser.getId(), currentUser.getRole());
            throw new BusinessException(ErrorCode.FORBIDDEN, "该功能仅限学生使用");
        }

        log.debug("学生角色验证通过 - 用户ID: {}", currentUser.getId());
    }

    /**
     * 验证用户是否为教师角色
     * 
     * @throws BusinessException 如果用户不是教师
     */
    public void validateTeacherRole() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != User.Role.TEACHER) {
            log.warn("非教师用户尝试访问教师功能 - 用户ID: {}, 角色: {}",
                    currentUser.getId(), currentUser.getRole());
            throw new BusinessException(ErrorCode.FORBIDDEN, "该功能仅限教师使用");
        }

        log.debug("教师角色验证通过 - 用户ID: {}", currentUser.getId());
    }
}
