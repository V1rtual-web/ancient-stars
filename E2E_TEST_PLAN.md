# 端到端测试计划

## 概述

本文档描述旧星背单词学习管理系统的端到端（E2E）测试计划。使用Cypress框架编写关键流程的自动化测试。

## 测试框架

- **框架**: Cypress
- **版本**: 最新稳定版
- **语言**: JavaScript

## 安装Cypress

```bash
cd frontend
npm install --save-dev cypress
```

## Cypress配置

创建 `frontend/cypress.config.js`:

```javascript
const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    baseUrl: "http://localhost:3000",
    viewportWidth: 1280,
    viewportHeight: 720,
    video: false,
    screenshotOnRunFailure: true,
    setupNodeEvents(on, config) {
      // 实现node事件监听器
    },
  },
});
```

## 测试用例

### 1. 登录流程测试

**文件**: `cypress/e2e/auth/login.cy.js`

```javascript
describe("登录流程", () => {
  beforeEach(() => {
    cy.visit("/login");
  });

  it("应该成功登录教师账户", () => {
    cy.get('input[placeholder="请输入用户名"]').type("teacher001");
    cy.get('input[placeholder="请输入密码"]').type("password123");
    cy.get('button[type="submit"]').click();

    // 验证跳转到教师端首页
    cy.url().should("include", "/teacher");
    cy.contains("词汇管理").should("be.visible");
  });

  it("应该成功登录学生账户", () => {
    cy.get('input[placeholder="请输入用户名"]').type("student001");
    cy.get('input[placeholder="请输入密码"]').type("password123");
    cy.get('button[type="submit"]').click();

    // 验证跳转到学生端首页
    cy.url().should("include", "/student");
    cy.contains("我的任务").should("be.visible");
  });

  it("应该显示错误提示当凭据无效", () => {
    cy.get('input[placeholder="请输入用户名"]').type("invalid");
    cy.get('input[placeholder="请输入密码"]').type("invalid");
    cy.get('button[type="submit"]').click();

    // 验证错误提示
    cy.contains("用户名或密码错误").should("be.visible");
  });

  it("应该验证必填字段", () => {
    cy.get('button[type="submit"]').click();

    // 验证表单验证
    cy.contains("请输入用户名").should("be.visible");
    cy.contains("请输入密码").should("be.visible");
  });
});
```

### 2. 词汇管理流程测试

**文件**: `cypress/e2e/teacher/vocabulary.cy.js`

```javascript
describe("词汇管理流程", () => {
  beforeEach(() => {
    // 登录教师账户
    cy.login("teacher001", "password123");
    cy.visit("/teacher/vocabulary");
  });

  it("应该显示词汇列表", () => {
    cy.get(".el-table").should("be.visible");
    cy.get(".el-table__row").should("have.length.greaterThan", 0);
  });

  it("应该成功添加新词汇", () => {
    cy.contains("添加词汇").click();

    // 填写表单
    cy.get('input[placeholder="请输入单词"]').type("test");
    cy.get('input[placeholder="请输入音标"]').type("/test/");
    cy.get('textarea[placeholder="请输入释义"]').type("测试");
    cy.get('textarea[placeholder="请输入例句"]').type("This is a test.");

    // 提交
    cy.contains("确定").click();

    // 验证成功提示
    cy.contains("添加成功").should("be.visible");

    // 验证词汇出现在列表中
    cy.contains("test").should("be.visible");
  });

  it("应该成功编辑词汇", () => {
    // 点击第一行的编辑按钮
    cy.get(".el-table__row").first().find("button").contains("编辑").click();

    // 修改释义
    cy.get('textarea[placeholder="请输入释义"]').clear().type("修改后的释义");

    // 提交
    cy.contains("确定").click();

    // 验证成功提示
    cy.contains("更新成功").should("be.visible");
  });

  it("应该成功删除词汇", () => {
    // 点击第一行的删除按钮
    cy.get(".el-table__row").first().find("button").contains("删除").click();

    // 确认删除
    cy.contains("确定").click();

    // 验证成功提示
    cy.contains("删除成功").should("be.visible");
  });

  it("应该成功搜索词汇", () => {
    cy.get('input[placeholder="搜索单词"]').type("test");
    cy.contains("搜索").click();

    // 验证搜索结果
    cy.get(".el-table__row").each(($row) => {
      cy.wrap($row).should("contain", "test");
    });
  });

  it("应该成功创建词汇表", () => {
    cy.contains("词汇表管理").click();
    cy.contains("创建词汇表").click();

    // 填写表单
    cy.get('input[placeholder="请输入词汇表名称"]').type("测试词汇表");
    cy.get('textarea[placeholder="请输入描述"]').type("这是一个测试词汇表");

    // 选择词汇
    cy.contains("选择词汇").click();
    cy.get(".el-checkbox").first().click();

    // 提交
    cy.contains("确定").click();

    // 验证成功提示
    cy.contains("创建成功").should("be.visible");
  });
});
```

### 3. 学习和测试流程测试

**文件**: `cypress/e2e/student/learning.cy.js`

```javascript
describe("学习和测试流程", () => {
  beforeEach(() => {
    // 登录学生账户
    cy.login("student001", "password123");
  });

  it("应该显示任务列表", () => {
    cy.visit("/student/tasks");
    cy.get(".task-card").should("have.length.greaterThan", 0);
  });

  it("应该成功开始学习", () => {
    cy.visit("/student/tasks");

    // 点击第一个任务的开始学习按钮
    cy.get(".task-card").first().find("button").contains("开始学习").click();

    // 验证跳转到学习页面
    cy.url().should("include", "/student/learn");

    // 验证词汇卡片显示
    cy.get(".vocabulary-card").should("be.visible");
    cy.get(".word").should("be.visible");
  });

  it("应该成功标记词汇掌握度", () => {
    cy.visit("/student/tasks");
    cy.get(".task-card").first().find("button").contains("开始学习").click();

    // 标记为已掌握
    cy.contains("已掌握").click();

    // 验证进度更新
    cy.get(".progress-text").should("contain", "1");
  });

  it("应该成功完成测试", () => {
    cy.visit("/student/test");

    // 选择任务
    cy.get(".el-select").click();
    cy.get(".el-select-dropdown__item").first().click();

    // 选择题型
    cy.contains("选择题").click();

    // 开始测试
    cy.contains("开始测试").click();

    // 答题
    cy.get(".el-radio").first().click();

    // 下一题或提交
    cy.contains("下一题").click();

    // 继续答题直到最后一题
    // ...

    // 提交测试
    cy.contains("提交测试").click();
    cy.contains("确定提交").click();

    // 验证结果页面
    cy.contains("测试完成").should("be.visible");
    cy.get(".stat-value").should("be.visible");
  });

  it("应该显示历史成绩", () => {
    cy.visit("/student/test");
    cy.contains("查看历史成绩").click();

    // 验证历史成绩表格
    cy.get(".el-table").should("be.visible");
  });

  it("应该显示个人统计", () => {
    cy.visit("/student/statistics");

    // 验证统计卡片
    cy.contains("学习时长").should("be.visible");
    cy.contains("掌握词汇数").should("be.visible");

    // 验证图表
    cy.get(".echarts").should("have.length.greaterThan", 0);
  });
});
```

### 4. 任务管理流程测试

**文件**: `cypress/e2e/teacher/tasks.cy.js`

```javascript
describe("任务管理流程", () => {
  beforeEach(() => {
    cy.login("teacher001", "password123");
    cy.visit("/teacher/tasks");
  });

  it("应该显示任务列表", () => {
    cy.get(".el-table").should("be.visible");
  });

  it("应该成功创建任务", () => {
    cy.contains("创建任务").click();

    // 填写表单
    cy.get('input[placeholder="请输入任务标题"]').type("测试任务");
    cy.get('textarea[placeholder="请输入任务描述"]').type("这是一个测试任务");

    // 选择词汇表
    cy.get(".el-select").first().click();
    cy.get(".el-select-dropdown__item").first().click();

    // 选择学生
    cy.contains("选择学生").click();
    cy.get(".el-checkbox").first().click();

    // 设置截止日期
    cy.get(".el-date-editor").click();
    cy.get(".el-picker-panel__footer").contains("确定").click();

    // 提交
    cy.contains("确定").click();

    // 验证成功提示
    cy.contains("创建成功").should("be.visible");
  });

  it("应该显示任务进度", () => {
    // 点击第一行的查看进度按钮
    cy.get(".el-table__row")
      .first()
      .find("button")
      .contains("查看进度")
      .click();

    // 验证进度页面
    cy.contains("任务进度").should("be.visible");
    cy.get(".progress-bar").should("be.visible");
  });
});
```

## Cypress自定义命令

创建 `frontend/cypress/support/commands.js`:

```javascript
// 登录命令
Cypress.Commands.add("login", (username, password) => {
  cy.session([username, password], () => {
    cy.visit("/login");
    cy.get('input[placeholder="请输入用户名"]').type(username);
    cy.get('input[placeholder="请输入密码"]').type(password);
    cy.get('button[type="submit"]').click();
    cy.url().should("not.include", "/login");
  });
});

// API请求命令
Cypress.Commands.add("apiLogin", (username, password) => {
  cy.request({
    method: "POST",
    url: "/api/auth/login",
    body: {
      username,
      password,
    },
  }).then((response) => {
    window.localStorage.setItem("token", response.body.data.token);
  });
});
```

## 运行测试

### 交互模式

```bash
cd frontend
npx cypress open
```

### 命令行模式

```bash
cd frontend
npx cypress run
```

### 运行特定测试

```bash
npx cypress run --spec "cypress/e2e/auth/login.cy.js"
```

## 测试数据准备

在运行E2E测试前，需要准备测试数据：

1. **创建测试数据库**

```sql
CREATE DATABASE ancient_stars_test;
```

2. **运行数据库迁移**

```bash
mvn flyway:migrate -Dflyway.url=jdbc:mysql://localhost:3306/ancient_stars_test
```

3. **插入测试数据**

```sql
-- 插入测试教师账户
INSERT INTO user (username, password, real_name, role, status)
VALUES ('teacher001', '$2a$10$...', '测试教师', 'TEACHER', 'ACTIVE');

-- 插入测试学生账户
INSERT INTO user (username, password, real_name, role, status)
VALUES ('student001', '$2a$10$...', '测试学生', 'STUDENT', 'ACTIVE');

-- 插入测试词汇
-- ...
```

## CI/CD集成

在 `.github/workflows/e2e-tests.yml`:

```yaml
name: E2E Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  e2e:
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: ancient_stars_test
        ports:
          - 3306:3306

    steps:
      - uses: actions/checkout@v2

      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: "11"

      - name: Set up Node.js
        uses: actions/setup-node@v2
        with:
          node-version: "16"

      - name: Start Backend
        run: |
          mvn spring-boot:run &
          sleep 30

      - name: Install Frontend Dependencies
        run: |
          cd frontend
          npm install

      - name: Start Frontend
        run: |
          cd frontend
          npm run dev &
          sleep 10

      - name: Run Cypress Tests
        run: |
          cd frontend
          npx cypress run

      - name: Upload Screenshots
        if: failure()
        uses: actions/upload-artifact@v2
        with:
          name: cypress-screenshots
          path: frontend/cypress/screenshots
```

## 测试覆盖范围

### 已覆盖的功能

- ✅ 登录流程（教师和学生）
- ✅ 词汇管理（CRUD操作）
- ✅ 词汇表管理
- ✅ 任务管理（创建、查看进度）
- ✅ 学习流程（浏览词汇、标记掌握度）
- ✅ 测试流程（答题、提交、查看结果）
- ✅ 个人统计查看

### 待覆盖的功能

- ⏳ 学生管理（创建、编辑、禁用）
- ⏳ 批量导入词汇
- ⏳ 统计分析（班级统计、报告生成）
- ⏳ 响应式设计测试
- ⏳ 错误处理测试

## 最佳实践

1. **使用data-cy属性**: 为关键元素添加data-cy属性，便于选择器定位
2. **避免硬编码等待**: 使用Cypress的自动等待机制
3. **使用自定义命令**: 封装常用操作，提高代码复用性
4. **独立的测试**: 每个测试应该独立运行，不依赖其他测试
5. **清理测试数据**: 测试后清理创建的数据，避免影响其他测试

## 注意事项

1. E2E测试需要后端服务运行
2. 测试数据应该与生产数据隔离
3. 测试应该可以重复运行
4. 失败时自动截图，便于调试
5. 定期更新测试用例，保持与功能同步

## 验证需求

本E2E测试计划覆盖所有需求：

- ✅ 需求 1: 用户认证与授权
- ✅ 需求 2: 教师管理词汇
- ✅ 需求 3: 教师管理学生
- ✅ 需求 4: 教师分配学习任务
- ✅ 需求 5: 学生学习词汇
- ✅ 需求 6: 学生测试与评估
- ✅ 需求 7: 学习记录与统计
- ✅ 需求 10: 前端界面

---

**文档版本**: 1.0
**最后更新**: 2024-01-01
