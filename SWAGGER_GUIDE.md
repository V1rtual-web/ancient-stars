# Swagger API 文档使用指南

## 概述

旧星背单词学习管理系统已配置完整的 Swagger/OpenAPI 文档，提供交互式 API 文档和测试界面。

## 访问方式

### 1. Swagger UI 界面

启动应用后，访问以下地址：

```
http://localhost:8080/api/swagger-ui.html
```

或者使用简化路径：

```
http://localhost:8080/api/swagger-ui/
```

### 2. OpenAPI JSON 文档

获取原始 OpenAPI 规范文档：

```
http://localhost:8080/api/v3/api-docs
```

## 功能特性

### 已配置的 API 模块

1. **认证管理** (`/auth`)
   - 用户登录
   - 令牌刷新
   - 用户登出

2. **词汇管理** (`/vocabulary`)
   - 词汇的增删改查
   - 词汇搜索
   - 批量导入（JSON/CSV）

3. **词汇表管理** (`/wordlist`)
   - 词汇表创建和管理
   - 词汇添加/移除
   - 批量操作

4. **教师管理** (`/api/teacher`)
   - 学生账户管理
   - 学生信息编辑
   - 账户启用/禁用

5. **任务管理** (`/tasks`)
   - 任务创建和分配
   - 进度跟踪
   - 任务查询

6. **学习记录管理** (`/api/learning`)
   - 学习进度记录
   - 掌握度标记
   - 学习历史查询

7. **测试管理** (`/api/test`)
   - 测试题目生成
   - 答案提交和评分
   - 历史成绩查询

8. **统计分析** (`/api/statistics`)
   - 学生个人统计
   - 班级统计
   - 学习报告生成

9. **系统健康检查** (`/health`)
   - 系统状态检查

## 使用说明

### 1. 认证配置

大部分 API 需要 JWT 认证。使用步骤：

1. 首先调用 `/auth/login` 接口获取访问令牌
2. 点击页面右上角的 "Authorize" 按钮
3. 在弹出框中输入：`Bearer <your-token>`
4. 点击 "Authorize" 确认
5. 现在可以测试需要认证的接口了

### 2. 测试 API

1. 展开任意 API 端点
2. 点击 "Try it out" 按钮
3. 填写必需的参数
4. 点击 "Execute" 执行请求
5. 查看响应结果

### 3. 查看模型定义

在页面底部的 "Schemas" 部分可以查看所有数据模型的定义，包括：

- 请求 DTO
- 响应 DTO
- 实体类

## 配置说明

### application.properties 配置

```properties
# Swagger配置
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.operations-sorter=alpha
```

### 安全配置

SecurityConfig 已配置允许匿名访问 Swagger 相关路径：

```java
.requestMatchers("/auth/**", "/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
```

## API 响应格式

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

## 注意事项

1. **开发环境**：Swagger UI 默认在所有环境启用
2. **生产环境**：建议在生产环境禁用 Swagger UI 或添加访问控制
3. **API 版本**：当前 API 版本为 1.0.0
4. **认证令牌**：JWT 令牌有效期为 2 小时

## 故障排除

### 无法访问 Swagger UI

1. 确认应用已启动：`http://localhost:8080/api/health`
2. 检查端口是否被占用
3. 查看应用日志是否有错误信息

### 认证失败

1. 确认令牌格式正确：`Bearer <token>`
2. 检查令牌是否过期
3. 确认用户角色权限是否匹配

### API 调用失败

1. 检查请求参数是否完整
2. 确认数据格式是否正确
3. 查看响应错误信息

## 相关文档

- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [SpringDoc OpenAPI 文档](https://springdoc.org/)
- [OpenAPI 规范](https://swagger.io/specification/)

## 联系方式

如有问题，请联系开发团队：

- Email: support@ancientstars.com
- 项目地址: [GitHub Repository]
