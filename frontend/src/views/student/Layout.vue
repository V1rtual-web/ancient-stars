<template>
  <div class="student-layout">
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="header-left">
            <el-button
              class="mobile-menu-btn"
              :icon="Menu"
              @click="toggleMobileMenu"
              circle
            />
            <h2>旧星背单词 - 学生端</h2>
          </div>
          <div class="user-info">
            <span class="username">{{ userStore.realName || userStore.username }}</span>
            <el-button @click="handleLogout" type="danger" size="small">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" :class="{ 'mobile-menu-open': mobileMenuOpen }">
          <el-menu :default-active="activeMenu" router @select="handleMenuSelect">
            <el-menu-item index="/student/tasks">
              <el-icon><List /></el-icon>
              <span>我的任务</span>
            </el-menu-item>
            <el-menu-item index="/student/self-study">
              <el-icon><Reading /></el-icon>
              <span>自主学习</span>
            </el-menu-item>
            <el-menu-item index="/student/review">
              <el-icon><Clock /></el-icon>
              <span>待复习</span>
            </el-menu-item>
            <el-menu-item index="/student/statistics">
              <el-icon><DataAnalysis /></el-icon>
              <span>学习统计</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <!-- 移动端遮罩层 -->
        <div
          v-if="mobileMenuOpen"
          class="mobile-overlay"
          @click="closeMobileMenu"
        />
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../store/modules/user'
import { Menu, Reading, Clock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const mobileMenuOpen = ref(false)

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

const closeMobileMenu = () => {
  mobileMenuOpen.value = false
}

const handleMenuSelect = () => {
  // 移动端选择菜单后自动关闭
  if (window.innerWidth < 768) {
    closeMobileMenu()
  }
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.student-layout {
  height: 100vh;
}

.el-container {
  height: 100%;
}

.el-header {
  background-color: #545c64;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mobile-menu-btn {
  display: none;
  background-color: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.mobile-menu-btn:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.header-content h2 {
  margin: 0;
  font-size: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  display: inline-block;
}

.el-aside {
  background-color: #f5f7fa;
  transition: left 0.3s ease;
}

.mobile-overlay {
  display: none;
}

.el-main {
  background-color: #fff;
  padding: 20px;
  overflow-y: auto;
}

/* 移动端样式 */
@media (max-width: 767px) {
  .el-header {
    padding: 0 12px;
    height: 60px !important;
  }

  .mobile-menu-btn {
    display: flex;
  }

  .header-content h2 {
    font-size: 16px;
  }

  .username {
    display: none;
  }

  .el-aside {
    width: 250px !important;
    position: fixed;
    top: 60px;
    left: -250px;
    height: calc(100vh - 60px);
    z-index: 1001;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  }

  .el-aside.mobile-menu-open {
    left: 0;
  }

  .mobile-overlay {
    display: block;
    position: fixed;
    top: 60px;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 1000;
  }

  .el-main {
    padding: 12px;
  }
}

/* 平板端样式 */
@media (min-width: 768px) and (max-width: 1023px) {
  .el-aside {
    width: 180px !important;
  }

  .header-content h2 {
    font-size: 18px;
  }

  .el-main {
    padding: 16px;
  }
}
</style>
