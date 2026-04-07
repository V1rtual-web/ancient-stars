# 旧星背单词学习管理系统 - 项目框架说明

## 项目概述

旧星（Ancient Stars）是一个基于Spring Boot和Vue的背单词学习管理系统，支持教师管理端和学生端。

## 技术栈

### 后端

- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- MySQL 8.0+
- JWT (JSON Web Token)
- Lombok
- SpringDoc OpenAPI (Swagger)

### 前端（待开发）

- Vue 3
- Vue Router
- Vuex/Pinia
- Axios
- Element Plus

## 项目结构

```
src/main/java/com/example/ancientstars/
├── common/                 # 公共类
│   ├── ApiResponse.java   # 统一响应格式
│   └── ErrorCode.java     # 错误码枚举
├── config/                # 配置类
│   ├── OpenApiConfig.java # Swagger配置
│   ├── SecurityConfig.java# Security配置
│   └── WebConfig.java     # Web配置（CORS等）
├── controller/            # 控制器层
│   └── HealthController.java # 健康检查
├── dto/                   # 数据传输对象
├── entity/                # 实体类
├── exception/             # 异常处理
│   ├── BusinessException.java        # 业务异常
│   └── GlobalExceptionHandler.java   # 全局异常处理器
├── repository/            # 数据访问层
├── security/              # 安全相关
└── service/               # 服务层
```

## 配置说明

### 数据库配置

在 `application.properties` 中配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ancient_stars
spring.datasource.username=root
spring.datasource.password=root
```

### JWT配置

```properties
jwt.secret=YW5jaWVudFN0YXJzU2VjcmV0S2V5Rm9yVm9jYWJ1bGFyeUxlYXJuaW5nU3lzdGVtMjAyNA==
jwt.expiration=7200000          # 2小时
jwt.refresh-expiration=604800000 # 7天
```

## API文档

启动项目后访问：

- Swagger UI: http://localhost:8080/api/swagger-ui.html
- API Docs: http://localhost:8080/api/api-docs

## 统一响应格式

### 成功响应

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2024-01-01T12:00:00"
}
```

### 失败响应

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "1002",
    "message": "参数错误"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

## 错误码说明

| 错误码 | 说明         |
| ------ | ------------ |
| 1xxx   | 通用错误     |
| 2xxx   | 认证授权错误 |
| 3xxx   | 用户相关错误 |
| 4xxx   | 词汇相关错误 |
| 5xxx   | 任务相关错误 |
| 6xxx   | 数据库错误   |

## 运行项目

### 前置条件

1. JDK 17+
2. Maven 3.6+
3. MySQL 8.0+

### 启动步骤

1. 创建数据库：

```sql
CREATE DATABASE ancient_stars CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `application.properties` 中的数据库配置

3. 运行项目：

```bash
mvn spring-boot:run
```

4. 访问健康检查接口：

```
GET http://localhost:8080/api/health
```

## 下一步开发

参考 `.kiro/specs/vocabulary-learning-system/tasks.md` 中的任务列表，按顺序完成：

1. ✅ 任务1：搭建项目基础框架（已完成）
2. 任务2：实现数据库表结构
3. 任务3：实现用户认证与授权模块
4. ...

## 注意事项

1. 所有API接口都返回统一的 `ApiResponse` 格式
2. 使用 `@RestControllerAdvice` 进行全局异常处理
3. JWT令牌用于无状态认证
4. 使用Lombok简化代码
5. 使用SpringDoc自动生成API文档
