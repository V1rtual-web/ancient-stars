# 设计文档 - 旧星背单词学习管理系统

## 概述

旧星背单词学习管理系统采用前后端分离架构，后端使用Spring Boot框架提供RESTful API服务，前端使用Vue.js构建单页应用，数据存储使用MySQL数据库。系统分为教师管理端和学生端两个主要模块，支持词汇管理、学习任务分配、在线测试和学习数据统计等功能。

## 架构设计

### 整体架构

系统采用三层架构模式：

```
┌─────────────────────────────────────┐
│         前端层 (Vue.js)              │
│  - 教师管理界面                       │
│  - 学生学习界面                       │
│  - 路由管理 (Vue Router)             │
│  - 状态管理 (Vuex)                   │
└─────────────────────────────────────┘
              ↓ HTTP/HTTPS
┌─────────────────────────────────────┐
│      应用层 (Spring Boot)            │
│  - Controller (API接口)              │
│  - Service (业务逻辑)                │
│  - Security (认证授权)               │
└─────────────────────────────────────┘
              ↓ JDBC
┌─────────────────────────────────────┐
│       数据层 (MySQL)                 │
│  - 用户表                            │
│  - 词汇表                            │
│  - 学习记录表                        │
└─────────────────────────────────────┘
```

### 技术栈

**后端:**

- Spring Boot 2.7+
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0+
- Maven

**前端:**

- Vue 3
- Vue Router
- Vuex/Pinia
- Axios
- Element Plus

## 组件和接口

### 后端组件结构

```
com.example.ancientstars
├── controller          # 控制器层
│   ├── AuthController
│   ├── TeacherController
│   ├── StudentController
│   ├── VocabularyController
│   ├── TaskController
│   └── StatisticsController
├── service            # 服务层
│   ├── UserService
│   ├── VocabularyService
│   ├── TaskService
│   ├── LearningRecordService
│   └── StatisticsService
├── repository         # 数据访问层
│   ├── UserRepository
│   ├── VocabularyRepository
│   ├── WordListRepository
│   ├── TaskRepository
│   └── LearningRecordRepository
├── entity            # 实体类
│   ├── User
│   ├── Vocabulary
│   ├── WordList
│   ├── Task
│   └── LearningRecord
├── dto               # 数据传输对象
│   ├── LoginRequest
│   ├── VocabularyDTO
│   ├── TaskDTO
│   └── StatisticsDTO
├── security          # 安全配置
│   ├── JwtTokenProvider
│   ├── JwtAuthenticationFilter
│   └── SecurityConfig
└── config            # 配置类
    ├── WebConfig
    └── SwaggerConfig
```

### 核心接口定义

#### 1. 认证接口

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request);

    // 用户登出
    @PostMapping("/logout")
    public ResponseEntity<Void> logout();

    // 刷新令牌
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest request);
}
```

#### 2. 词汇管理接口

```java
@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyController {

    // 创建词汇
    @PostMapping
    public ResponseEntity<VocabularyDTO> createVocabulary(@RequestBody VocabularyDTO dto);

    // 更新词汇
    @PutMapping("/{id}")
    public ResponseEntity<VocabularyDTO> updateVocabulary(@PathVariable Long id, @RequestBody VocabularyDTO dto);

    // 删除词汇
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVocabulary(@PathVariable Long id);

    // 查询词汇
    @GetMapping("/{id}")
    public ResponseEntity<VocabularyDTO> getVocabulary(@PathVariable Long id);

    // 搜索词汇
    @GetMapping("/search")
    public ResponseEntity<Page<VocabularyDTO>> searchVocabulary(@RequestParam String keyword, Pageable pageable);

    // 批量导入
    @PostMapping("/import")
    public ResponseEntity<ImportResult> importVocabulary(@RequestParam("file") MultipartFile file);

    // 创建词汇表
    @PostMapping("/wordlist")
    public ResponseEntity<WordListDTO> createWordList(@RequestBody WordListDTO dto);
}
```

#### 3. 学习任务接口

```java
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    // 创建任务
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO dto);

    // 分配任务给学生
    @PostMapping("/{taskId}/assign")
    public ResponseEntity<Void> assignTask(@PathVariable Long taskId, @RequestBody List<Long> studentIds);

    // 查看任务详情
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable Long id);

    // 查看学生的任务列表
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<TaskDTO>> getStudentTasks(@PathVariable Long studentId);

    // 更新任务进度
    @PutMapping("/{taskId}/progress")
    public ResponseEntity<Void> updateProgress(@PathVariable Long taskId, @RequestBody ProgressDTO dto);
}
```

#### 4. 学习记录接口

```java
@RestController
@RequestMapping("/api/learning")
public class LearningRecordController {

    // 记录学习进度
    @PostMapping("/record")
    public ResponseEntity<LearningRecordDTO> recordLearning(@RequestBody LearningRecordDTO dto);

    // 提交测试
    @PostMapping("/test/submit")
    public ResponseEntity<TestResultDTO> submitTest(@RequestBody TestSubmissionDTO dto);

    // 查看学习历史
    @GetMapping("/history/{studentId}")
    public ResponseEntity<List<LearningRecordDTO>> getLearningHistory(@PathVariable Long studentId);
}
```

#### 5. 统计分析接口

```java
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    // 学生个人统计
    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentStatisticsDTO> getStudentStatistics(@PathVariable Long studentId);

    // 班级统计
    @GetMapping("/class/{classId}")
    public ResponseEntity<ClassStatisticsDTO> getClassStatistics(@PathVariable Long classId);

    // 生成学习报告
    @GetMapping("/report/{studentId}")
    public ResponseEntity<ReportDTO> generateReport(@PathVariable Long studentId, @RequestParam String period);
}
```

## 数据模型

### 数据库表设计

#### 1. 用户表 (user)

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    role ENUM('TEACHER', 'STUDENT') NOT NULL,
    class_id BIGINT,
    status ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_class_id (class_id)
);
```

#### 2. 词汇表 (vocabulary)

```sql
CREATE TABLE vocabulary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL,
    phonetic VARCHAR(100),
    translation TEXT NOT NULL,
    example_sentence TEXT,
    difficulty_level ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM',
    created_by BIGINT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_word (word),
    INDEX idx_created_by (created_by),
    INDEX idx_difficulty (difficulty_level),
    FOREIGN KEY (created_by) REFERENCES user(id)
);
```

#### 3. 词汇表 (word_list)

```sql
CREATE TABLE word_list (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_by BIGINT NOT NULL,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_created_by (created_by),
    FOREIGN KEY (created_by) REFERENCES user(id)
);
```

#### 4. 词汇表关联表 (word_list_vocabulary)

```sql
CREATE TABLE word_list_vocabulary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    word_list_id BIGINT NOT NULL,
    vocabulary_id BIGINT NOT NULL,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_list_vocab (word_list_id, vocabulary_id),
    INDEX idx_word_list (word_list_id),
    INDEX idx_vocabulary (vocabulary_id),
    FOREIGN KEY (word_list_id) REFERENCES word_list(id) ON DELETE CASCADE,
    FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE
);
```

#### 5. 学习任务表 (task)

```sql
CREATE TABLE task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    word_list_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    deadline TIMESTAMP,
    status ENUM('ACTIVE', 'COMPLETED', 'EXPIRED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_word_list (word_list_id),
    INDEX idx_created_by (created_by),
    INDEX idx_deadline (deadline),
    FOREIGN KEY (word_list_id) REFERENCES word_list(id),
    FOREIGN KEY (created_by) REFERENCES user(id)
);
```

#### 6. 任务分配表 (task_assignment)

```sql
CREATE TABLE task_assignment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    progress INT DEFAULT 0,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'NOT_STARTED',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    UNIQUE KEY uk_task_student (task_id, student_id),
    INDEX idx_task (task_id),
    INDEX idx_student (student_id),
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES user(id) ON DELETE CASCADE
);
```

#### 7. 学习记录表 (learning_record)

```sql
CREATE TABLE learning_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    vocabulary_id BIGINT NOT NULL,
    task_id BIGINT,
    mastery_level ENUM('UNKNOWN', 'LEARNING', 'FAMILIAR', 'MASTERED') DEFAULT 'LEARNING',
    review_count INT DEFAULT 0,
    last_reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_vocab (student_id, vocabulary_id, task_id),
    INDEX idx_student (student_id),
    INDEX idx_vocabulary (vocabulary_id),
    INDEX idx_task (task_id),
    FOREIGN KEY (student_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE SET NULL
);
```

#### 8. 测试记录表 (test_record)

```sql
CREATE TABLE test_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    task_id BIGINT,
    score DECIMAL(5,2) NOT NULL,
    total_questions INT NOT NULL,
    correct_answers INT NOT NULL,
    duration_seconds INT,
    test_type ENUM('CHOICE', 'FILL_BLANK', 'TRANSLATION') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_student (student_id),
    INDEX idx_task (task_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (student_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE SET NULL
);
```

#### 9. 测试详情表 (test_detail)

```sql
CREATE TABLE test_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    test_record_id BIGINT NOT NULL,
    vocabulary_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    student_answer TEXT,
    correct_answer TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_test_record (test_record_id),
    INDEX idx_vocabulary (vocabulary_id),
    FOREIGN KEY (test_record_id) REFERENCES test_record(id) ON DELETE CASCADE,
    FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE
);
```

### 实体关系图

```
User (用户)
  ├─ 1:N → Vocabulary (创建的词汇)
  ├─ 1:N → WordList (创建的词汇表)
  ├─ 1:N → Task (创建的任务)
  ├─ 1:N → TaskAssignment (分配的任务)
  ├─ 1:N → LearningRecord (学习记录)
  └─ 1:N → TestRecord (测试记录)

WordList (词汇表)
  ├─ N:M → Vocabulary (包含的词汇)
  └─ 1:N → Task (关联的任务)

Task (任务)
  ├─ 1:N → TaskAssignment (任务分配)
  └─ 1:N → LearningRecord (学习记录)

TestRecord (测试记录)
  └─ 1:N → TestDetail (测试详情)
```

## 正确性属性

_属性是关于系统应该做什么的特征或行为，在所有有效执行中都应该成立。属性是人类可读规范和机器可验证正确性保证之间的桥梁。_

### 属性 1: 用户认证令牌有效性

*对于任何*有效的用户凭据，登录后生成的JWT令牌应该能够通过验证并正确解析出用户信息

**验证需求: 1.1**

### 属性 2: 角色权限隔离

*对于任何*API请求，教师角色不应该能够访问仅限学生的接口，学生角色不应该能够访问仅限教师的接口

**验证需求: 1.3**

### 属性 3: 词汇数据完整性

*对于任何*创建或更新的词汇，必须包含单词和释义字段，且单词字段不能为空字符串

**验证需求: 2.1, 2.2**

### 属性 4: 词汇软删除一致性

*对于任何*被删除的词汇，其is_deleted标志应该被设置为true，且该词汇不应该出现在正常的查询结果中

**验证需求: 2.3**

### 属性 5: 词汇表关联完整性

*对于任何*词汇表，添加词汇后，通过词汇表ID查询应该能够返回所有已添加的词汇

**验证需求: 2.4**

### 属性 6: 学生账户唯一性

*对于任何*新创建的学生账户，其用户名必须在系统中唯一，不能与已有用户重复

**验证需求: 3.1**

### 属性 7: 任务分配通知一致性

*对于任何*被分配任务的学生，task_assignment表中应该存在对应的记录，且状态为NOT_STARTED

**验证需求: 4.2**

### 属性 8: 任务进度计算正确性

*对于任何*学习任务，学生的进度百分比应该等于(已掌握词汇数 / 任务总词汇数) × 100

**验证需求: 4.3**

### 属性 9: 学习记录幂等性

*对于任何*词汇，同一学生对同一任务中的同一词汇多次标记掌握，应该只更新现有记录而不创建重复记录

**验证需求: 5.3**

### 属性 10: 测试评分准确性

*对于任何*提交的测试，计算的分数应该等于(正确答案数 / 总题数) × 100

**验证需求: 6.2**

### 属性 11: 测试题目随机性

*对于任何*两次独立的测试生成，即使来自同一词汇表，题目顺序应该不同（除非词汇表只有一个词）

**验证需求: 6.1**

### 属性 12: 错题记录完整性

*对于任何*测试，所有答错的题目都应该在test_detail表中有对应记录，且is_correct字段为false

**验证需求: 6.3**

### 属性 13: 统计数据一致性

*对于任何*学生，其个人统计中的掌握词汇数应该等于learning_record表中该学生mastery_level为MASTERED的记录数

**验证需求: 7.1**

### 属性 14: 数据库事务原子性

*对于任何*涉及多表操作的业务（如创建任务并分配给学生），要么所有操作都成功，要么所有操作都回滚

**验证需求: 8.1**

### 属性 15: API响应格式一致性

*对于任何*API响应，成功时应该返回统一的JSON格式包含data字段，失败时应该返回包含error字段的JSON格式

**验证需求: 9.2, 9.3**

## 错误处理

### 异常处理策略

1. **全局异常处理器**: 使用@ControllerAdvice统一处理异常
2. **自定义异常**: 定义业务异常类（如VocabularyNotFoundException, UnauthorizedException等）
3. **错误响应格式**:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "错误描述",
    "timestamp": "2024-01-01T12:00:00Z"
  }
}
```

### 常见错误场景

- **认证失败**: 返回401 Unauthorized
- **权限不足**: 返回403 Forbidden
- **资源不存在**: 返回404 Not Found
- **参数验证失败**: 返回400 Bad Request
- **服务器错误**: 返回500 Internal Server Error
- **数据库连接失败**: 记录日志并返回503 Service Unavailable

## 测试策略

### 单元测试

- 使用JUnit 5和Mockito进行单元测试
- 测试Service层的业务逻辑
- 测试Repository层的数据访问
- 覆盖率目标: 80%以上

### 属性测试

使用jqwik进行基于属性的测试，验证系统的正确性属性：

**配置要求**:

- 每个属性测试至少运行100次迭代
- 每个测试必须标注对应的设计文档属性
- 标签格式: `@Tag("Feature: vocabulary-learning-system, Property N: [属性描述]")`

**测试重点**:

- 数据完整性属性（属性3, 4, 5, 6）
- 业务逻辑正确性（属性8, 9, 10, 11）
- 安全性属性（属性1, 2）
- 数据一致性（属性13, 14, 15）

### 集成测试

- 使用Spring Boot Test进行集成测试
- 使用H2内存数据库进行测试
- 测试API接口的完整流程
- 测试数据库事务和并发场景

### 前端测试

- 使用Jest进行单元测试
- 使用Vue Test Utils测试组件
- 使用Cypress进行E2E测试

## 安全设计

### 认证机制

- 使用JWT (JSON Web Token)进行无状态认证
- Token有效期: 2小时
- Refresh Token有效期: 7天
- 密码使用BCrypt加密存储

### 授权机制

- 基于角色的访问控制(RBAC)
- 使用Spring Security的@PreAuthorize注解
- 教师权限: 管理词汇、学生、任务
- 学生权限: 查看任务、学习词汇、参加测试

### 数据安全

- SQL注入防护: 使用JPA参数化查询
- XSS防护: 前端输入验证和输出转义
- CSRF防护: 使用CSRF Token
- HTTPS加密传输

## 性能优化

### 数据库优化

- 合理使用索引
- 分页查询避免全表扫描
- 使用连接池管理数据库连接
- 定期清理过期数据

### 缓存策略

- 使用Redis缓存热点数据
- 缓存词汇表数据
- 缓存用户会话信息
- 设置合理的过期时间

### 前端优化

- 路由懒加载
- 组件按需加载
- 图片懒加载
- 使用CDN加速静态资源
