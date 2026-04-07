# 需求文档 - 旧星背单词学习管理系统

## 简介

旧星是一个基于Spring Boot和Vue的背单词学习管理系统，支持教师管理端和学生端两个模块。系统使用MySQL数据库存储数据，提供完整的词汇学习和管理功能。

## 术语表

- **System**: 旧星背单词学习管理系统
- **Teacher**: 教师用户，负责管理词汇、学生和学习任务
- **Student**: 学生用户，使用系统学习词汇
- **Vocabulary**: 词汇，包含单词、释义、例句等信息
- **Word_List**: 词汇表，由教师创建的词汇集合
- **Learning_Task**: 学习任务，教师分配给学生的学习计划
- **Learning_Record**: 学习记录，学生的学习进度和成绩
- **Backend**: Spring Boot后端服务
- **Frontend**: Vue前端应用
- **Database**: MySQL数据库

## 需求

### 需求 1: 用户认证与授权

**用户故事:** 作为系统用户，我希望能够安全地登录系统，以便访问相应权限的功能。

#### 验收标准

1. WHEN 用户提交有效的用户名和密码 THEN THE System SHALL 验证凭据并返回认证令牌
2. WHEN 用户提交无效的凭据 THEN THE System SHALL 拒绝访问并返回错误信息
3. WHEN 认证用户访问资源 THEN THE System SHALL 根据用户角色（教师或学生）授权访问
4. WHEN 用户登出 THEN THE System SHALL 使认证令牌失效
5. WHERE 用户选择记住登录状态 THEN THE System SHALL 延长会话有效期

### 需求 2: 教师管理词汇

**用户故事:** 作为教师，我希望能够创建和管理词汇表，以便为学生提供学习材料。

#### 验收标准

1. WHEN 教师创建新词汇 THEN THE System SHALL 存储词汇信息（单词、音标、释义、例句）
2. WHEN 教师编辑已有词汇 THEN THE System SHALL 更新词汇信息并保留修改历史
3. WHEN 教师删除词汇 THEN THE System SHALL 标记词汇为已删除状态
4. WHEN 教师创建词汇表 THEN THE System SHALL 允许添加多个词汇到词汇表中
5. WHEN 教师搜索词汇 THEN THE System SHALL 返回匹配的词汇列表
6. THE System SHALL 支持批量导入词汇（CSV或Excel格式）

### 需求 3: 教师管理学生

**用户故事:** 作为教师，我希望能够管理学生账户，以便组织教学活动。

#### 验收标准

1. WHEN 教师创建学生账户 THEN THE System SHALL 生成唯一的学生ID和初始密码
2. WHEN 教师查看学生列表 THEN THE System SHALL 显示所有学生的基本信息
3. WHEN 教师编辑学生信息 THEN THE System SHALL 更新学生资料
4. WHEN 教师禁用学生账户 THEN THE System SHALL 阻止该学生登录系统
5. THE System SHALL 支持按班级或分组管理学生

### 需求 4: 教师分配学习任务

**用户故事:** 作为教师，我希望能够为学生分配学习任务，以便指导学生的学习进度。

#### 验收标准

1. WHEN 教师创建学习任务 THEN THE System SHALL 关联词汇表、目标学生和截止日期
2. WHEN 教师分配任务给学生 THEN THE System SHALL 通知相关学生
3. WHEN 教师查看任务完成情况 THEN THE System SHALL 显示每个学生的进度统计
4. WHEN 教师修改任务 THEN THE System SHALL 更新任务信息并通知学生
5. WHEN 任务截止日期到达 THEN THE System SHALL 自动标记未完成任务

### 需求 5: 学生学习词汇

**用户故事:** 作为学生，我希望能够学习分配的词汇，以便提高词汇量。

#### 验收标准

1. WHEN 学生查看学习任务 THEN THE System SHALL 显示待学习的词汇列表
2. WHEN 学生学习词汇 THEN THE System SHALL 显示单词、音标、释义和例句
3. WHEN 学生标记词汇为已掌握 THEN THE System SHALL 记录学习进度
4. WHEN 学生进行词汇测试 THEN THE System SHALL 随机选择词汇进行测验
5. THE System SHALL 支持多种学习模式（浏览模式、记忆模式、测试模式）

### 需求 6: 学生测试与评估

**用户故事:** 作为学生，我希望能够通过测试检验学习效果，以便了解自己的掌握程度。

#### 验收标准

1. WHEN 学生开始测试 THEN THE System SHALL 从任务词汇表中随机生成测试题
2. WHEN 学生提交答案 THEN THE System SHALL 评分并显示正确答案
3. WHEN 测试完成 THEN THE System SHALL 保存测试成绩和错题记录
4. THE System SHALL 支持多种题型（选择题、填空题、英译汉、汉译英）
5. WHEN 学生查看历史成绩 THEN THE System SHALL 显示所有测试记录和统计分析

### 需求 7: 学习记录与统计

**用户故事:** 作为用户，我希望能够查看学习数据统计，以便了解学习效果。

#### 验收标准

1. WHEN 学生查看个人统计 THEN THE System SHALL 显示学习时长、掌握词汇数、测试成绩趋势
2. WHEN 教师查看班级统计 THEN THE System SHALL 显示整体学习进度和成绩分布
3. WHEN 生成学习报告 THEN THE System SHALL 包含详细的学习数据和可视化图表
4. THE System SHALL 实时更新学习记录
5. THE System SHALL 支持按时间范围筛选统计数据

### 需求 8: 数据持久化

**用户故事:** 作为系统管理员，我希望所有数据能够可靠地存储在数据库中，以便保证数据安全和一致性。

#### 验收标准

1. WHEN 数据写入数据库 THEN THE System SHALL 使用事务确保数据一致性
2. WHEN 数据库连接失败 THEN THE System SHALL 返回错误信息并记录日志
3. THE System SHALL 使用MySQL存储所有业务数据
4. THE System SHALL 定期备份数据库
5. WHEN 并发操作发生 THEN THE System SHALL 使用乐观锁或悲观锁防止数据冲突

### 需求 9: API接口设计

**用户故事:** 作为前端开发者，我希望后端提供RESTful API，以便前端能够方便地调用服务。

#### 验收标准

1. THE Backend SHALL 提供RESTful风格的API接口
2. WHEN API被调用 THEN THE Backend SHALL 返回JSON格式的响应
3. WHEN API调用失败 THEN THE Backend SHALL 返回标准的错误码和错误信息
4. THE Backend SHALL 使用JWT进行API认证
5. THE Backend SHALL 提供API文档（Swagger/OpenAPI）

### 需求 10: 前端界面

**用户故事:** 作为用户，我希望界面友好易用，以便快速完成操作。

#### 验收标准

1. THE Frontend SHALL 使用Vue框架构建单页应用
2. WHEN 用户操作界面 THEN THE Frontend SHALL 提供即时的视觉反馈
3. THE Frontend SHALL 适配不同屏幕尺寸（响应式设计）
4. WHEN 数据加载时 THEN THE Frontend SHALL 显示加载状态
5. THE Frontend SHALL 提供清晰的导航和操作提示
