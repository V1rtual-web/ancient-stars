# StudyCard 组件使用文档

## 概述

StudyCard 是一个用于展示和学习词汇的卡片组件，支持左右滑动切换词汇，并提供"已掌握"功能。

## 功能特性

- ✅ 展示词汇完整信息（单词、音标、释义、例句）
- ✅ 支持左右滑动切换词汇（触摸设备）
- ✅ 支持按钮切换词汇（桌面设备）
- ✅ 提供"已掌握"按钮及点击事件
- ✅ 点击"已掌握"后自动切换到下一个词汇
- ✅ 显示学习进度（当前/总数）
- ✅ 平滑的过渡动画
- ✅ 响应式设计，支持移动端和桌面端

## Props

| 属性名       | 类型   | 必填 | 默认值 | 说明               |
| ------------ | ------ | ---- | ------ | ------------------ |
| vocabularies | Array  | 是   | []     | 词汇列表数组       |
| initialIndex | Number | 否   | 0      | 初始显示的词汇索引 |

### vocabularies 数组项结构

```javascript
{
  id: Number,           // 词汇ID
  word: String,         // 单词
  phonetic: String,     // 音标（可选）
  translation: String,  // 释义
  example: String       // 例句（可选）
}
```

## Events

| 事件名      | 参数                 | 说明                         |
| ----------- | -------------------- | ---------------------------- |
| learned     | vocabularyId: Number | 当用户点击"已掌握"按钮时触发 |
| indexChange | newIndex: Number     | 当词汇索引变化时触发         |

## 使用示例

### 基础使用

```vue
<template>
  <StudyCard :vocabularies="vocabularies" @learned="handleLearned" />
</template>

<script setup>
import { ref } from "vue";
import StudyCard from "@/components/StudyCard.vue";

const vocabularies = ref([
  {
    id: 1,
    word: "apple",
    phonetic: "/ˈæpl/",
    translation: "n. 苹果",
    example: "I like apples.",
  },
  {
    id: 2,
    word: "banana",
    phonetic: "/bəˈnɑːnə/",
    translation: "n. 香蕉",
    example: "Bananas are yellow.",
  },
]);

const handleLearned = (vocabularyId) => {
  console.log("已掌握词汇ID:", vocabularyId);
};
</script>
```

### 完整示例（带索引追踪）

```vue
<template>
  <StudyCard
    :vocabularies="vocabularies"
    :initial-index="currentIndex"
    @learned="handleLearned"
    @index-change="handleIndexChange"
  />
</template>

<script setup>
import { ref } from "vue";
import StudyCard from "@/components/StudyCard.vue";
import { selfStudyAPI } from "@/api/selfStudy";

const vocabularies = ref([]);
const currentIndex = ref(0);

const handleLearned = async (vocabularyId) => {
  try {
    // API 调用已在组件内部完成
    // 这里可以更新本地状态
    const vocab = vocabularies.value.find((v) => v.id === vocabularyId);
    if (vocab) {
      vocab.isLearned = true;
    }
  } catch (error) {
    console.error("处理失败:", error);
  }
};

const handleIndexChange = (newIndex) => {
  currentIndex.value = newIndex;
  console.log("当前索引:", newIndex);
};
</script>
```

## 交互说明

### 桌面端

- 点击左右箭头按钮切换词汇
- 点击"已掌握"按钮标记当前词汇

### 移动端

- 左右滑动卡片切换词汇
- 点击"已掌握"按钮标记当前词汇

### 自动切换

- 点击"已掌握"后，组件会在 500ms 后自动切换到下一个词汇
- 如果已经是最后一个词汇，则不会自动切换

## API 集成

组件内部使用 `selfStudyAPI.createStudyRecord(vocabularyId)` 来创建学习记录。

确保在项目中正确配置了 API：

```javascript
// src/api/selfStudy.js
export const selfStudyAPI = {
  createStudyRecord(vocabularyId) {
    return request.post("/api/student/study-records", { vocabularyId });
  },
};
```

## 样式定制

组件使用 scoped 样式，如需定制，可以通过以下方式：

1. 修改组件内部的 CSS 变量
2. 使用深度选择器覆盖样式
3. 创建自定义主题

## 注意事项

1. 确保传入的 vocabularies 数组不为空
2. 每个词汇对象必须包含 id、word 和 translation 字段
3. phonetic 和 example 字段是可选的
4. 组件会自动处理 API 调用和错误提示
5. 建议一次加载 20-50 个词汇以获得最佳性能

## 相关组件

- VocabularyList.vue - 词汇列表页面
- SelfStudy.vue - 自主学习页面（使用 StudyCard 的示例）

## 相关需求

- 需求 1.4: 展示词汇完整信息
- 需求 1.5: 标记已掌握词汇
- 需求 9.3: 学习卡片界面
- 需求 9.4: "已掌握"按钮
- 需求 9.5: 自动切换功能
