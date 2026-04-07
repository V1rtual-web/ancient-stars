# 旧星背单词 - 学生登录页面

这是一个简单的Vue 3前端登录页面，用于测试后端认证模块。

## 安装依赖

```bash
cd frontend
npm install
```

## 启动开发服务器

```bash
npm run dev
```

前端将运行在 http://localhost:3000

## 测试说明

1. 确保后端服务已启动（运行在 http://localhost:8080）
2. 使用测试账号登录：
   - 学号：输入数据库中已存在的学生账号
   - 密码：输入对应的密码

## 功能特性

- ✅ 用户名密码登录
- ✅ 记住密码功能
- ✅ JWT Token存储（localStorage）
- ✅ 错误提示
- ✅ 加载状态
- ✅ 响应式设计

## API代理配置

开发环境下，所有 `/api` 请求会自动代理到 `http://localhost:8080`

## 登录成功后

登录成功后，Token和用户信息会保存到localStorage：

- `accessToken`: 访问令牌
- `refreshToken`: 刷新令牌
- `userInfo`: 用户信息

可以在浏览器控制台查看登录信息。
