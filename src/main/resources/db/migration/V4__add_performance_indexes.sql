-- 性能优化索引验证和补充
-- 本迁移文件用于验证和补充性能优化所需的索引

-- 验证 study_record 表索引
-- 已存在的索引：
-- - uk_student_vocabulary (student_id, vocabulary_id) - 唯一索引，防止重复记录
-- - idx_student_id (student_id) - 用于按学生查询
-- - idx_next_review_time (next_review_time) - 用于复习提醒查询

-- 添加复合索引以优化复习提醒查询
CREATE INDEX IF NOT EXISTS idx_next_review_status 
ON study_record(next_review_time, student_id) 
COMMENT '优化复习提醒查询性能';

-- 验证 daily_progress 表索引
-- 已存在的索引：
-- - uk_student_date (student_id, study_date) - 唯一索引，防止重复记录
-- - idx_student_id (student_id) - 用于按学生查询
-- - idx_study_date (study_date) - 用于按日期查询

-- 添加复合索引以优化日期范围查询
CREATE INDEX IF NOT EXISTS idx_student_date_range 
ON daily_progress(student_id, study_date) 
COMMENT '优化学生进度日期范围查询';

-- 验证 review_reminder 表索引
-- 已存在的索引：
-- - idx_student_remind (student_id, remind_time, status) - 复合索引，用于查询待复习列表
-- - idx_remind_time (remind_time) - 用于按提醒时间查询

-- 添加状态索引以优化按状态查询
CREATE INDEX IF NOT EXISTS idx_status_remind_time 
ON review_reminder(status, remind_time) 
COMMENT '优化按状态和时间查询复习提醒';

-- 添加学习记录ID索引以优化关联查询
CREATE INDEX IF NOT EXISTS idx_study_record_id 
ON review_reminder(study_record_id) 
COMMENT '优化复习提醒与学习记录的关联查询';
