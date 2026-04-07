import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/modules/user'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../components/LoginPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/student',
    name: 'StudentLayout',
    component: () => import('../views/student/Layout.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
    children: [
      {
        path: 'tasks',
        name: 'StudentTasks',
        component: () => import('../views/student/Tasks.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
      },
      {
        path: 'learn/:taskId',
        name: 'StudentLearn',
        component: () => import('../views/student/Learn.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
      },
      {
        path: 'test/:taskId',
        name: 'StudentTest',
        component: () => import('../views/student/Test.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
      },
      {
        path: 'statistics',
        name: 'StudentStatistics',
        component: () => import('../views/student/Statistics.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
      },
      {
        path: 'self-study',
        name: 'StudentSelfStudy',
        component: () => import('../views/student/SelfStudy.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
      },
      {
        path: 'review',
        name: 'StudentReview',
        component: () => import('../views/student/Review.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
      },
      {
        path: 'progress',
        name: 'StudentProgress',
        component: () => import('../views/student/StudentProgress.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
      }
    ]
  },
  {
    path: '/teacher',
    name: 'TeacherLayout',
    component: () => import('../views/teacher/Layout.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
    children: [
      {
        path: 'vocabulary',
        name: 'TeacherVocabulary',
        component: () => import('../views/teacher/Vocabulary.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'students',
        name: 'TeacherStudents',
        component: () => import('../views/teacher/Students.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'tasks',
        name: 'TeacherTasks',
        component: () => import('../views/teacher/Tasks.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'statistics',
        name: 'TeacherStatistics',
        component: () => import('../views/teacher/Statistics.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'progress',
        name: 'TeacherProgress',
        component: () => import('../views/teacher/TeacherProgress.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      }
    ]
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 认证检查
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 检查路由是否需要认证
  if (to.meta.requiresAuth) {
    // 检查是否已登录
    if (!userStore.isLoggedIn) {
      // 未登录，重定向到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath } // 保存目标路由，登录后跳转
      })
      return
    }
    
    // 检查角色权限
    if (to.meta.role && userStore.userRole !== to.meta.role) {
      // 角色不匹配，重定向到对应角色的首页
      if (userStore.isTeacher) {
        next('/teacher/vocabulary')
      } else if (userStore.isStudent) {
        next('/student/tasks')
      } else {
        next('/login')
      }
      return
    }
  }
  
  // 如果已登录且访问登录页，重定向到对应角色的首页
  if (to.path === '/login' && userStore.isLoggedIn) {
    if (userStore.isTeacher) {
      next('/teacher/vocabulary')
    } else if (userStore.isStudent) {
      next('/student/tasks')
    } else {
      next()
    }
    return
  }
  
  next()
})

export default router
