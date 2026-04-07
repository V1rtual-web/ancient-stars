# 数据库设置指南

## 完整数据库表结构

### 前置条件

- MySQL 8.0+ 已安装并运行
- 数据库用户：root
- 数据库密码：123456（可在 `application.properties` 中修改）

### 快速开始

#### 方法一：使用批处理脚本（Windows）

1. 双击运行 `test-db-connection.bat`
2. 脚本会自动测试数据库连接
3. 如果连接成功，选择 Y 初始化数据库

#### 方法二：手动执行SQL脚本

```bash
# 连接到MySQL
mysql -u root -p

# 执行初始化脚本
source init-database.sql;
```

#### 方法三：使用MySQL客户端工具

1. 打开 MySQL Workbench 或其他客户端工具
2. 连接到本地MySQL服务器
3. 打开 `init-database.sql` 文件
4. 执行脚本

### 数据库表结构

系统包含9个核心数据表：

#### 1. 用户表 (user)

存储教师和学生的基本信息

| 字段       | 类型         | 说明                  |
| ---------- | ------------ | --------------------- |
| id         | BIGINT       | 主键，自增            |
| username   | VARCHAR(50)  | 用户名，唯一          |
| password   | VARCHAR(255) | 密码（BCrypt加密）    |
| real_name  | VARCHAR(50)  | 真实姓名              |
| email      | VARCHAR(100) | 邮箱                  |
| role       | ENUM         | 角色：TEACHER/STUDENT |
| class_id   | BIGINT       | 班级ID（学生使用）    |
| status     | ENUM         | 状态：ACTIVE/DISABLED |
| created_at | TIMESTAMP    | 创建时间              |
| updated_at | TIMESTAMP    | 更新时间              |

#### 2. 词汇表 (vocabulary)

存储单词及其释义、音标等信息

| 字段             | 类型         | 说明                   |
| ---------------- | ------------ | ---------------------- |
| id               | BIGINT       | 主键，自增             |
| word             | VARCHAR(100) | 单词                   |
| phonetic         | VARCHAR(100) | 音标                   |
| translation      | TEXT         | 释义                   |
| example_sentence | TEXT         | 例句                   |
| difficulty_level | ENUM         | 难度：EASY/MEDIUM/HARD |
| created_by       | BIGINT       | 创建者ID               |
| is_deleted       | BOOLEAN      | 是否删除（软删除）     |
| created_at       | TIMESTAMP    | 创建时间               |
| updated_at       | TIMESTAMP    | 更新时间               |

#### 3. 词汇表 (word_list)

存储词汇表的基本信息

| 字段        | 类型         | 说明       |
| ----------- | ------------ | ---------- |
| id          | BIGINT       | 主键，自增 |
| name        | VARCHAR(100) | 词汇表名称 |
| description | TEXT         | 描述       |
| created_by  | BIGINT       | 创建者ID   |
| is_public   | BOOLEAN      | 是否公开   |
| created_at  | TIMESTAMP    | 创建时间   |
| updated_at  | TIMESTAMP    | 更新时间   |

#### 4. 词汇表关联表 (word_list_vocabulary)

关联词汇表和词汇的多对多关系

| 字段          | 类型      | 说明       |
| ------------- | --------- | ---------- |
| id            | BIGINT    | 主键，自增 |
| word_list_id  | BIGINT    | 词汇表ID   |
| vocabulary_id | BIGINT    | 词汇ID     |
| sort_order    | INT       | 排序顺序   |
| created_at    | TIMESTAMP | 创建时间   |

#### 5. 学习任务表 (task)

存储教师创建的学习任务

| 字段         | 类型         | 说明                           |
| ------------ | ------------ | ------------------------------ |
| id           | BIGINT       | 主键，自增                     |
| title        | VARCHAR(200) | 任务标题                       |
| description  | TEXT         | 任务描述                       |
| word_list_id | BIGINT       | 词汇表ID                       |
| created_by   | BIGINT       | 创建者ID                       |
| deadline     | TIMESTAMP    | 截止时间                       |
| status       | ENUM         | 状态：ACTIVE/COMPLETED/EXPIRED |
| created_at   | TIMESTAMP    | 创建时间                       |
| updated_at   | TIMESTAMP    | 更新时间                       |

#### 6. 任务分配表 (task_assignment)

记录任务分配给学生的情况

| 字段         | 类型      | 说明                                    |
| ------------ | --------- | --------------------------------------- |
| id           | BIGINT    | 主键，自增                              |
| task_id      | BIGINT    | 任务ID                                  |
| student_id   | BIGINT    | 学生ID                                  |
| progress     | INT       | 进度百分比                              |
| status       | ENUM      | 状态：NOT_STARTED/IN_PROGRESS/COMPLETED |
| assigned_at  | TIMESTAMP | 分配时间                                |
| completed_at | TIMESTAMP | 完成时间                                |

#### 7. 学习记录表 (learning_record)

记录学生的学习进度和掌握情况

| 字段             | 类型      | 说明                                         |
| ---------------- | --------- | -------------------------------------------- |
| id               | BIGINT    | 主键，自增                                   |
| student_id       | BIGINT    | 学生ID                                       |
| vocabulary_id    | BIGINT    | 词汇ID                                       |
| task_id          | BIGINT    | 任务ID                                       |
| mastery_level    | ENUM      | 掌握程度：UNKNOWN/LEARNING/FAMILIAR/MASTERED |
| review_count     | INT       | 复习次数                                     |
| last_reviewed_at | TIMESTAMP | 最后复习时间                                 |
| created_at       | TIMESTAMP | 创建时间                                     |
| updated_at       | TIMESTAMP | 更新时间                                     |

#### 8. 测试记录表 (test_record)

记录学生的测试成绩

| 字段             | 类型         | 说明                                    |
| ---------------- | ------------ | --------------------------------------- |
| id               | BIGINT       | 主键，自增                              |
| student_id       | BIGINT       | 学生ID                                  |
| task_id          | BIGINT       | 任务ID                                  |
| score            | DECIMAL(5,2) | 得分                                    |
| total_questions  | INT          | 总题数                                  |
| correct_answers  | INT          | 正确答案数                              |
| duration_seconds | INT          | 用时（秒）                              |
| test_type        | ENUM         | 测试类型：CHOICE/FILL_BLANK/TRANSLATION |
| created_at       | TIMESTAMP    | 创建时间                                |

#### 9. 测试详情表 (test_detail)

记录测试的详细题目和答案

| 字段           | 类型      | 说明       |
| -------------- | --------- | ---------- |
| id             | BIGINT    | 主键，自增 |
| test_record_id | BIGINT    | 测试记录ID |
| vocabulary_id  | BIGINT    | 词汇ID     |
| question_text  | TEXT      | 题目内容   |
| student_answer | TEXT      | 学生答案   |
| correct_answer | TEXT      | 正确答案   |
| is_correct     | BOOLEAN   | 是否正确   |
| created_at     | TIMESTAMP | 创建时间   |

### 表关系图

```
user (用户)
  ├─ 1:N → vocabulary (创建的词汇)
  ├─ 1:N → word_list (创建的词汇表)
  ├─ 1:N → task (创建的任务)
  ├─ 1:N → task_assignment (分配的任务)
  ├─ 1:N → learning_record (学习记录)
  └─ 1:N → test_record (测试记录)

word_list (词汇表)
  ├─ N:M → vocabulary (包含的词汇，通过 word_list_vocabulary)
  └─ 1:N → task (关联的任务)

task (任务)
  ├─ 1:N → task_assignment (任务分配)
  └─ 1:N → learning_record (学习记录)

test_record (测试记录)
  └─ 1:N → test_detail (测试详情)
```

### 默认账户

初始化脚本会创建以下测试账户：

| 用户名    | 密码       | 角色    | 说明                   |
| --------- | ---------- | ------- | ---------------------- |
| admin     | admin123   | TEACHER | 系统管理员             |
| teacher01 | teacher123 | TEACHER | 测试教师账户           |
| student01 | student123 | STUDENT | 测试学生账户1（班级1） |
| student02 | student123 | STUDENT | 测试学生账户2（班级1） |
| student03 | student123 | STUDENT | 测试学生账户3（班级1） |

### 验证安装

运行以下SQL验证数据库是否正确创建：

```sql
USE ancient_stars;

-- 查看所有表
SHOW TABLES;

-- 查看用户数据
SELECT id, username, real_name, role, status FROM user;

-- 查看表结构
DESCRIBE user;
DESCRIBE vocabulary;
DESCRIBE word_list;
```

### 配置说明

数据库连接配置位于 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ancient_stars
spring.datasource.username=root
spring.datasource.password=123456
```

如需修改数据库连接信息，请同时更新：

1. `application.properties` 文件
2. `test-db-connection.bat` 脚本（如果使用）

### 下一步

数据库创建完成后，可以：

1. 启动Spring Boot应用：`mvn spring-boot:run`
2. 访问 http://localhost:8080/api/health 测试应用是否正常运行
3. 访问 http://localhost:8080/api/swagger-ui.html 查看API文档
4. 使用默认账户登录系统

### 故障排查

#### 连接失败

- 检查MySQL服务是否运行：`net start MySQL80`（Windows）
- 验证用户名和密码是否正确
- 确认端口3306未被占用

#### 权限问题

如果遇到权限错误，执行：

```sql
GRANT ALL PRIVILEGES ON ancient_stars.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

#### 字符集问题

确保数据库使用 utf8mb4 字符集：

```sql
ALTER DATABASE ancient_stars CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 外键约束错误

如果遇到外键约束错误，确保：

1. 表的创建顺序正确（父表先于子表）
2. 外键引用的字段类型匹配
3. 引用的记录存在

### 数据库维护

#### 备份数据库

```bash
mysqldump -u root -p ancient_stars > backup.sql
```

#### 恢复数据库

```bash
mysql -u root -p ancient_stars < backup.sql
```

#### 清空所有表数据

```sql
USE ancient_stars;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE test_detail;
TRUNCATE TABLE test_record;
TRUNCATE TABLE learning_record;
TRUNCATE TABLE task_assignment;
TRUNCATE TABLE task;
TRUNCATE TABLE word_list_vocabulary;
TRUNCATE TABLE word_list;
TRUNCATE TABLE vocabulary;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;
```
