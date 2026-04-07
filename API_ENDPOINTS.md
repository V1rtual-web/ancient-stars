# API 端点清单

本文档列出了旧星背单词学习管理系统的所有 API 端点。

## 1. 认证管理 (AuthController)

**基础路径**: `/auth`

| 方法 | 端点            | 描述     | 认证 |
| ---- | --------------- | -------- | ---- |
| POST | `/auth/login`   | 用户登录 | 否   |
| POST | `/auth/refresh` | 刷新令牌 | 否   |
| POST | `/auth/logout`  | 用户登出 | 是   |

## 2. 系统健康检查 (HealthController)

**基础路径**: `/health`

| 方法 | 端点      | 描述     | 认证 |
| ---- | --------- | -------- | ---- |
| GET  | `/health` | 健康检查 | 否   |

## 3. 词汇管理 (VocabularyController)

**基础路径**: `/vocabulary`

| 方法   | 端点                             | 描述                 | 认证 | 角色    |
| ------ | -------------------------------- | -------------------- | ---- | ------- |
| POST   | `/vocabulary`                    | 创建词汇             | 是   | TEACHER |
| PUT    | `/vocabulary/{id}`               | 更新词汇             | 是   | TEACHER |
| DELETE | `/vocabulary/{id}`               | 删除词汇（软删除）   | 是   | TEACHER |
| GET    | `/vocabulary/{id}`               | 查询词汇详情         | 是   | ALL     |
| GET    | `/vocabulary`                    | 分页查询词汇         | 是   | ALL     |
| GET    | `/vocabulary/search`             | 搜索词汇             | 是   | ALL     |
| GET    | `/vocabulary/difficulty/{level}` | 按难度查询           | 是   | ALL     |
| POST   | `/vocabulary/import`             | 批量导入词汇（JSON） | 是   | TEACHER |
| POST   | `/vocabulary/import/csv`         | 从CSV导入词汇        | 是   | TEACHER |

## 4. 词汇表管理 (WordListController)

**基础路径**: `/wordlist`

| 方法   | 端点                                               | 描述             | 认证 | 角色    |
| ------ | -------------------------------------------------- | ---------------- | ---- | ------- |
| POST   | `/wordlist`                                        | 创建词汇表       | 是   | TEACHER |
| PUT    | `/wordlist/{id}`                                   | 更新词汇表       | 是   | TEACHER |
| DELETE | `/wordlist/{id}`                                   | 删除词汇表       | 是   | TEACHER |
| GET    | `/wordlist/{id}`                                   | 查询词汇表       | 是   | ALL     |
| GET    | `/wordlist/{id}/detail`                            | 查询词汇表详情   | 是   | ALL     |
| GET    | `/wordlist`                                        | 分页查询词汇表   | 是   | ALL     |
| GET    | `/wordlist/my`                                     | 查询我的词汇表   | 是   | TEACHER |
| GET    | `/wordlist/search`                                 | 搜索词汇表       | 是   | ALL     |
| POST   | `/wordlist/{wordListId}/vocabulary/{vocabularyId}` | 添加词汇到词汇表 | 是   | TEACHER |
| DELETE | `/wordlist/{wordListId}/vocabulary/{vocabularyId}` | 从词汇表移除词汇 | 是   | TEACHER |
| POST   | `/wordlist/{wordListId}/vocabularies`              | 批量添加词汇     | 是   | TEACHER |

## 5. 教师管理 (TeacherController)

**基础路径**: `/api/teacher`

| 方法 | 端点                                 | 描述         | 认证 | 角色    |
| ---- | ------------------------------------ | ------------ | ---- | ------- |
| POST | `/api/teacher/students`              | 创建学生账户 | 是   | TEACHER |
| GET  | `/api/teacher/students`              | 获取学生列表 | 是   | TEACHER |
| GET  | `/api/teacher/students/{id}`         | 获取学生详情 | 是   | TEACHER |
| PUT  | `/api/teacher/students/{id}`         | 更新学生信息 | 是   | TEACHER |
| POST | `/api/teacher/students/{id}/disable` | 禁用学生账户 | 是   | TEACHER |
| POST | `/api/teacher/students/{id}/enable`  | 启用学生账户 | 是   | TEACHER |

## 6. 任务管理 (TaskController)

**基础路径**: `/tasks`

| 方法   | 端点                          | 描述             | 认证 | 角色    |
| ------ | ----------------------------- | ---------------- | ---- | ------- |
| POST   | `/tasks`                      | 创建任务         | 是   | TEACHER |
| POST   | `/tasks/{taskId}/assign`      | 分配任务         | 是   | TEACHER |
| PUT    | `/tasks/{taskId}/progress`    | 更新任务进度     | 是   | ALL     |
| GET    | `/tasks/{taskId}`             | 获取任务详情     | 是   | ALL     |
| GET    | `/tasks/teacher/{teacherId}`  | 获取教师任务列表 | 是   | TEACHER |
| GET    | `/tasks/student/{studentId}`  | 获取学生任务列表 | 是   | ALL     |
| GET    | `/tasks/{taskId}/assignments` | 获取任务分配记录 | 是   | TEACHER |
| PUT    | `/tasks/{taskId}`             | 更新任务         | 是   | TEACHER |
| DELETE | `/tasks/{taskId}`             | 删除任务         | 是   | TEACHER |

## 7. 学习记录管理 (LearningRecordController)

**基础路径**: `/api/learning`

| 方法   | 端点                                                             | 描述               | 认证 | 角色             |
| ------ | ---------------------------------------------------------------- | ------------------ | ---- | ---------------- |
| POST   | `/api/learning/mark-mastery`                                     | 标记词汇掌握度     | 是   | STUDENT          |
| GET    | `/api/learning/student/{studentId}`                              | 获取学生学习记录   | 是   | TEACHER, STUDENT |
| GET    | `/api/learning/student/{studentId}/task/{taskId}`                | 获取任务学习记录   | 是   | TEACHER, STUDENT |
| GET    | `/api/learning/student/{studentId}/mastery/{masteryLevel}`       | 按掌握度查询       | 是   | TEACHER, STUDENT |
| GET    | `/api/learning/student/{studentId}/mastered-count`               | 统计已掌握词汇     | 是   | TEACHER, STUDENT |
| GET    | `/api/learning/student/{studentId}/task/{taskId}/mastered-count` | 统计任务已掌握词汇 | 是   | TEACHER, STUDENT |
| GET    | `/api/learning/{id}`                                             | 获取学习记录详情   | 是   | TEACHER, STUDENT |
| DELETE | `/api/learning/{id}`                                             | 删除学习记录       | 是   | TEACHER          |

## 8. 测试管理 (TestController)

**基础路径**: `/api/test`

| 方法 | 端点                            | 描述         | 认证 | 角色             |
| ---- | ------------------------------- | ------------ | ---- | ---------------- |
| POST | `/api/test/generate`            | 生成测试题目 | 是   | STUDENT          |
| POST | `/api/test/submit`              | 提交测试答案 | 是   | STUDENT          |
| GET  | `/api/test/history/{studentId}` | 查询历史成绩 | 是   | TEACHER, STUDENT |
| GET  | `/api/test/{testId}/details`    | 查询测试详情 | 是   | TEACHER, STUDENT |

## 9. 统计分析 (StatisticsController)

**基础路径**: `/api/statistics`

| 方法 | 端点                                  | 描述             | 认证 | 角色             |
| ---- | ------------------------------------- | ---------------- | ---- | ---------------- |
| GET  | `/api/statistics/student/{studentId}` | 获取学生个人统计 | 是   | TEACHER, STUDENT |
| GET  | `/api/statistics/class/{classId}`     | 获取班级统计     | 是   | TEACHER          |
| GET  | `/api/statistics/report/{studentId}`  | 生成学习报告     | 是   | TEACHER, STUDENT |

## 统计信息

- **总端点数**: 56
- **公开端点**: 4 (登录、刷新令牌、登出、健康检查)
- **需要认证的端点**: 52
- **仅教师可访问**: 26
- **仅学生可访问**: 3
- **教师和学生都可访问**: 23

## 认证说明

### 获取访问令牌

1. 调用 `/auth/login` 接口
2. 使用用户名和密码登录
3. 获取返回的 `accessToken`

### 使用访问令牌

在请求 Header 中添加：

```
Authorization: Bearer <your-access-token>
```

### 刷新令牌

当访问令牌过期时：

1. 调用 `/auth/refresh` 接口
2. 使用 `refreshToken` 获取新的访问令牌

## 响应格式

### 成功响应

```json
{
  "success": true,
  "data": {
    // 响应数据
  },
  "message": "操作成功",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### 错误响应

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

## 常见错误码

| 错误码         | HTTP状态码 | 描述             |
| -------------- | ---------- | ---------------- |
| UNAUTHORIZED   | 401        | 未认证或令牌无效 |
| FORBIDDEN      | 403        | 权限不足         |
| NOT_FOUND      | 404        | 资源不存在       |
| BAD_REQUEST    | 400        | 请求参数错误     |
| INTERNAL_ERROR | 500        | 服务器内部错误   |

## 分页参数

大部分列表查询接口支持分页参数：

| 参数      | 类型   | 默认值    | 描述                 |
| --------- | ------ | --------- | -------------------- |
| page      | int    | 0         | 页码（从0开始）      |
| size      | int    | 20        | 每页大小             |
| sortBy    | string | createdAt | 排序字段             |
| direction | string | DESC      | 排序方向（ASC/DESC） |

## 使用示例

### 1. 登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teacher01",
    "password": "password123"
  }'
```

### 2. 创建词汇

```bash
curl -X POST http://localhost:8080/api/vocabulary \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "word": "hello",
    "phonetic": "/həˈloʊ/",
    "translation": "你好",
    "exampleSentence": "Hello, world!",
    "difficultyLevel": 1
  }'
```

### 3. 查询学生任务

```bash
curl -X GET http://localhost:8080/api/tasks/student/1 \
  -H "Authorization: Bearer <token>"
```

## 相关文档

- [SWAGGER_GUIDE.md](./SWAGGER_GUIDE.md) - Swagger 使用指南
- [API_DOCUMENTATION_SUMMARY.md](./API_DOCUMENTATION_SUMMARY.md) - API 文档配置总结
