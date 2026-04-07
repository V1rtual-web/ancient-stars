# 旧星背单词学习管理系统

一个基于Spring Boot和Vue的背单词学习管理系统，支持教师管理端和学生端两个模块。

## 功能特性

- 🚧 教师用户管理（登录、信息管理）
- 🚧 学生管理
- 🚧 词汇管理
- 🚧 学习任务分配
- 🚧 在线测试
- 🚧 学习数据统计

## 技术栈

### 后端

- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0+
- Maven

### 前端（待开发）

- Vue 3
- Element Plus

## 快速开始

### 1. 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置

#### 快速初始化（推荐）

**Windows用户：**

```bash
# 双击运行批处理脚本
test-db-connection.bat
```

**Linux/Mac用户：**

```bash
# 执行SQL脚本
mysql -u root -p < init-database.sql
```

详细的数据库设置说明请查看 [DATABASE_SETUP.md](DATABASE_SETUP.md)

#### 手动配置

如需修改数据库连接信息，编辑 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ancient_stars
spring.datasource.username=root
spring.datasource.password=你的密码
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080/api` 启动

### 4. 默认账户

数据库初始化后会创建以下测试账户：

**教师账户：**

- 系统管理员：`admin` / `admin123`
- 测试教师：`teacher01` / `teacher123`

**学生账户：**

- 学生1：`student01` / `student123`
- 学生2：`student02` / `student123`
- 学生3：`student03` / `student123`

## API文档

启动应用后，访问 Swagger UI：

```
http://localhost:8080/api/swagger-ui.html
```

## 项目结构

```
src/main/java/com/example/ancientstars/
├── common/          # 通用类（ApiResponse, ErrorCode等）
├── config/          # 配置类（Security, Swagger等）
├── controller/      # 控制器层
├── dto/             # 数据传输对象
├── entity/          # 实体类（当前：User）
├── exception/       # 异常类
├── repository/      # 数据访问层（当前：UserRepository）
├── security/        # 安全相关类（JWT等）
└── service/         # 服务层
```

## 数据库表结构

系统包含9个核心数据表：

### 核心表

1. **user** - 用户表（教师和学生）
2. **vocabulary** - 词汇表（单词信息）
3. **word_list** - 词汇表（词汇集合）
4. **word_list_vocabulary** - 词汇表关联表
5. **task** - 学习任务表
6. **task_assignment** - 任务分配表
7. **learning_record** - 学习记录表
8. **test_record** - 测试记录表
9. **test_detail** - 测试详情表

详细的表结构说明请查看 [DATABASE_SETUP.md](DATABASE_SETUP.md)

## 开发进度

- [x] 项目基础框架搭建
- [x] 完整数据库设计（9个核心表）
- [ ] 用户认证与授权模块
- [ ] 教师信息管理模块
- [ ] 学生管理模块
- [ ] 词汇管理模块
- [ ] 学习任务模块
- [ ] 测试与评估模块
- [ ] 统计分析模块
- [ ] Vue前端开发

## 测试

运行所有测试：

```bash
mvn test
```

## 许可证

MIT License

## 联系方式

如有问题，请提交Issue。
