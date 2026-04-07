# 需求文档

## 介绍

学生自主背单词功能允许学生在不依赖教师分配任务的情况下，自主选择词汇进行学习。系统将记录学生的学习行为，基于艾宾浩斯遗忘曲线提供智能复习提醒，并将学习进度实时同步给教师，帮助教师了解学生的自主学习情况。

## 术语表

- **Self_Study_System**: 学生自主背单词系统
- **Student**: 学生用户
- **Teacher**: 教师用户
- **Vocabulary**: 词汇，包含单词、音标、释义、例句等信息
- **Study_Record**: 学习记录，记录学生背诵某个单词的时间和状态
- **Review_Reminder**: 复习提醒，基于艾宾浩斯遗忘曲线生成的复习通知
- **Daily_Progress**: 每日进度，记录学生每天背诵的单词数量
- **Progress_Report**: 进度报告，教师可查看的学生学习统计数据
- **Ebbinghaus_Curve**: 艾宾浩斯遗忘曲线，用于计算复习时间点的算法
- **Review_Scheduler**: 复习调度器，负责生成和发送复习提醒的后台服务

## 需求

### 需求 1: 学生自主选择词汇学习

**用户故事:** 作为学生，我想要自主选择词汇进行背诵学习，这样我可以根据自己的需求和兴趣安排学习内容

#### 验收标准

1. THE Self_Study_System SHALL 提供词汇库浏览界面，展示所有可用词汇
2. THE Self_Study_System SHALL 支持按词汇表分类浏览词汇
3. THE Self_Study_System SHALL 支持按关键词搜索词汇
4. WHEN Student 选择一个词汇开始学习, THE Self_Study_System SHALL 展示该词汇的完整信息（单词、音标、释义、例句）
5. WHEN Student 标记已掌握某个词汇, THE Self_Study_System SHALL 记录该学习行为

### 需求 2: 学习记录管理

**用户故事:** 作为系统，我需要准确记录学生背过的每个单词及背诵时间，这样可以为复习提醒和进度统计提供数据基础

#### 验收标准

1. WHEN Student 标记已掌握某个词汇, THE Self_Study_System SHALL 创建一条Study_Record，包含学生ID、词汇ID和当前时间戳
2. THE Self_Study_System SHALL 确保同一学生对同一词汇的Study_Record唯一性（幂等性）
3. IF Student 重复标记同一词汇, THEN THE Self_Study_System SHALL 更新该Study_Record的时间戳而不是创建新记录
4. THE Self_Study_System SHALL 持久化存储所有Study_Record到数据库
5. THE Self_Study_System SHALL 在数据库连接失败时返回错误信息并保证数据不丢失

### 需求 3: 艾宾浩斯遗忘曲线复习提醒

**用户故事:** 作为学生，我希望系统能在合适的时间提醒我复习已背过的单词，这样可以提高记忆效果

#### 验收标准

1. WHEN Student 标记已掌握某个词汇, THE Self_Study_System SHALL 基于Ebbinghaus_Curve计算该词汇的复习时间点
2. THE Self_Study_System SHALL 在词汇背诵后的第7天生成Review_Reminder
3. THE Review_Scheduler SHALL 每天定时检查需要复习的词汇
4. WHEN 复习时间到达, THE Self_Study_System SHALL 向Student发送复习提醒通知
5. THE Self_Study_System SHALL 在学生端界面展示待复习词汇列表
6. WHEN Student 完成复习, THE Self_Study_System SHALL 更新Study_Record并重新计算下一次复习时间

### 需求 4: 每日学习进度记录

**用户故事:** 作为学生，我想要查看自己每天背了多少个单词，这样可以了解自己的学习进度和坚持情况

#### 验收标准

1. THE Self_Study_System SHALL 按日期统计Student每天新学习的词汇数量
2. THE Self_Study_System SHALL 将每日学习数据存储为Daily_Progress记录
3. THE Self_Study_System SHALL 在学生端展示每日学习进度统计（今日已学、本周已学、累计已学）
4. THE Self_Study_System SHALL 提供日历视图展示每日学习情况
5. THE Self_Study_System SHALL 在每日学习数量达到0时显示鼓励提示

### 需求 5: 教师查看学生学习进度

**用户故事:** 作为教师，我想要实时查看学生的自主背单词进度，这样可以了解学生的学习积极性并提供针对性指导

#### 验收标准

1. THE Self_Study_System SHALL 实时同步Student的学习数据到Progress_Report
2. THE Self_Study_System SHALL 为Teacher提供学生学习进度查询接口
3. WHEN Teacher 查询某个学生的进度, THE Self_Study_System SHALL 返回该学生的每日学习数量统计
4. THE Self_Study_System SHALL 支持Teacher按时间范围筛选学生学习数据（最近7天、最近30天、自定义范围）
5. THE Self_Study_System SHALL 在教师端展示学生学习趋势图表
6. THE Self_Study_System SHALL 支持Teacher查看班级整体自主学习统计（平均每日学习量、学习最积极的学生）

### 需求 6: 复习调度器后台服务

**用户故事:** 作为系统管理员，我需要一个可靠的后台服务来处理复习提醒，确保学生能按时收到复习通知

#### 验收标准

1. THE Review_Scheduler SHALL 每天在固定时间（如每天早上8点）执行一次调度任务
2. THE Review_Scheduler SHALL 查询所有需要在当天复习的Study_Record
3. THE Review_Scheduler SHALL 为每个需要复习的Student生成Review_Reminder
4. IF Review_Scheduler 执行失败, THEN THE Self_Study_System SHALL 记录错误日志并在下次执行时重试
5. THE Review_Scheduler SHALL 在高并发情况下保证性能（处理10000条记录应在60秒内完成）

### 需求 7: 学习数据持久化与一致性

**用户故事:** 作为系统，我需要确保学习数据的准确性和一致性，避免数据丢失或重复

#### 验收标准

1. THE Self_Study_System SHALL 使用数据库事务确保Study_Record创建的原子性
2. WHEN 创建Study_Record和Daily_Progress, THE Self_Study_System SHALL 在同一事务中完成
3. IF 事务执行失败, THEN THE Self_Study_System SHALL 回滚所有数据变更
4. THE Self_Study_System SHALL 在Study_Record表上创建唯一索引（学生ID + 词汇ID）防止重复记录
5. THE Self_Study_System SHALL 定期备份学习数据（每天凌晨2点）

### 需求 8: API接口设计

**用户故事:** 作为前端开发者，我需要清晰的API接口来实现学生自主学习功能

#### 验收标准

1. THE Self_Study_System SHALL 提供RESTful API接口用于词汇浏览（GET /api/vocabularies）
2. THE Self_Study_System SHALL 提供API接口用于创建学习记录（POST /api/study-records）
3. THE Self_Study_System SHALL 提供API接口用于查询待复习词汇（GET /api/review-reminders）
4. THE Self_Study_System SHALL 提供API接口用于查询学生每日进度（GET /api/daily-progress）
5. THE Self_Study_System SHALL 提供API接口供教师查询学生进度（GET /api/teacher/student-progress）
6. THE Self_Study_System SHALL 在所有API响应中使用统一的JSON格式（包含code、message、data字段）
7. THE Self_Study_System SHALL 对所有API接口进行JWT认证
8. THE Self_Study_System SHALL 在API文档中说明所有接口的请求参数和响应格式

### 需求 9: 前端用户界面

**用户故事:** 作为学生，我需要友好的用户界面来进行自主学习

#### 验收标准

1. THE Self_Study_System SHALL 在学生端提供"自主学习"入口
2. THE Self_Study_System SHALL 提供词汇浏览页面，支持分页加载（每页20个词汇）
3. THE Self_Study_System SHALL 提供词汇学习卡片界面，支持左右滑动切换词汇
4. THE Self_Study_System SHALL 在学习卡片上提供"已掌握"按钮
5. THE Self_Study_System SHALL 在点击"已掌握"后显示成功提示并自动切换到下一个词汇
6. THE Self_Study_System SHALL 提供"待复习"页面展示需要复习的词汇列表
7. THE Self_Study_System SHALL 在学生端首页展示每日学习进度统计卡片
8. THE Self_Study_System SHALL 在教师端"学生管理"页面添加"自主学习进度"查看入口
9. THE Self_Study_System SHALL 在移动端和桌面端都提供良好的用户体验

### 需求 10: 性能与可扩展性

**用户故事:** 作为系统管理员，我需要系统能够支持大量学生同时使用

#### 验收标准

1. THE Self_Study_System SHALL 在1000个并发学生同时学习时保持响应时间在2秒以内
2. THE Self_Study_System SHALL 对词汇查询接口实现缓存机制（缓存时间24小时）
3. THE Self_Study_System SHALL 对学习记录写入操作进行批量处理优化
4. THE Self_Study_System SHALL 在数据库表上创建必要的索引以提高查询性能
5. THE Self_Study_System SHALL 支持水平扩展（可部署多个应用实例）
