# 安装ECharts依赖说明

## 问题说明

由于系统PowerShell执行策略限制，无法直接运行npm命令。

## 解决方案

### 方法1：使用管理员权限运行PowerShell（推荐）

1. 以管理员身份打开PowerShell
2. 运行以下命令临时允许脚本执行：

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process
```

3. 进入frontend目录并安装依赖：

```powershell
cd frontend
npm install
```

### 方法2：使用CMD命令提示符

1. 打开CMD命令提示符（不需要管理员权限）
2. 进入frontend目录：

```cmd
cd frontend
```

3. 安装依赖：

```cmd
npm install
```

### 方法3：使用Git Bash

1. 打开Git Bash
2. 进入frontend目录：

```bash
cd frontend
```

3. 安装依赖：

```bash
npm install
```

## 验证安装

安装完成后，检查node_modules目录下是否存在：

- `echarts` 目录
- `vue-echarts` 目录

或者运行：

```bash
npm list echarts vue-echarts
```

应该看到类似输出：

```
ancient-stars-frontend@1.0.0
├── echarts@5.4.3
└── vue-echarts@6.6.1
```

## 运行项目

依赖安装完成后，可以运行：

```bash
npm run dev
```

启动开发服务器，然后访问统计分析页面查看效果。
