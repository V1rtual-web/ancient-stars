# 前端项目安装指南

## 前置要求

- Node.js 16+
- npm 或 yarn 或 pnpm

## 安装步骤

### 方法1：使用npm（推荐）

```bash
cd frontend
npm install
```

### 方法2：使用yarn

```bash
cd frontend
yarn install
```

### 方法3：使用pnpm

```bash
cd frontend
pnpm install
```

## 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

## 依赖说明

本项目已配置以下依赖：

### 核心依赖

- `vue@^3.4.0` - Vue 3框架
- `vue-router@^4.2.5` - 路由管理
- `pinia@^2.1.7` - 状态管理
- `axios@^1.6.0` - HTTP客户端
- `element-plus@^2.5.0` - UI组件库
- `@element-plus/icons-vue@^2.3.1` - Element Plus图标

### 开发依赖

- `@vitejs/plugin-vue@^5.0.0` - Vite的Vue插件
- `vite@^5.0.0` - 构建工具

## 验证安装

安装完成后，检查以下文件是否存在：

- `node_modules/` - 依赖包目录
- `package-lock.json` - 依赖锁定文件

## 常见问题

### 1. 安装速度慢

使用国内镜像源：

```bash
npm config set registry https://registry.npmmirror.com
npm install
```

### 2. 权限错误

使用管理员权限运行命令，或使用：

```bash
npm install --unsafe-perm
```

### 3. 依赖冲突

清除缓存后重新安装：

```bash
rm -rf node_modules package-lock.json
npm install
```

## 下一步

安装完成后，可以：

1. 启动开发服务器：`npm run dev`
2. 查看项目结构：阅读 `PROJECT_STRUCTURE.md`
3. 开始开发具体功能模块
