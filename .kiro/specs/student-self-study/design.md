# 设计文档

## 系统架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue 3)                        │
├──────────────────┬──────────────────┬──────────────────────┤
│   学生自主学习    │   复习提醒页面    │   教师进度查看        │
│   - 词汇浏览      │   - 待复习列表    │   - 学生进度统计      │
│   - 学习卡片      │   - 复习完成      │   - 班级统计图表      │
│   - 进度统计      │                  │                      │
└──────────────────┴──────────────────┴──────────────────────┘
                            ↓ HTTP/REST API
┌─────────────────────────────────────────────────────────────┐
│                    应用层 (Spring Boot)                      │
├──────────────────┬──────────────────┬──────────────────────┤
│  学习记录服务     │   复习提醒服务    │   进度统计服务        │
│  - 创建学习记录   │   - 生成提醒      │   - 每日进度统计      │
│  - 查询学习历史   │   - 查询待复习    │   - 教师查询接口      │
└──────────────────┴──────────────────┴──────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    调度层 (Spring Scheduler)                 │
│  - 复习提醒调度器 (每天8:00执行)                             │
│  - 进度统计调度器 (每天0:00执行)                             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    数据层 (MySQL)                            │
│  - study_record (学习记录表)                                 │
│  - daily_progress (每日进度表)                               │
│  - review_reminder (复习提醒表)                              │
└─────────────────────────────────────────────────────────────┘
```

## 数据库设计

### 1. 学习记录表 (study_record)

存储学生背诵单词的记录。

```sql
CREATE TABLE study_record (
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
```

### 2. 每日进度表 (daily_progress)

统计学生每天的学习进度。

```sql
CREATE TABLE daily_progress (
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
```

### 3. 复习提醒表 (review_reminder)

存储需要复习的单词提醒。

```sql
CREATE TABLE review_reminder (
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
```

## API 接口设计

### 学生端接口

#### 1. 获取词汇列表

```
GET /api/student/vocabularies
```

**请求参数：**

- `page` (int, optional): 页码，默认1
- `size` (int, optional): 每页数量，默认20
- `wordListId` (long, optional): 词汇表ID筛选
- `keyword` (string, optional): 关键词搜索

**响应：**

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "word": "apple",
        "phonetic": "/ˈæpl/",
        "translation": "n. 苹果",
        "example": "I like apples.",
        "isLearned": false
      }
    ],
    "total": 100,
    "page": 1,
    "size": 20
  }
}
```

#### 2. 创建学习记录

```
POST /api/student/study-records
```

**请求体：**

```json
{
  "vocabularyId": 1
}
```

**响应：**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "studentId": 10,
    "vocabularyId": 1,
    "studyTime": "2026-04-01T10:30:00",
    "nextReviewTime": "2026-04-08T10:30:00"
  }
}
```

#### 3. 获取待复习词汇列表

```
GET /api/student/review-reminders
```

**响应：**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "vocabulary": {
        "id": 1,
        "word": "apple",
        "phonetic": "/ˈæpl/",
        "translation": "n. 苹果"
      },
      "remindTime": "2026-04-01T08:00:00",
      "daysSinceLastStudy": 7
    }
  ]
}
```

#### 4. 完成复习

```
POST /api/student/review-reminders/{id}/complete
```

**响应：**

```json
{
  "success": true,
  "data": {
    "message": "复习完成",
    "nextReviewTime": "2026-04-15T10:30:00"
  }
}
```

#### 5. 获取学习进度统计

```
GET /api/student/progress
```

**请求参数：**

- `startDate` (date, optional): 开始日期
- `endDate` (date, optional): 结束日期

**响应：**

```json
{
  "success": true,
  "data": {
    "todayNewWords": 15,
    "todayReviewWords": 8,
    "weekNewWords": 85,
    "totalLearnedWords": 320,
    "dailyProgress": [
      {
        "date": "2026-04-01",
        "newWords": 15,
        "reviewWords": 8
      }
    ]
  }
}
```

### 教师端接口

#### 6. 获取学生学习进度

```
GET /api/teacher/student-progress/{studentId}
```

**请求参数：**

- `startDate` (date, optional): 开始日期
- `endDate` (date, optional): 结束日期

**响应：**

```json
{
  "success": true,
  "data": {
    "studentId": 10,
    "studentName": "张三",
    "totalLearnedWords": 320,
    "averageDailyWords": 12,
    "dailyProgress": [
      {
        "date": "2026-04-01",
        "newWords": 15,
        "reviewWords": 8
      }
    ]
  }
}
```

#### 7. 获取班级学习统计

```
GET /api/teacher/class-progress
```

**请求参数：**

- `classId` (long, optional): 班级ID
- `startDate` (date, optional): 开始日期
- `endDate` (date, optional): 结束日期

**响应：**

```json
{
  "success": true,
  "data": {
    "classId": 1,
    "totalStudents": 30,
    "activeStudents": 25,
    "averageDailyWords": 10,
    "topStudents": [
      {
        "studentId": 10,
        "studentName": "张三",
        "totalWords": 320
      }
    ]
  }
}
```

## 核心算法设计

### 艾宾浩斯遗忘曲线复习算法

根据艾宾浩斯遗忘曲线，复习间隔设计如下：

```java
public class EbbinghausAlgorithm {

    /**
     * 计算下次复习时间
     * @param lastReviewTime 上次复习时间
     * @param reviewCount 已复习次数
     * @return 下次复习时间
     */
    public LocalDateTime calculateNextReviewTime(LocalDateTime lastReviewTime, int reviewCount) {
        int daysToAdd;

        switch (reviewCount) {
            case 0: // 第一次学习后
                daysToAdd = 1;  // 1天后复习
                break;
            case 1: // 第一次复习后
                daysToAdd = 2;  // 2天后复习
                break;
            case 2: // 第二次复习后
                daysToAdd = 4;  // 4天后复习
                break;
            case 3: // 第三次复习后
                daysToAdd = 7;  // 7天后复习
                break;
            case 4: // 第四次复习后
                daysToAdd = 15; // 15天后复习
                break;
            default: // 第五次及以后
                daysToAdd = 30; // 30天后复习
                break;
        }

        return lastReviewTime.plusDays(daysToAdd);
    }
}
```

**注意：** 根据需求，首次学习后7天提醒复习，可以调整为：

```java
case 0: // 第一次学习后
    daysToAdd = 7;  // 7天后复习
    break;
```

### 复习调度器设计

```java
@Component
public class ReviewScheduler {

    @Autowired
    private StudyRecordService studyRecordService;

    @Autowired
    private ReviewReminderService reviewReminderService;

    /**
     * 每天早上8点执行复习提醒生成任务
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void generateDailyReviewReminders() {
        log.info("开始生成每日复习提醒");

        LocalDateTime today = LocalDateTime.now();

        // 查询所有需要在今天复习的学习记录
        List<StudyRecord> recordsToReview = studyRecordService
            .findRecordsNeedReview(today);

        // 为每条记录生成复习提醒
        for (StudyRecord record : recordsToReview) {
            reviewReminderService.createReminder(
                record.getStudentId(),
                record.getVocabularyId(),
                record.getId(),
                today
            );
        }

        log.info("复习提醒生成完成，共生成{}条提醒", recordsToReview.size());
    }
}
```

## 前端页面设计

### 1. 学生自主学习页面 (`/student/self-study`)

**页面结构：**

```
┌─────────────────────────────────────────────┐
│  自主学习                          [进度统计] │
├─────────────────────────────────────────────┤
│  词汇表筛选: [全部 ▼]  搜索: [_______] [🔍]  │
├─────────────────────────────────────────────┤
│  ┌───────────────────────────────────────┐  │
│  │         apple                          │  │
│  │         /ˈæpl/                        │  │
│  │                                       │  │
│  │         n. 苹果                        │  │
│  │                                       │  │
│  │         I like apples.                │  │
│  │                                       │  │
│  │         [已掌握]  [下一个]             │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  进度: 15/100                               │
└─────────────────────────────────────────────┘
```

**功能：**

- 词汇卡片展示
- 左右滑动切换词汇
- 点击"已掌握"记录学习
- 实时显示学习进度

### 2. 复习提醒页面 (`/student/review`)

**页面结构：**

```
┌─────────────────────────────────────────────┐
│  待复习词汇 (8)                              │
├─────────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐    │
│  │ apple     /ˈæpl/                    │    │
│  │ n. 苹果                              │    │
│  │ 学习于: 7天前          [开始复习]    │    │
│  └─────────────────────────────────────┘    │
│  ┌─────────────────────────────────────┐    │
│  │ banana    /bəˈnɑːnə/                │    │
│  │ n. 香蕉                              │    │
│  │ 学习于: 7天前          [开始复习]    │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

### 3. 学习进度统计页面 (`/student/progress`)

**页面结构：**

```
┌─────────────────────────────────────────────┐
│  我的学习进度                                │
├─────────────────────────────────────────────┤
│  ┌──────┐  ┌──────┐  ┌──────┐              │
│  │ 今日  │  │ 本周  │  │ 累计  │              │
│  │  15  │  │  85  │  │ 320  │              │
│  │ 新学  │  │ 新学  │  │ 已学  │              │
│  └──────┘  └──────┘  └──────┘              │
├─────────────────────────────────────────────┤
│  学习趋势图                                  │
│  ┌─────────────────────────────────────┐    │
│  │  📊 每日学习量折线图                 │    │
│  └─────────────────────────────────────┘    │
├─────────────────────────────────────────────┤
│  学习日历                                    │
│  ┌─────────────────────────────────────┐    │
│  │  📅 日历热力图                       │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

### 4. 教师查看学生进度页面 (`/teacher/student-progress`)

**页面结构：**

```
┌─────────────────────────────────────────────┐
│  学生自主学习进度                            │
├─────────────────────────────────────────────┤
│  班级: [一班 ▼]  时间: [最近7天 ▼]          │
├─────────────────────────────────────────────┤
│  班级统计:                                   │
│  活跃学生: 25/30  平均每日: 10词             │
├─────────────────────────────────────────────┤
│  学生列表:                                   │
│  ┌─────────────────────────────────────┐    │
│  │ 张三    累计: 320词  日均: 12词      │    │
│  │ [查看详情]                           │    │
│  └─────────────────────────────────────┘    │
│  ┌─────────────────────────────────────┐    │
│  │ 李四    累计: 280词  日均: 10词      │    │
│  │ [查看详情]                           │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

## 技术实现细节

### 后端服务层设计

#### StudyRecordService

```java
@Service
public class StudyRecordService {

    @Transactional
    public StudyRecord createStudyRecord(Long studentId, Long vocabularyId) {
        // 检查是否已存在记录
        Optional<StudyRecord> existing = studyRecordRepository
            .findByStudentIdAndVocabularyId(studentId, vocabularyId);

        if (existing.isPresent()) {
            // 更新现有记录
            return updateStudyRecord(existing.get());
        }

        // 创建新记录
        StudyRecord record = new StudyRecord();
        record.setStudentId(studentId);
        record.setVocabularyId(vocabularyId);
        record.setStudyTime(LocalDateTime.now());
        record.setReviewCount(0);
        record.setNextReviewTime(calculateNextReviewTime(LocalDateTime.now(), 0));
        record.setMasteryLevel(MasteryLevel.NEW);

        StudyRecord saved = studyRecordRepository.save(record);

        // 更新每日进度
        updateDailyProgress(studentId, LocalDate.now(), true);

        return saved;
    }
}
```

### 前端状态管理

#### selfStudyStore.js

```javascript
import { defineStore } from "pinia";
import { selfStudyAPI } from "@/api/selfStudy";

export const useSelfStudyStore = defineStore("selfStudy", {
  state: () => ({
    vocabularies: [],
    currentIndex: 0,
    reviewReminders: [],
    progress: null,
  }),

  actions: {
    async loadVocabularies(params) {
      const response = await selfStudyAPI.getVocabularies(params);
      this.vocabularies = response.data.items;
    },

    async markAsLearned(vocabularyId) {
      await selfStudyAPI.createStudyRecord(vocabularyId);
      this.currentIndex++;
    },

    async loadReviewReminders() {
      const response = await selfStudyAPI.getReviewReminders();
      this.reviewReminders = response.data;
    },

    async loadProgress() {
      const response = await selfStudyAPI.getProgress();
      this.progress = response.data;
    },
  },
});
```

## 性能优化策略

### 1. 数据库优化

- 在 `study_record` 表的 `(student_id, vocabulary_id)` 上创建唯一索引
- 在 `next_review_time` 上创建索引以加速复习查询
- 在 `daily_progress` 表的 `(student_id, study_date)` 上创建唯一索引

### 2. 缓存策略

- 词汇列表使用 Redis 缓存，缓存时间 24 小时
- 学生进度统计使用缓存，缓存时间 5 分钟
- 复习提醒列表使用缓存，每次完成复习后清除缓存

### 3. 批量处理

- 复习提醒生成使用批量插入
- 每日进度统计使用批量更新

### 4. 异步处理

- 学习记录创建后，异步更新每日进度
- 复习提醒生成使用异步任务

## 安全性设计

### 1. 权限控制

- 学生只能访问自己的学习记录和进度
- 教师只能查看自己班级学生的进度
- 所有 API 接口都需要 JWT 认证

### 2. 数据验证

- 前端和后端都进行参数验证
- 防止 SQL 注入和 XSS 攻击
- 限制 API 调用频率（每分钟最多 60 次）

## 测试策略

### 1. 单元测试

- 测试艾宾浩斯算法的正确性
- 测试学习记录创建的幂等性
- 测试每日进度统计的准确性

### 2. 集成测试

- 测试完整的学习流程
- 测试复习提醒生成流程
- 测试教师查询学生进度

### 3. 性能测试

- 测试 1000 并发用户同时学习
- 测试复习调度器处理 10000 条记录的性能
- 测试数据库查询性能

## 部署方案

### 1. 数据库迁移

- 使用 Flyway 或 Liquibase 管理数据库版本
- 创建数据库迁移脚本

### 2. 应用部署

- 使用 Docker 容器化部署
- 配置定时任务调度器
- 配置日志收集和监控

### 3. 监控告警

- 监控复习调度器执行状态
- 监控 API 响应时间
- 监控数据库连接池状态
