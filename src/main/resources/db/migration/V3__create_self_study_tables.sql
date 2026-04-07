-- 创建学习记录表
CREATE TABLE IF NOT EXISTS study_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    vocabulary_id BIGINT NOT NULL COMMENT '词汇ID',
    study_time DATETIME NOT NULL COMMENT '学习时间',
    review_count INT DEFAULT 0 COMMENT '复习次数',
    last_review_time DATETIME COMMENT '最后复习时间',
    next_review_time DATETIME COMMENT '下次复习时间',
    mastery_level ENUM('NEW', 'LEARNING', 'FAMILIAR', 'MASTERED') DEFAULT 'NEW' COMMENT '掌握程度',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_vocabulary (student_id, vocabulary_id),
    INDEX idx_student_id (student_id),
    INDEX idx_next_review_time (next_review_time),
    FOREIGN KEY (student_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- 创建每日进度表
CREATE TABLE IF NOT EXISTS daily_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    study_date DATE NOT NULL COMMENT '学习日期',
    new_words_count INT DEFAULT 0 COMMENT '新学单词数',
    review_words_count INT DEFAULT 0 COMMENT '复习单词数',
    total_study_time INT DEFAULT 0 COMMENT '总学习时长(秒)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_date (student_id, study_date),
    INDEX idx_student_id (student_id),
    INDEX idx_study_date (study_date),
    FOREIGN KEY (student_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日进度表';

-- 创建复习提醒表
CREATE TABLE IF NOT EXISTS review_reminder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    vocabulary_id BIGINT NOT NULL COMMENT '词汇ID',
    study_record_id BIGINT NOT NULL COMMENT '学习记录ID',
    remind_time DATETIME NOT NULL COMMENT '提醒时间',
    status ENUM('PENDING', 'COMPLETED', 'SKIPPED') DEFAULT 'PENDING' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_student_remind (student_id, remind_time, status),
    INDEX idx_remind_time (remind_time),
    FOREIGN KEY (student_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE,
    FOREIGN KEY (study_record_id) REFERENCES study_record(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复习提醒表';
