<template>
  <div class="login-page">
    <!-- 角色选择页 -->
    <div v-if="!selectedRole" class="role-select-container">
      <div class="brand">
        <div class="brand-icon">📚</div>
        <h1>旧星背单词</h1>
        <p>请选择您的身份</p>
      </div>
      <div class="role-cards">
        <div class="role-card student-card" @click="selectRole('STUDENT')">
          <div class="role-icon">🎓</div>
          <h2>学生登录</h2>
          <p>查看任务、背单词、查看进度</p>
          <div class="role-arrow">→</div>
        </div>
        <div class="role-card teacher-card" @click="selectRole('TEACHER')">
          <div class="role-icon">👨‍🏫</div>
          <h2>教师登录</h2>
          <p>管理词汇、布置任务、查看班级</p>
          <div class="role-arrow">→</div>
        </div>
      </div>
    </div>

    <!-- 登录/注册表单 -->
    <div v-else class="form-container">
      <div class="form-card">
        <button class="back-btn" @click="selectedRole = null">← 返回</button>

        <div class="form-header">
          <div class="role-badge" :class="selectedRole === 'STUDENT' ? 'student' : 'teacher'">
            {{ selectedRole === 'STUDENT' ? '🎓 学生' : '👨‍🏫 教师' }}
          </div>
          <h2>旧星背单词</h2>
          <p>{{ selectedRole === 'STUDENT' ? '学生学习系统' : '教师管理系统' }}</p>
        </div>

        <el-tabs v-model="activeTab" class="form-tabs">
          <!-- 登录 -->
          <el-tab-pane label="登录" name="login">
            <el-form @submit.prevent="handleLogin" class="auth-form">
              <el-form-item>
                <el-input
                  v-model="loginForm.username"
                  placeholder="用户名"
                  :disabled="loading"
                  clearable
                  size="large"
                >
                  <template #prefix><span class="input-icon">👤</span></template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="密码"
                  :disabled="loading"
                  show-password
                  size="large"
                >
                  <template #prefix><span class="input-icon">🔒</span></template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-checkbox v-model="loginForm.rememberMe" :disabled="loading">记住密码</el-checkbox>
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  native-type="submit"
                  :loading="loading"
                  size="large"
                  class="submit-btn"
                  :class="selectedRole === 'STUDENT' ? 'student-btn' : 'teacher-btn'"
                >
                  {{ loading ? '登录中...' : '登录' }}
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 注册 -->
          <el-tab-pane label="注册" name="register">
            <el-form @submit.prevent="handleRegister" class="auth-form">
              <el-form-item>
                <el-input v-model="registerForm.username" placeholder="用户名（4-50个字符）" :disabled="loading" clearable size="large">
                  <template #prefix><span class="input-icon">👤</span></template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-input v-model="registerForm.realName" placeholder="真实姓名" :disabled="loading" clearable size="large">
                  <template #prefix><span class="input-icon">📝</span></template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-input v-model="registerForm.email" type="email" placeholder="邮箱（可选）" :disabled="loading" clearable size="large">
                  <template #prefix><span class="input-icon">📧</span></template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-input v-model="registerForm.password" type="password" placeholder="密码（6-20个字符）" :disabled="loading" show-password size="large">
                  <template #prefix><span class="input-icon">🔒</span></template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" :disabled="loading" show-password size="large">
                  <template #prefix><span class="input-icon">🔒</span></template>
                </el-input>
              </el-form-item>
              <el-form-item v-if="selectedRole === 'STUDENT'">
                <el-input v-model.number="registerForm.classId" type="number" placeholder="班级ID（可选）" :disabled="loading" clearable size="large">
                  <template #prefix><span class="input-icon">🏫</span></template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  native-type="submit"
                  :loading="loading"
                  size="large"
                  class="submit-btn"
                  :class="selectedRole === 'STUDENT' ? 'student-btn' : 'teacher-btn'"
                >
                  {{ loading ? '注册中...' : '注册' }}
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="form-footer">
          <p v-if="activeTab === 'login'">
            {{ selectedRole === 'STUDENT' ? '首次登录请使用教师分配的初始密码' : '请使用管理员分配的账号登录' }}
          </p>
          <p v-else>注册后将自动登录系统</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/modules/user'
import { ElMessage } from 'element-plus'
import { authAPI } from '../api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedRole = ref(null)
const activeTab = ref('login')
const loading = ref(false)

const loginForm = ref({ username: '', password: '', rememberMe: false })
const registerForm = ref({
  username: '', password: '', confirmPassword: '',
  realName: '', email: '', classId: null
})

const selectRole = (role) => {
  selectedRole.value = role
  activeTab.value = 'login'
}

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const result = await userStore.login(
      loginForm.value.username,
      loginForm.value.password,
      loginForm.value.rememberMe
    )
    if (result.success) {
      // 验证角色是否匹配
      if (userStore.userRole !== selectedRole.value) {
        ElMessage.error(`该账号不是${selectedRole.value === 'STUDENT' ? '学生' : '教师'}账号`)
        await userStore.logout()
        return
      }
      ElMessage.success('登录成功！')
      const redirect = route.query.redirect
      if (redirect) {
        router.push(redirect)
      } else if (userStore.isTeacher) {
        router.push('/teacher/vocabulary')
      } else {
        router.push('/student/tasks')
      }
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.value.username || registerForm.value.username.length < 4) {
    ElMessage.warning('用户名长度不能少于4个字符')
    return
  }
  if (!registerForm.value.password || registerForm.value.password.length < 6) {
    ElMessage.warning('密码长度不能少于6个字符')
    return
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (!registerForm.value.realName) {
    ElMessage.warning('请输入真实姓名')
    return
  }
  if (registerForm.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.value.email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }
  loading.value = true
  try {
    const response = await authAPI.register({
      username: registerForm.value.username,
      password: registerForm.value.password,
      realName: registerForm.value.realName,
      email: registerForm.value.email || null,
      role: selectedRole.value,
      classId: registerForm.value.classId || null
    })
    if (response.success && response.data) {
      ElMessage.success('注册成功！正在自动登录...')
      const data = response.data
      localStorage.setItem('accessToken', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)
      localStorage.setItem('userInfo', JSON.stringify({
        userId: data.userId,
        username: data.username,
        role: data.role
      }))
      await userStore.fetchUserInfo()
      setTimeout(() => {
        router.push(data.role === 'TEACHER' ? '/teacher/vocabulary' : '/student/tasks')
      }, 500)
    } else {
      ElMessage.error(response.error?.message || '注册失败')
    }
  } catch (error) {
    console.error('注册错误:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const rememberedUsername = localStorage.getItem('rememberedUsername')
  if (rememberedUsername) {
    loginForm.value.username = rememberedUsername
    loginForm.value.rememberMe = true
  }
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

/* 角色选择 */
.role-select-container {
  text-align: center;
  padding: 20px;
}

.brand {
  margin-bottom: 48px;
  color: white;
}

.brand-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.brand h1 {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: 2px;
}

.brand p {
  font-size: 16px;
  opacity: 0.7;
  margin: 0;
}

.role-cards {
  display: flex;
  gap: 24px;
  justify-content: center;
  flex-wrap: wrap;
}

.role-card {
  width: 220px;
  padding: 36px 24px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  color: white;
}

.role-card::before {
  content: '';
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.3s;
  background: rgba(255,255,255,0.1);
}

.role-card:hover::before { opacity: 1; }
.role-card:hover { transform: translateY(-6px); box-shadow: 0 20px 40px rgba(0,0,0,0.3); }

.student-card { background: linear-gradient(135deg, #667eea, #764ba2); }
.teacher-card { background: linear-gradient(135deg, #f093fb, #f5576c); }

.role-icon { font-size: 48px; margin-bottom: 16px; }
.role-card h2 { font-size: 20px; font-weight: 600; margin: 0 0 8px; }
.role-card p { font-size: 13px; opacity: 0.85; margin: 0 0 20px; line-height: 1.5; }

.role-arrow {
  font-size: 20px;
  opacity: 0.7;
  transition: transform 0.3s;
}
.role-card:hover .role-arrow { transform: translateX(4px); opacity: 1; }

/* 登录表单 */
.form-container {
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.form-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  padding: 36px 40px;
  position: relative;
}

.back-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
  margin-bottom: 20px;
  transition: color 0.2s;
}
.back-btn:hover { color: #333; }

.form-header {
  text-align: center;
  margin-bottom: 24px;
}

.role-badge {
  display: inline-block;
  padding: 4px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 12px;
  color: white;
}
.role-badge.student { background: linear-gradient(135deg, #667eea, #764ba2); }
.role-badge.teacher { background: linear-gradient(135deg, #f093fb, #f5576c); }

.form-header h2 {
  font-size: 22px;
  color: #1a1a2e;
  margin: 0 0 4px;
}
.form-header p {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.auth-form { margin-top: 16px; }

.input-icon { font-size: 14px; }

.submit-btn {
  width: 100%;
  border: none;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
}

.student-btn { background: linear-gradient(135deg, #667eea, #764ba2) !important; }
.teacher-btn { background: linear-gradient(135deg, #f093fb, #f5576c) !important; }

.form-footer {
  margin-top: 16px;
  text-align: center;
}
.form-footer p {
  font-size: 12px;
  color: #bbb;
  margin: 0;
}

@media (max-width: 480px) {
  .role-cards { flex-direction: column; align-items: center; }
  .form-card { padding: 28px 24px; }
  .brand h1 { font-size: 28px; }
}
</style>
