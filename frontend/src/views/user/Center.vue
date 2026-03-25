<template>
  <div class="user-center">
    <!-- 头部导航 -->
    <header class="site-header">
      <div class="container">
        <router-link to="/" class="back-link">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </router-link>
      </div>
    </header>

    <!-- 主要内容 -->
    <main class="main-content">
      <div class="container">
        <div class="user-content">
          <!-- 侧边栏 -->
          <aside class="user-sidebar">
            <div class="user-profile">
              <div class="avatar">
                <img v-if="authStore.user?.avatar" :src="authStore.user.avatar" alt="头像" />
                <el-icon v-else size="60"><User /></el-icon>
              </div>
              <h2 class="username">{{ authStore.user?.username }}</h2>
              <p class="email">{{ authStore.user?.email }}</p>
              <el-tag v-if="authStore.isAdmin" type="danger" size="small">管理员</el-tag>
              <el-tag v-else-if="authStore.user?.role === 2" type="warning" size="small">作者</el-tag>
            </div>

            <el-menu :default-active="activeMenu" class="user-menu" @select="handleMenuSelect">
              <el-menu-item index="profile">
                <el-icon><User /></el-icon>
                个人资料
              </el-menu-item>
              <el-menu-item index="articles">
                <el-icon><Document /></el-icon>
                我的文章
              </el-menu-item>
              <el-menu-item index="create">
                <el-icon><Edit /></el-icon>
                写文章
              </el-menu-item>
              <el-menu-item v-if="authStore.isAdmin" index="admin">
                <el-icon><Setting /></el-icon>
                管理后台
              </el-menu-item>
            </el-menu>
          </aside>

          <!-- 主内容区 -->
          <div class="user-main">
            <!-- 个人资料 -->
            <div v-if="activeMenu === 'profile'" class="profile-section">
              <h3>个人资料</h3>
              <el-form ref="formRef" :model="profileForm" label-width="100px">
                <el-form-item label="用户名">
                  <el-input v-model="profileForm.username" :disabled="true" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="profileForm.email" :disabled="true" />
                </el-form-item>
                <el-form-item label="角色">
                  <el-tag v-if="authStore.isAdmin" type="danger">管理员</el-tag>
                  <el-tag v-else-if="authStore.user?.role === 2" type="warning">作者</el-tag>
                  <el-tag v-else>普通用户</el-tag>
                </el-form-item>
                <el-form-item label="注册时间">
                  {{ formatDate(authStore.user?.createdAt || '') }}
                </el-form-item>
              </el-form>
            </div>

            <!-- 我的文章 -->
            <div v-if="activeMenu === 'articles'" class="articles-section">
              <div class="section-header">
                <h3>我的文章</h3>
                <el-button type="primary" @click="handleCreateArticle">
                  <el-icon><Plus /></el-icon>
                  写文章
                </el-button>
              </div>

              <div v-loading="articlesLoading" class="articles-list">
                <div v-if="myArticles.length === 0 && !articlesLoading" class="empty-state">
                  <el-empty description="暂无文章" />
                </div>

                <div v-for="article in myArticles" :key="article.id" class="article-item">
                  <div class="article-info">
                    <h4 class="article-title">{{ article.title }}</h4>
                    <p class="article-summary">{{ article.summary }}</p>
                    <div class="article-meta">
                      <el-tag v-if="article.status === 0" type="info" size="small">草稿</el-tag>
                      <el-tag v-else-if="article.status === 1" type="success" size="small">已发布</el-tag>
                      <span class="meta-item">浏览：{{ article.viewCount }}</span>
                      <span class="meta-item">点赞：{{ article.likeCount }}</span>
                      <span class="meta-item">{{ formatDate(article.createdAt || '') }}</span>
                    </div>
                  </div>
                  <div class="article-actions">
                    <el-button size="small" @click="handleEditArticle(article.id)">编辑</el-button>
                    <el-button size="small" type="danger" @click="handleDeleteArticle(article.id)">删除</el-button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 写文章 -->
            <div v-if="activeMenu === 'create'" class="create-section">
              <router-view />
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getArticlesByAuthorApi, deleteArticleApi } from '@/api'
import { formatDate } from '@/utils/format'
import type { Article } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const activeMenu = ref('profile')
const articlesLoading = ref(false)
const myArticles = ref<Article[]>([])

const profileForm = reactive({
  username: authStore.user?.username || '',
  email: authStore.user?.email || ''
})

const loadMyArticles = async () => {
  if (!authStore.user?.id) return

  articlesLoading.value = true
  try {
    const res = await getArticlesByAuthorApi(authStore.user.id, 1, 50)
    myArticles.value = res.data
  } catch (error) {
    console.error('加载文章失败:', error)
  } finally {
    articlesLoading.value = false
  }
}

const handleMenuSelect = (index: string) => {
  activeMenu.value = index

  if (index === 'articles') {
    loadMyArticles()
  } else if (index === 'admin') {
    router.push('/admin')
  } else if (index === 'create') {
    router.push('/article/create')
  }
}

const handleCreateArticle = () => {
  router.push('/article/create')
}

const handleEditArticle = (id: number) => {
  router.push(`/article/${id}/edit`)
}

const handleDeleteArticle = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteArticleApi(id)
    ElMessage.success('删除成功')
    loadMyArticles()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

watch(() => route.path, () => {
  if (route.path.includes('create') || route.path.includes('edit')) {
    activeMenu.value = 'create'
  }
})

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  // 根据路由设置活动菜单
  if (route.path.includes('articles') || route.path.includes('article')) {
    activeMenu.value = 'articles'
  }
})
</script>

<style scoped lang="scss">
.user-center {
  min-height: 100vh;
  background: #f5f7fa;
}

.site-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1rem 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
  }

  .back-link {
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
    color: #333;
    text-decoration: none;

    &:hover {
      color: #409eff;
    }
  }
}

.main-content {
  padding: 2rem 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
  }
}

.user-content {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 2rem;
}

.user-sidebar {
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  height: fit-content;
}

.user-profile {
  text-align: center;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #eee;
  margin-bottom: 1.5rem;

  .avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    overflow: hidden;
    margin: 0 auto 1rem;
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .username {
    font-size: 1.25rem;
    color: #333;
    margin-bottom: 0.5rem;
  }

  .email {
    color: #999;
    font-size: 0.875rem;
    margin-bottom: 0.75rem;
  }
}

.user-menu {
  border-right: none;

  .el-menu-item {
    height: 50px;
    line-height: 50px;
    border-radius: 4px;

    &:hover {
      background: #f5f7fa;
    }

    &.is-active {
      background: #ecf5ff;
      color: #409eff;
    }
  }
}

.user-main {
  background: #fff;
  border-radius: 8px;
  padding: 2rem;
  min-height: 500px;

  h3 {
    font-size: 1.25rem;
    margin-bottom: 1.5rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #eee;
  }
}

.profile-section {
  .el-form {
    max-width: 500px;
  }
}

.articles-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;

    h3 {
      margin-bottom: 0;
      border-bottom: none;
      padding-bottom: 0;
    }
  }

  .articles-list {
    .article-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 1.5rem 0;
      border-bottom: 1px solid #eee;

      &:last-child {
        border-bottom: none;
      }

      .article-info {
        flex: 1;

        .article-title {
          font-size: 1.125rem;
          color: #333;
          margin-bottom: 0.5rem;
        }

        .article-summary {
          color: #666;
          font-size: 0.9rem;
          margin-bottom: 0.75rem;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .article-meta {
          display: flex;
          align-items: center;
          gap: 1rem;
          font-size: 0.85rem;
          color: #999;

          .meta-item {
            color: #999;
          }
        }
      }

      .article-actions {
        display: flex;
        gap: 0.5rem;
      }
    }
  }

  .empty-state {
    padding: 3rem 0;
    text-align: center;
  }
}
</style>
