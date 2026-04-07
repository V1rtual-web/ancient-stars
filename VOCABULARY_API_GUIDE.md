# 词汇管理模块 API 使用指南

## 概述

词汇管理模块已完整实现，包括词汇的增删改查、搜索、软删除、词汇表管理和CSV批量导入功能。

## 已实现的功能

### 1. 词汇管理 (VocabularyController)

#### 创建词汇

- **接口**: `POST /api/vocabulary`
- **权限**: 教师
- **请求体**:

```json
{
  "word": "hello",
  "phonetic": "/həˈloʊ/",
  "definition": "used as a greeting",
  "translation": "你好",
  "example": "Hello, how are you?",
  "difficultyLevel": 1
}
```

#### 更新词汇

- **接口**: `PUT /api/vocabulary/{id}`
- **权限**: 教师

#### 删除词汇（软删除）

- **接口**: `DELETE /api/vocabulary/{id}`
- **权限**: 教师
- **说明**: 词汇不会真正删除，只是标记为已删除

#### 查询词汇详情

- **接口**: `GET /api/vocabulary/{id}`
- **权限**: 所有用户

#### 分页查询词汇

- **接口**: `GET /api/vocabulary?page=0&size=20&sortBy=createdAt&direction=DESC`
- **权限**: 所有用户

#### 搜索词汇（模糊查询）

- **接口**: `GET /api/vocabulary/search?keyword=hello&page=0&size=20`
- **权限**: 所有用户
- **说明**: 支持按单词、释义、翻译模糊搜索

#### 按难度查询

- **接口**: `GET /api/vocabulary/difficulty/{level}`
- **权限**: 所有用户

#### 批量导入词汇（JSON）

- **接口**: `POST /api/vocabulary/import`
- **权限**: 教师
- **请求体**:

```json
{
  "vocabularies": [
    {
      "word": "hello",
      "phonetic": "/həˈloʊ/",
      "translation": "你好",
      "difficultyLevel": 1
    }
  ],
  "wordListId": 1
}
```

#### 从CSV导入词汇

- **接口**: `POST /api/vocabulary/import/csv`
- **权限**: 教师
- **参数**:
  - `file`: CSV文件
  - `wordListId`: 词汇表ID（可选）
- **CSV格式**:

```csv
word,phonetic,definition,translation,example,difficultyLevel
hello,/həˈloʊ/,used as a greeting,你好,"Hello, how are you?",1
```

### 2. 词汇表管理 (WordListController)

#### 创建词汇表

- **接口**: `POST /api/wordlist`
- **权限**: 教师
- **请求体**:

```json
{
  "name": "CET-4词汇",
  "description": "大学英语四级词汇表"
}
```

#### 更新词汇表

- **接口**: `PUT /api/wordlist/{id}`
- **权限**: 教师（仅创建者）

#### 删除词汇表

- **接口**: `DELETE /api/wordlist/{id}`
- **权限**: 教师（仅创建者）

#### 查询词汇表

- **接口**: `GET /api/wordlist/{id}`
- **权限**: 所有用户

#### 查询词汇表详情（含词汇列表）

- **接口**: `GET /api/wordlist/{id}/detail`
- **权限**: 所有用户
- **返回**: 词汇表信息 + 所有词汇列表

#### 分页查询所有词汇表

- **接口**: `GET /api/wordlist?page=0&size=20`
- **权限**: 所有用户

#### 查询我的词汇表

- **接口**: `GET /api/wordlist/my?page=0&size=20`
- **权限**: 教师

#### 搜索词汇表

- **接口**: `GET /api/wordlist/search?keyword=CET&page=0&size=20`
- **权限**: 所有用户

#### 添加词汇到词汇表

- **接口**: `POST /api/wordlist/{wordListId}/vocabulary/{vocabularyId}`
- **权限**: 教师

#### 从词汇表移除词汇

- **接口**: `DELETE /api/wordlist/{wordListId}/vocabulary/{vocabularyId}`
- **权限**: 教师

#### 批量添加词汇到词汇表

- **接口**: `POST /api/wordlist/{wordListId}/vocabularies`
- **权限**: 教师
- **请求体**: `[1, 2, 3, 4, 5]` (词汇ID数组)

## 数据库表结构

### vocabulary 表

- `id`: 主键
- `word`: 单词（唯一）
- `phonetic`: 音标
- `definition`: 英文释义
- `translation`: 中文翻译
- `example`: 例句
- `difficulty_level`: 难度级别
- `deleted`: 软删除标记
- `created_at`: 创建时间
- `updated_at`: 更新时间

### word_list 表

- `id`: 主键
- `name`: 词汇表名称
- `description`: 描述
- `creator_id`: 创建者ID
- `word_count`: 词汇数量
- `created_at`: 创建时间
- `updated_at`: 更新时间

### word_list_vocabulary 表（关联表）

- `id`: 主键
- `word_list_id`: 词汇表ID
- `vocabulary_id`: 词汇ID
- `sort_order`: 排序
- `created_at`: 创建时间

## 测试方法

### 1. 使用Swagger UI

访问: http://localhost:8080/api/swagger-ui.html

### 2. 使用Postman

1. 先登录获取Token: `POST /api/auth/login`
2. 在后续请求的Header中添加: `Authorization: Bearer {token}`

### 3. 使用提供的CSV示例

项目根目录下的 `vocabulary-sample.csv` 文件可用于测试批量导入功能。

## 特性说明

### 软删除

- 词汇删除使用软删除机制，不会真正从数据库删除
- 使用 `@SQLDelete` 和 `@Where` 注解实现
- 已删除的词汇不会出现在查询结果中

### 词汇表关联

- 词汇和词汇表是多对多关系
- 同一个词汇可以属于多个词汇表
- 词汇表会自动维护词汇数量

### 权限控制

- 教师可以创建、修改、删除词汇和词汇表
- 学生只能查看词汇和词汇表
- 词汇表只能由创建者修改和删除

### 搜索功能

- 支持按单词、释义、翻译进行模糊搜索
- 支持按难度级别筛选
- 支持分页和排序

## 下一步

根据tasks.md，接下来的任务是：

- 5.1 编写属性测试验证词汇软删除一致性 ✅
- 5.2 编写属性测试验证词汇表关联完整性 ✅
- 5.3 编写单元测试验证词汇管理功能 ✅
- 6. 实现学生管理模块
