# 前端加载状态和错误处理指南

本文档说明如何在前端项目中使用统一的加载状态和错误处理功能。

## 目录

1. [错误处理](#错误处理)
2. [加载状态管理](#加载状态管理)
3. [空状态组件](#空状态组件)
4. [最佳实践](#最佳实践)

## 错误处理

### 1. 统一错误处理工具

位置：`src/utils/errorHandler.js`

#### 基本用法

```javascript
import { handleError } from "@/utils/errorHandler";

try {
  const response = await api.getData();
  // 处理成功响应
} catch (error) {
  // 统一错误处理，自动显示错误消息
  handleError(error, "加载数据失败");
}
```

#### 高级用法

```javascript
import {
  handleError,
  isNetworkError,
  isServerError,
  getFriendlyErrorMessage,
} from "@/utils/errorHandler";

try {
  const response = await api.getData();
} catch (error) {
  // 不显示错误消息，只返回错误文本
  const errorMsg = handleError(error, "操作失败", false);

  // 检查错误类型
  if (isNetworkError(error)) {
    console.log("网络错误");
  } else if (isServerError(error)) {
    console.log("服务器错误");
  }

  // 获取友好的错误提示
  const friendlyMsg = getFriendlyErrorMessage(error);
}
```

### 2. request.js 拦截器

位置：`src/utils/request.js`

已增强的错误处理功能：

- 自动处理 400, 401, 403, 404, 500, 502, 503, 504 等HTTP状态码
- 网络错误检测（超时、连接失败等）
- 统一的中文错误提示
- 401错误自动跳转登录页

## 加载状态管理

### 1. useLoading Composable

位置：`src/composables/useLoading.js`

#### 基本用法

```vue
<template>
  <div>
    <el-button @click="loadData" :loading="loading">加载数据</el-button>
    <div v-loading="loading">
      <!-- 内容 -->
    </div>
  </div>
</template>

<script setup>
import { useLoading } from "@/composables/useLoading";

const { loading, startLoading, stopLoading } = useLoading();

const loadData = async () => {
  startLoading();
  try {
    const response = await api.getData();
    // 处理数据
  } catch (error) {
    handleError(error);
  } finally {
    stopLoading();
  }
};
</script>
```

#### 使用 withLoading 包装函数

```javascript
import { useLoading } from "@/composables/useLoading";

const { loading, withLoading } = useLoading();

// 自动管理loading状态
const loadData = withLoading(async () => {
  const response = await api.getData();
  return response.data;
});

// 调用时自动显示loading
await loadData();
```

#### 全屏加载

```javascript
const { startLoading, stopLoading } = useLoading();

// 显示全屏loading
startLoading({
  fullscreen: true,
  text: "正在处理...",
});

try {
  await api.processData();
} finally {
  stopLoading();
}
```

### 2. 多个加载状态管理

```vue
<script setup>
import { useMultipleLoading } from "@/composables/useLoading";

const { setLoading, isLoading, isAnyLoading } = useMultipleLoading();

const loadUsers = async () => {
  setLoading("users", true);
  try {
    await api.getUsers();
  } finally {
    setLoading("users", false);
  }
};

const loadTasks = async () => {
  setLoading("tasks", true);
  try {
    await api.getTasks();
  } finally {
    setLoading("tasks", false);
  }
};
</script>

<template>
  <div v-loading="isLoading('users')">用户列表</div>
  <div v-loading="isLoading('tasks')">任务列表</div>
  <div v-if="isAnyLoading()">正在加载...</div>
</template>
```

## 空状态组件

### LoadingSpinner 组件

位置：`src/components/LoadingSpinner.vue`

```vue
<template>
  <!-- 基本用法 -->
  <LoadingSpinner />

  <!-- 自定义文本 -->
  <LoadingSpinner text="正在加载数据..." />

  <!-- 自定义大小 -->
  <LoadingSpinner :size="60" />

  <!-- 全屏显示 -->
  <LoadingSpinner fullscreen text="处理中..." />
</template>

<script setup>
import LoadingSpinner from "@/components/LoadingSpinner.vue";
</script>
```

### EmptyState 组件

位置：`src/components/EmptyState.vue`

```vue
<template>
  <!-- 基本用法 -->
  <EmptyState description="暂无数据" />

  <!-- 自定义图片大小 -->
  <EmptyState description="暂无任务" :image-size="150" />

  <!-- 带操作按钮 -->
  <EmptyState description="暂无词汇">
    <el-button type="primary" @click="handleAdd">添加词汇</el-button>
  </EmptyState>
</template>

<script setup>
import EmptyState from "@/components/EmptyState.vue";
</script>
```

## 最佳实践

### 1. 完整的API调用模式

```vue
<template>
  <div class="page">
    <!-- 加载状态 -->
    <LoadingSpinner v-if="loading" />

    <!-- 空状态 -->
    <EmptyState
      v-else-if="!loading && list.length === 0"
      description="暂无数据"
    >
      <el-button type="primary" @click="handleAdd">添加数据</el-button>
    </EmptyState>

    <!-- 数据列表 -->
    <div v-else>
      <el-table :data="list" v-loading="loading">
        <!-- 表格列 -->
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { handleError } from "@/utils/errorHandler";
import { useLoading } from "@/composables/useLoading";
import LoadingSpinner from "@/components/LoadingSpinner.vue";
import EmptyState from "@/components/EmptyState.vue";

const { loading, withLoading } = useLoading();
const list = ref([]);

const loadList = withLoading(async () => {
  try {
    const response = await api.getList();
    list.value = response.data || [];
  } catch (error) {
    handleError(error, "加载列表失败");
  }
});

onMounted(() => {
  loadList();
});
</script>
```

### 2. 表单提交模式

```vue
<template>
  <el-form @submit.prevent="handleSubmit">
    <!-- 表单项 -->
    <el-button type="primary" :loading="submitting" @click="handleSubmit">
      提交
    </el-button>
  </el-form>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { handleError } from "@/utils/errorHandler";

const submitting = ref(false);
const formData = ref({});

const handleSubmit = async () => {
  submitting.value = true;
  try {
    await api.submitForm(formData.value);
    ElMessage.success("提交成功");
  } catch (error) {
    handleError(error, "提交失败");
  } finally {
    submitting.value = false;
  }
};
</script>
```

### 3. 多个并发请求

```vue
<script setup>
import { ref, onMounted } from "vue";
import { handleError } from "@/utils/errorHandler";
import { useLoading } from "@/composables/useLoading";

const { loading, startLoading, stopLoading } = useLoading();
const users = ref([]);
const tasks = ref([]);

const loadAllData = async () => {
  startLoading();
  try {
    // 并发请求
    const [usersRes, tasksRes] = await Promise.all([
      api.getUsers(),
      api.getTasks(),
    ]);

    users.value = usersRes.data || [];
    tasks.value = tasksRes.data || [];
  } catch (error) {
    handleError(error, "加载数据失败");
  } finally {
    stopLoading();
  }
};

onMounted(() => {
  loadAllData();
});
</script>
```

### 4. 错误重试机制

```vue
<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { handleError, isNetworkError } from "@/utils/errorHandler";

const maxRetries = 3;
let retryCount = 0;

const loadDataWithRetry = async () => {
  try {
    const response = await api.getData();
    retryCount = 0; // 重置重试计数
    return response.data;
  } catch (error) {
    if (isNetworkError(error) && retryCount < maxRetries) {
      retryCount++;
      ElMessage.warning(`网络错误，正在重试 (${retryCount}/${maxRetries})...`);
      // 延迟后重试
      await new Promise((resolve) => setTimeout(resolve, 1000 * retryCount));
      return loadDataWithRetry();
    } else {
      handleError(error, "加载数据失败");
      throw error;
    }
  }
};
</script>
```

## 注意事项

1. **始终使用 try-catch-finally**：确保loading状态在finally中被重置
2. **避免重复错误提示**：request.js已经处理了基本错误，业务层只需处理特殊情况
3. **合理使用空状态**：区分"加载中"、"无数据"和"加载失败"三种状态
4. **网络错误友好提示**：使用中文提示，避免技术术语
5. **loading状态管理**：避免多个loading状态冲突，使用useMultipleLoading管理多个独立的loading

## 验证需求

本实现满足以下需求：

- **需求 10.2**：为所有API调用添加加载状态
- **需求 10.4**：实现统一的错误提示（Toast/Message）
- **需求 10.5**：实现网络错误处理和空状态提示
