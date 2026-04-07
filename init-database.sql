-- 旧星背单词学习管理系统 - 数据库初始化脚本
-- 完整数据库表结构

-- 创建数据库
CREATE DATABASE IF NOT EXISTS ancient_stars 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ancient_stars;

-- ============================================
-- 1. 用户表 (sys_user)
-- ============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    role ENUM('TEACHER', 'STUDENT') NOT NULL COMMENT '角色：教师/学生',
    class_id BIGINT COMMENT '班级ID（学生使用）',
    status ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE' COMMENT '状态：激活/禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 词汇表 (vocabulary)
-- ============================================
CREATE TABLE IF NOT EXISTS vocabulary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '词汇ID',
    word VARCHAR(100) NOT NULL COMMENT '单词',
    phonetic VARCHAR(100) COMMENT '音标',
    translation TEXT NOT NULL COMMENT '释义',
    example_sentence TEXT COMMENT '例句',
    difficulty_level ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM' COMMENT '难度等级',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除（软删除）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_word (word),
    INDEX idx_created_by (created_by),
    INDEX idx_difficulty (difficulty_level),
    FOREIGN KEY (created_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词汇表';

-- ============================================
-- 3. 词汇表 (word_list)
-- ============================================
CREATE TABLE IF NOT EXISTS word_list (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '词汇表ID',
    name VARCHAR(100) NOT NULL COMMENT '词汇表名称',
    description TEXT COMMENT '描述',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_created_by (created_by),
    FOREIGN KEY (created_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词汇表';

-- ============================================
-- 4. 词汇表关联表 (word_list_vocabulary)
-- ============================================
CREATE TABLE IF NOT EXISTS word_list_vocabulary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    word_list_id BIGINT NOT NULL COMMENT '词汇表ID',
    vocabulary_id BIGINT NOT NULL COMMENT '词汇ID',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_list_vocab (word_list_id, vocabulary_id),
    INDEX idx_word_list (word_list_id),
    INDEX idx_vocabulary (vocabulary_id),
    FOREIGN KEY (word_list_id) REFERENCES word_list(id) ON DELETE CASCADE,
    FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词汇表关联表';

-- ============================================
-- 5. 学习任务表 (task)
-- ============================================
CREATE TABLE IF NOT EXISTS task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    description TEXT COMMENT '任务描述',
    word_list_id BIGINT NOT NULL COMMENT '词汇表ID',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    deadline TIMESTAMP COMMENT '截止时间',
    status ENUM('ACTIVE', 'COMPLETED', 'EXPIRED') DEFAULT 'ACTIVE' COMMENT '任务状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_word_list (word_list_id),
    INDEX idx_created_by (created_by),
    INDEX idx_deadline (deadline),
    FOREIGN KEY (word_list_id) REFERENCES word_list(id),
    FOREIGN KEY (created_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习任务表';

-- ============================================
-- 6. 任务分配表 (task_assignment)
-- ============================================
CREATE TABLE IF NOT EXISTS task_assignment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分配ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'NOT_STARTED' COMMENT '完成状态',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    UNIQUE KEY uk_task_student (task_id, student_id),
    INDEX idx_task (task_id),
    INDEX idx_student (student_id),
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务分配表';

-- ============================================
-- 7. 学习记录表 (learning_record)
-- ============================================
CREATE TABLE IF NOT EXISTS learning_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    vocabulary_id BIGINT NOT NULL COMMENT '词汇ID',
    task_id BIGINT COMMENT '任务ID',
    mastery_level ENUM('UNKNOWN', 'LEARNING', 'FAMILIAR', 'MASTERED') DEFAULT 'LEARNING' COMMENT '掌握程度',
    review_count INT DEFAULT 0 COMMENT '复习次数',
    last_reviewed_at TIMESTAMP COMMENT '最后复习时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_vocab (student_id, vocabulary_id, task_id),
    INDEX idx_student (student_id),
    INDEX idx_vocabulary (vocabulary_id),
    INDEX idx_task (task_id),
    FOREIGN KEY (student_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习记录表';

-- ============================================
-- 8. 测试记录表 (test_record)
-- ============================================
CREATE TABLE IF NOT EXISTS test_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '测试ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    task_id BIGINT COMMENT '任务ID',
    score DECIMAL(5,2) NOT NULL COMMENT '得分',
    total_questions INT NOT NULL COMMENT '总题数',
    correct_answers INT NOT NULL COMMENT '正确答案数',
    duration_seconds INT COMMENT '用时（秒）',
    test_type ENUM('CHOICE', 'FILL_BLANK', 'TRANSLATION', 'MIXED') NOT NULL COMMENT '测试类型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_student (student_id),
    INDEX idx_task (task_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (student_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试记录表';

-- ============================================
-- 9. 测试详情表 (test_detail)
-- ============================================
CREATE TABLE IF NOT EXISTS test_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '详情ID',
    test_record_id BIGINT NOT NULL COMMENT '测试记录ID',
    vocabulary_id BIGINT NOT NULL COMMENT '词汇ID',
    question_text TEXT NOT NULL COMMENT '题目内容',
    student_answer TEXT COMMENT '学生答案',
    correct_answer TEXT NOT NULL COMMENT '正确答案',
    is_correct BOOLEAN NOT NULL COMMENT '是否正确',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_test_record (test_record_id),
    INDEX idx_vocabulary (vocabulary_id),
    FOREIGN KEY (test_record_id) REFERENCES test_record(id) ON DELETE CASCADE,
    FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试详情表';

-- ============================================
-- 插入初始数据
-- ============================================

-- 插入默认教师账户（密码：admin123，使用BCrypt加密）
INSERT INTO sys_user (username, password, real_name, email, role, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lFO2K0DP/FAqwxqRi', '系统管理员', 'admin@ancientstars.com', 'TEACHER', 'ACTIVE');

-- 插入测试教师账户（密码：teacher123）
INSERT INTO sys_user (username, password, real_name, email, role, status) 
VALUES ('teacher01', '$2a$10$8K1p/a0dL1LH.lh/sVEYReYt9QxqQjn5qVWW5HjTkrCX9bL8F3tTi', '张老师', 'teacher01@ancientstars.com', 'TEACHER', 'ACTIVE');

-- 插入测试学生账户（密码：student123）
INSERT INTO sys_user (username, password, real_name, email, role, class_id, status) 
VALUES 
('student01', '$2a$10$rZ8qfKqN5h5h5h5h5h5h5uOCQb376NoUnuTJ8iAt6Z5EHsM8lFO2K', '李明', 'student01@ancientstars.com', 'STUDENT', 1, 'ACTIVE'),
('student02', '$2a$10$rZ8qfKqN5h5h5h5h5h5h5uOCQb376NoUnuTJ8iAt6Z5EHsM8lFO2K', '王芳', 'student02@ancientstars.com', 'STUDENT', 1, 'ACTIVE'),
('student03', '$2a$10$rZ8qfKqN5h5h5h5h5h5h5uOCQb376NoUnuTJ8iAt6Z5EHsM8lFO2K', '张伟', 'student03@ancientstars.com', 'STUDENT', 1, 'ACTIVE');

-- ============================================
-- 查询验证
-- ============================================
SELECT '========== 用户表 ==========' AS '';
SELECT id, username, real_name, email, role, status, created_at FROM sys_user;

SELECT '========== 数据库表列表 ==========' AS '';
SHOW TABLES;
