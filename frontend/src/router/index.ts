import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/Index.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', guestOnly: true }
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/article/Detail.vue'),
    meta: { title: '文章详情' }
  },
  {
    path: '/article/create',
    name: 'ArticleCreate',
    component: () => import('@/views/article/Editor.vue'),
    meta: { title: '创建文章', requiresAuth: true }
  },
  {
    path: '/article/:id/edit',
    name: 'ArticleEdit',
    component: () => import('@/views/article/Editor.vue'),
    meta: { title: '编辑文章', requiresAuth: true }
  },
  {
    path: '/category/:slug',
    name: 'Category',
    component: () => import('@/views/home/Index.vue'),
    meta: { title: '分类' }
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: () => import('@/views/user/Center.vue'),
    meta: { title: '个人中心', requiresAuth: true },
    children: [
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Center.vue'),
        meta: { title: '个人资料' }
      }
    ]
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/oauth-callback',
    name: 'OAuthCallback',
    component: () => import('@/views/auth/OAuthCallback.vue'),
    meta: { title: 'OAuth 登录' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('access_token')
  const userInfo = localStorage.getItem('user_info')
  const userRole = userInfo ? JSON.parse(userInfo).role : null
  const requiresAuth = to.meta.requiresAuth as boolean
  const requiresAdmin = to.meta.requiresAdmin as boolean
  const guestOnly = to.meta.guestOnly as boolean

  // 需要登录的页面
  if (requiresAuth && !token) {
    next({ name: 'Login' })
    return
  }

  // 需要管理员权限的页面
  if (requiresAdmin) {
    if (!token) {
      next({ name: 'Login' })
      return
    }
    if (userRole !== 3) {
      next({ name: 'Home' })
      return
    }
    next()
    return
  }

  // 仅访客访问的页面（已登录则跳转到首页）
  if (guestOnly && token) {
    next({ name: 'Home' })
    return
  }

  // 设置页面标题
  document.title = `${to.meta.title as string} - 实战AI`

  next()
})

export default router
