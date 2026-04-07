package com.example.ancientstars.service;

import com.example.ancientstars.entity.User;
import com.example.ancientstars.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 教师授权服务
 * 负责验证教师是否有权限访问特定学生的数据
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAuthorizationService {

    private final UserRepository userRepository;

    /**
     * 验证教师是否可以查看指定学生的数据
     * 教师只能查看自己班级的学生
     * 
     * @param teacherId 教师ID
     * @param studentId 学生ID
     * @throws AccessDeniedException 如果教师无权访问该学生
     */
    @Transactional(readOnly = true)
    public void verifyTeacherCanViewStudent(Long teacherId, Long studentId) {
        log.debug("验证教师 {} 是否可以查看学生 {} 的数据", teacherId, studentId);

        // 查询教师信息
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在: " + teacherId));

        // 查询学生信息
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在: " + studentId));

        // 验证学生是否属于教师的班级
        if (!isStudentInTeacherClass(teacher, student)) {
            log.warn("教师 {} 尝试访问不属于自己班级的学生 {} 的数据", teacherId, studentId);
            throw new AccessDeniedException("您无权查看该学生的数据");
        }

        log.debug("教师 {} 有权查看学生 {} 的数据", teacherId, studentId);
    }

    /**
     * 判断学生是否属于教师的班级
     * 
     * @param teacher 教师
     * @param student 学生
     * @return 是否属于同一班级
     */
    private boolean isStudentInTeacherClass(User teacher, User student) {
        // 如果教师或学生没有班级信息，返回 false
        if (teacher.getClassId() == null || student.getClassId() == null) {
            return false;
        }

        // 验证是否属于同一班级
        return teacher.getClassId().equals(student.getClassId());
    }

    /**
     * 获取教师的班级ID
     * 
     * @param teacherId 教师ID
     * @return 班级ID
     * @throws IllegalArgumentException 如果教师不存在或没有班级
     */
    @Transactional(readOnly = true)
    public Long getTeacherClassId(Long teacherId) {
        log.debug("获取教师 {} 的班级ID", teacherId);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在: " + teacherId));

        if (teacher.getClassId() == null) {
            throw new IllegalArgumentException("教师未分配班级");
        }

        return teacher.getClassId();
    }

    /**
     * 验证教师是否可以查看指定班级的数据
     * 教师只能查看自己的班级
     * 
     * @param teacherId 教师ID
     * @param classId   班级ID
     * @throws AccessDeniedException 如果教师无权访问该班级
     */
    @Transactional(readOnly = true)
    public void verifyTeacherCanViewClass(Long teacherId, Long classId) {
        log.debug("验证教师 {} 是否可以查看班级 {} 的数据", teacherId, classId);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("教师不存在: " + teacherId));

        if (teacher.getClassId() == null) {
            throw new IllegalArgumentException("教师未分配班级");
        }

        if (!teacher.getClassId().equals(classId)) {
            log.warn("教师 {} 尝试访问不属于自己的班级 {} 的数据", teacherId, classId);
            throw new AccessDeniedException("您无权查看该班级的数据");
        }

        log.debug("教师 {} 有权查看班级 {} 的数据", teacherId, classId);
    }
}
