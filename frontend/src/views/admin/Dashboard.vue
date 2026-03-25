<template>
  <div class="admin-dashboard">
    <!-- 侧边栏导航 -->
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <h2>管理后台</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        @select="handleMenuSelect"
      >
        <el-menu-item index="dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="articles">
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="comments">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <main class="admin-main">
      <!-- 顶部栏 -->
      <header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>管理后台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ menuName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <router-link to="/">
            <el-button text>返回首页</el-button>
          </router-link>
          <span class="admin-user">{{ authStore.user?.username }}</span>
        </div>
      </header>

      <!-- 内容区域 -->
      <div class="admin-content">
        <!-- 数据统计 -->
        <div v-if="activeMenu === 'dashboard'" class="dashboard-section">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-icon users"><el-icon><User /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
                  <div class="stat-label">总用户数</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-icon articles"><el-icon><Document /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalArticles || 0 }}</div>
                  <div class="stat-label">总文章数</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-icon views"><el-icon><View /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalViews || 0 }}</div>
                  <div class="stat-label">总浏览量</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-icon comments"><el-icon><ChatDotRound /></el-icon></div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalComments || 0 }}</div>
                  <div class="stat-label">总评论数</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 文章管理 -->
        <div v-if="activeMenu === 'articles'" class="articles-section">
          <div class="section-header">
            <h3>文章管理</h3>
            <div class="header-actions">
              <el-input
                v-model="articleKeyword"
                placeholder="搜索文章..."
                clearable
                style="width: 200px"
                @clear="loadArticles"
                @keyup.enter="loadArticles"
              >
                <template #append>
                  <el-button @click="loadArticles"><el-icon><Search /></el-icon></el-button>
                </template>
              </el-input>
              <el-select v-model="articleStatus" placeholder="状态" style="width: 120px" clearable @change="loadArticles">
                <el-option label="草稿" :value="0" />
                <el-option label="已发布" :value="1" />
                <el-option label="已下架" :value="2" />
              </el-select>
            </div>
          </div>

          <el-table :data="articles" v-loading="articlesLoading" stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="authorId" label="作者 ID" width="80" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" type="info">草稿</el-tag>
                <el-tag v-else-if="row.status === 1" type="success">已发布</el-tag>
                <el-tag v-else type="danger">已下架</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览" width="80" />
            <el-table-column prop="likeCount" label="点赞" width="80" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="handleEditArticle(row.id)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteArticle(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="articlePage"
              v-model:page-size="articlePageSize"
              :total="articleTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @current-change="loadArticles"
              @size-change="loadArticles"
            />
          </div>
        </div>

        <!-- 用户管理 -->
        <div v-if="activeMenu === 'users'" class="users-section">
          <div class="section-header">
            <h3>用户管理</h3>
            <div class="header-actions">
              <el-input
                v-model="userKeyword"
                placeholder="搜索用户..."
                clearable
                style="width: 200px"
                @clear="loadUsers"
                @keyup.enter="loadUsers"
              >
                <template #append>
                  <el-button @click="loadUsers"><el-icon><Search /></el-icon></el-button>
                </template>
              </el-input>
            </div>
          </div>

          <el-table :data="users" v-loading="usersLoading" stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column prop="role" label="角色" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.role === 3" type="danger">管理员</el-tag>
                <el-tag v-else-if="row.role === 2" type="warning">作者</el-tag>
                <el-tag v-else>普通用户</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.status === 1" type="success">正常</el-tag>
                <el-tag v-else type="danger">禁用</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="provider" label="登录方式" width="100" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="handleToggleUserStatus(row)">
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button size="small" type="danger" @click="handleDeleteUser(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="userPage"
              v-model:page-size="userPageSize"
              :total="userTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @current-change="loadUsers"
              @size-change="loadUsers"
            />
          </div>
        </div>

        <!-- 评论管理 -->
        <div v-if="activeMenu === 'comments'" class="comments-section">
          <div class="section-header">
            <h3>评论管理</h3>
          </div>
          <el-empty description="评论管理功能开发中..." />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  getStatsApi,
  getAdminArticlesApi,
  deleteArticleApi,
  getAdminUsersApi,
  updateUserStatusApi,
  deleteUserApi
} from '@/api'
import type { Article, User } from '@/types'

const authStore = useAuthStore()

const activeMenu = ref('dashboard')
const menuName = computed(() => {
  const names: Record<string, string> = {
    dashboard: '数据统计',
    articles: '文章管理',
    users: '用户管理',
    comments: '评论管理'
  }
  return names[activeMenu.value]
})

// 统计信息
const stats = reactive({
  totalUsers: 0,
  totalArticles: 0,
  totalViews: 0,
  totalComments: 0,
  activeUsers: 0,
  publishedArticles: 0,
  draftArticles: 0,
  totalLikes: 0
})

// 文章管理
const articlesLoading = ref(false)
const articles = ref<Article[]>([])
const articlePage = ref(1)
const articlePageSize = ref(10)
const articleTotal = ref(0)
const articleKeyword = ref('')
const articleStatus = ref<number | undefined>(undefined)

// 用户管理
const usersLoading = ref(false)
const users = ref<User[]>([])
const userPage = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)
const userKeyword = ref('')

const handleMenuSelect = (index: string) => {
  activeMenu.value = index
  if (index === 'articles') {
    loadArticles()
  } else if (index === 'users') {
    loadUsers()
  }
}

const loadStats = async () => {
  try {
    const res = await getStatsApi()
    Object.assign(stats, res.data)
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const loadArticles = async () => {
  articlesLoading.value = true
  try {
    const res = await getAdminArticlesApi(
      articlePage.value,
      articlePageSize.value,
      undefined,
      articleKeyword.value,
      articleStatus.value
    )
    articles.value = res.data
    articleTotal.value = res.total
  } catch (error) {
    console.error('加载文章失败:', error)
  } finally {
    articlesLoading.value = false
  }
}

const loadUsers = async () => {
  usersLoading.value = true
  try {
    const res = await getAdminUsersApi(userPage.value, userPageSize.value, userKeyword.value)
    users.value = res.data
    userTotal.value = res.total
  } catch (error) {
    console.error('加载用户失败:', error)
  } finally {
    usersLoading.value = false
  }
}

const handleEditArticle = (articleId: number) => {
  window.open(`/article/${articleId}/edit`, '_blank')
}

const handleDeleteArticle = async (articleId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteArticleApi(articleId)
    ElMessage.success('删除成功')
    loadArticles()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleToggleUserStatus = async (user: User) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1
    await ElMessageBox.confirm(`确定要${newStatus === 1 ? '启用' : '禁用'}该用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateUserStatusApi(user.id, newStatus)
    ElMessage.success('操作成功')
    user.status = newStatus
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleDeleteUser = async (userId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteUserApi(userId)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 监听菜单变化加载数据
watch(activeMenu, (newMenu) => {
  if (newMenu === 'dashboard') {
    loadStats()
  }
})

onMounted(() => {
  loadStats()
})
</script>

<style scoped lang="scss">
.admin-dashboard {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.admin-sidebar {
  width: 220px;
  background: #304156;
  flex-shrink: 0;

  .sidebar-header {
    padding: 1.5rem;
    background: #2b3a4a;

    h2 {
      color: #fff;
      font-size: 1.25rem;
      margin: 0;
    }
  }

  .admin-menu {
    border-right: none;
  }
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.admin-header {
  background: #fff;
  padding: 1rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);

  .header-right {
    display: flex;
    align-items: center;
    gap: 1rem;

    .admin-user {
      color: #606266;
    }
  }
}

.admin-content {
  flex: 1;
  padding: 1.5rem;
}

.dashboard-section {
  .stat-card {
    display: flex;
    align-items: center;
    padding: 1.5rem;

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      color: #fff;
      margin-right: 1rem;

      &.users { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
      &.articles { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
      &.views { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
      &.comments { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
    }

    .stat-info {
      .stat-value {
        font-size: 1.75rem;
        font-weight: bold;
        color: #333;
      }

      .stat-label {
        color: #999;
        font-size: 0.875rem;
        margin-top: 0.25rem;
      }
    }
  }
}

.articles-section,
.users-section,
.comments-section {
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;

    h3 {
      font-size: 1.25rem;
      margin: 0;
    }

    .header-actions {
      display: flex;
      gap: 1rem;
    }
  }

  .pagination {
    margin-top: 1.5rem;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
