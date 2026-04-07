package com.example.ancientstars.service;

import com.example.ancientstars.common.ErrorCode;
import com.example.ancientstars.dto.LoginRequest;
import com.example.ancientstars.dto.LoginResponse;
import com.example.ancientstars.entity.User;
import com.example.ancientstars.exception.BusinessException;
import com.example.ancientstars.repository.UserRepository;
import com.example.ancientstars.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务类
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * 用户登录
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成令牌
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(request.getUsername());

        // 获取用户信息
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getRole().name());
    }

    /**
     * 刷新访问令牌
     */
    public String refreshAccessToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        return tokenProvider.generateToken(username);
    }

    /**
     * 根据用户名获取用户
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 创建用户
     */
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * 更新用户
     */
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (userDetails.getRealName() != null) {
            user.setRealName(userDetails.getRealName());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getClassId() != null) {
            user.setClassId(userDetails.getClassId());
        }
        if (userDetails.getStatus() != null) {
            user.setStatus(userDetails.getStatus());
        }

        return userRepository.save(user);
    }

    /**
     * 禁用用户
     */
    @Transactional
    public void disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(User.Status.DISABLED);
        userRepository.save(user);
    }

    /**
     * 启用用户
     */
    @Transactional
    public void enableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);
    }

    /**
     * 生成初始密码（8位随机字符串）
     */
    public String generateInitialPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    /**
     * 创建学生账户
     */
    @Transactional
    public com.example.ancientstars.dto.StudentCreationResult createStudent(String username, String realName,
            String email, Long classId) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        String initialPassword = generateInitialPassword();

        User student = new User();
        student.setUsername(username);
        student.setPassword(passwordEncoder.encode(initialPassword));
        student.setRealName(realName);
        student.setEmail(email);
        student.setRole(User.Role.STUDENT);
        student.setClassId(classId);
        student.setStatus(User.Status.ACTIVE);

        User savedStudent = userRepository.save(student);

        return new com.example.ancientstars.dto.StudentCreationResult(savedStudent, initialPassword);
    }

    /**
     * 更新学生信息
     */
    @Transactional
    public User updateStudent(Long id, String realName, String email, Long classId) {
        User student = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (student.getRole() != User.Role.STUDENT) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (realName != null) {
            student.setRealName(realName);
        }
        if (email != null) {
            student.setEmail(email);
        }
        if (classId != null) {
            student.setClassId(classId);
        }

        return userRepository.save(student);
    }

    /**
     * 按班级查询学生列表
     */
    @Transactional(readOnly = true)
    public java.util.List<User> getStudentsByClass(Long classId) {
        if (classId == null) {
            return userRepository.findByRole(User.Role.STUDENT);
        }
        return userRepository.findByRoleAndClassId(User.Role.STUDENT, classId);
    }

    /**
     * 获取所有学生
     */
    @Transactional(readOnly = true)
    public java.util.List<User> getAllStudents() {
        return userRepository.findByRole(User.Role.STUDENT);
    }

    /**
     * 根据ID获取用户
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 用户注册
     */
    @Transactional
    public LoginResponse register(com.example.ancientstars.dto.RegisterRequest request) {
        log.info("开始注册用户: username={}, role={}", request.getUsername(), request.getRole());

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("用户名已存在: {}", request.getUsername());
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        try {
            // 创建新用户
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setRole(request.getRole());
            user.setClassId(request.getClassId());
            user.setStatus(User.Status.ACTIVE);

            log.info("保存用户到数据库");
            userRepository.save(user);
            log.info("用户保存成功: id={}", user.getId());

            // 自动登录
            log.info("开始自动登录");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成令牌
            log.info("生成访问令牌");
            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(request.getUsername());

            log.info("注册成功: username={}", request.getUsername());
            return new LoginResponse(
                    accessToken,
                    refreshToken,
                    user.getId(),
                    user.getUsername(),
                    user.getRole().name());
        } catch (Exception e) {
            log.error("注册失败: username={}, error={}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }
}
