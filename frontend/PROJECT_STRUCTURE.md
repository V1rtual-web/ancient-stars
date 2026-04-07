# Vue 前端项目结构说明

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API接口封装
│   │   └── auth.js       # 认证相关API
│   ├── components/       # 公共组件
│   │   └── LoginPage.vue # 登录页面组件
│   ├── router/           # 路由配置
│   │   └── index.js      # 路由定义和守卫
│   ├── store/            # 状态管理（Pinia）
│   │   ├── index.js      # Pinia实例
│   │   └── modules/
│   │       └── user.js   # 用户状态管理
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios封装（拦截器）
│   ├── views/            # 页面组件
│   │   ├── student/      # 学生端页面
│   │   │   ├── Layout.vue
│   │   │   ├── Tasks.vue
│   │   │   ├── Learn.vue
│   │   │   ├── Test.vue
│   │   │   └── Statistics.vue
│   │   └── teacher/      # 教师端页面
│   │       ├── Layout.vue
│   │       ├── Vocabulary.vue
│   │       ├── Students.vue
│   │       ├── Tasks.vue
│   │       └── Statistics.vue
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html
├── package.json
├── vite.config.js
└── README.md
```

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Vue Router 4** - 官方路由管理器
- **Pinia** - Vue 3推荐的状态管理库
- **Axios** - HTTP客户端
- **Element Plus** - Vue 3 UI组件库
- **Vite** - 下一代前端构建工具

## 核心功能

### 1. Axios拦截器配置 (`src/utils/request.js`)

**请求拦截器：**

- 自动从localStorage获取JWT Token
- 在请求头中添加 `Authorization: Bearer {token}`

**响应拦截器：**

- 统一处理响应数据格式
- 自动处理HTTP错误状态码（401, 403, 404, 500等）
- 401错误时自动清除token并跳转到登录页
- 使用Element Plus的Message组件显示错误提示

### 2. 路由守卫配置 (`src/router/index.js`)

**认证检查：**

- 检查路由是否需要认证（`meta.requiresAuth`）
- 未登录用户访问受保护路由时重定向到登录页
- 保存目标路由，登录后自动跳转

**角色权限检查：**

- 检查用户角色是否匹配路由要求（`meta.role`）
- 教师只能访问教师端页面
- 学生只能访问学生端页面
- 角色不匹配时重定向到对应角色的首页

**自动跳转：**

- 已登录用户访问登录页时自动跳转到对应角色的首页

### 3. 状态管理 (`src/store/modules/user.js`)

**State：**

- `token` - 访问令牌
- `refreshToken` - 刷新令牌
- `userInfo` - 用户信息

**Getters：**

- `isLoggedIn` - 是否已登录
- `userRole` - 用户角色
- `isTeacher` - 是否是教师
- `isStudent` - 是否是学生
- `username` - 用户名
- `realName` - 真实姓名

**Actions：**

- `login()` - 用户登录
- `logout()` - 用户登出
- `refreshAccessToken()` - 刷新令牌
- `setToken()` - 设置令牌
- `setUserInfo()` - 设置用户信息

## 安装依赖

```bash
# 进入frontend目录
cd frontend

# 安装依赖
npm install
```

## 运行项目

```bash
# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

## API代理配置

在 `vite.config.js` 中配置了API代理：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

前端访问 `/api/*` 会自动代理到后端 `http://localhost:8080/api/*`

## 路由说明

### 公共路由

- `/login` - 登录页面

### 学生端路由

- `/student/tasks` - 我的任务
- `/student/learn/:taskId` - 词汇学习
- `/student/test/:taskId` - 词汇测试
- `/student/statistics` - 学习统计

### 教师端路由

- `/teacher/vocabulary` - 词汇管理
- `/teacher/students` - 学生管理
- `/teacher/tasks` - 任务管理
- `/teacher/statistics` - 统计分析

## 下一步开发

当前项目已完成基础框架搭建，包括：

- ✅ 项目结构配置
- ✅ 依赖安装配置
- ✅ Axios拦截器（请求添加Token，响应统一处理）
- ✅ 路由守卫（认证检查和角色权限检查）
- ✅ Pinia状态管理
- ✅ 基础页面布局

待实现功能：

- 各个页面的具体业务逻辑
- 词汇管理界面
- 学生管理界面
- 任务管理界面
- 学习和测试界面
- 统计分析界面
