<template>
  <div class="home-page">
    <!-- 头部导航 -->
    <header class="site-header">
      <div class="container">
        <div class="logo">AI Share</div>
        <nav class="main-nav">
          <router-link to="/">首页</router-link>
          <div class="nav-dropdown">
            <a href="#" @click.prevent="showCategories = !showCategories">分类 ▾</a>
            <ul v-if="showCategories" class="dropdown-menu">
              <li v-for="cat in categories" :key="cat.id">
                <router-link :to="`/category/${cat.slug}`">{{ cat.name }}</router-link>
              </li>
            </ul>
          </div>
          <router-link to="/admin" v-if="authStore.isAdmin">管理</router-link>
          <template v-if="authStore.isAuthenticated">
            <span class="user-name">{{ authStore.user?.username }}</span>
            <a href="#" @click.prevent="handleLogout">退出</a>
          </template>
          <template v-else>
            <router-link to="/login">登录</router-link>
            <router-link to="/register">注册</router-link>
          </template>
        </nav>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="container">
        <div class="content-wrapper">
          <!-- 文章列表 -->
          <div class="article-list">
            <div class="filters">
              <el-input
                v-model="keyword"
                placeholder="搜索文章..."
                clearable
                @clear="loadArticles"
                @keyup.enter="loadArticles"
              >
                <template #append>
                  <el-button @click="loadArticles">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </div>

            <div v-loading="loading" class="articles">
              <div v-if="articles.length === 0 && !loading" class="empty-state">
                <el-empty description="暂无文章" />
              </div>

              <div v-for="article in articles" :key="article.id" class="article-card">
                <router-link :to="`/article/${article.id}`" class="article-link">
                  <h2 class="article-title">{{ article.title }}</h2>
                  <p class="article-summary">{{ article.summary }}</p>
                  <div class="article-meta">
                    <span class="meta-item">
                      <el-icon><View /></el-icon>
                      {{ article.viewCount }}
                    </span>
                    <span class="meta-item">
                      <el-icon><Star /></el-icon>
                      {{ article.likeCount }}
                    </span>
                    <span class="meta-item">
                      <el-icon><ChatDotRound /></el-icon>
                      {{ article.commentCount }}
                    </span>
                    <span class="meta-item time">{{ formatDate(article.createdAt) }}</span>
                  </div>
                </router-link>
              </div>
            </div>

            <!-- 分页 -->
            <div class="pagination" v-if="total > 0">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total="total"
                :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next"
                @current-change="loadArticles"
                @size-change="loadArticles"
              />
            </div>
          </div>

          <!-- 侧边栏 -->
          <aside class="sidebar">
            <div class="sidebar-widget">
              <h3>热门标签</h3>
              <div class="tag-cloud">
                <el-tag
                  v-for="tag in tags"
                  :key="tag.id"
                  size="small"
                  class="tag-item"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
            </div>

            <div class="sidebar-widget">
              <h3>分类</h3>
              <ul class="category-list">
                <li v-for="cat in categories" :key="cat.id">
                  <router-link :to="`/category/${cat.slug}`">{{ cat.name }}</router-link>
                  <span class="count">({{ cat.sortOrder }})</span>
                </li>
              </ul>
            </div>
          </aside>
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="site-footer">
      <div class="container">
        <p>&copy; 2026 AI Share. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getArticlesApi, getHotTagsApi, getCategoriesApi, logoutApi } from '@/api'
import type { Article, Tag, Category } from '@/types'
import { formatDate } from '@/utils/format'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const articles = ref<Article[]>([])
const tags = ref<Tag[]>([])
const categories = ref<Category[]>([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showCategories = ref(false)

const loadArticles = async () => {
  loading.value = true
  try {
    const categoryId = route.params.slug ? undefined : undefined
    const res = await getArticlesApi(currentPage.value, pageSize.value, categoryId, keyword.value)
    articles.value = res.data.data
    total.value = res.data.total
  } catch (error) {
    console.error('加载文章失败:', error)
  } finally {
    loading.value = false
  }
}

const loadTags = async () => {
  try {
    const res = await getHotTagsApi(10)
    tags.value = res.data
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoriesApi()
    categories.value = res.data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const handleLogout = async () => {
  try {
    await logoutApi()
    authStore.clearAuth()
    router.push('/')
  } catch (error) {
    console.error('退出失败:', error)
  }
}

onMounted(() => {
  loadArticles()
  loadTags()
  loadCategories()
})
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.site-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1rem 0;

  .container {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .logo {
    font-size: 1.5rem;
    font-weight: bold;
    color: #409eff;
  }

  .main-nav {
    display: flex;
    gap: 1.5rem;
    align-items: center;

    a {
      color: #333;
      text-decoration: none;
      cursor: pointer;

      &:hover {
        color: #409eff;
      }
    }

    .nav-dropdown {
      position: relative;

      .dropdown-menu {
        position: absolute;
        top: 100%;
        left: 0;
        background: #fff;
        border: 1px solid #eee;
        border-radius: 4px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        padding: 0.5rem 0;
        min-width: 120px;
        z-index: 100;

        li {
          list-style: none;

          a {
            display: block;
            padding: 0.5rem 1rem;
            white-space: nowrap;
          }
        }
      }
    }
  }
}

.main-content {
  flex: 1;
  padding: 2rem 0;
  background: #f5f7fa;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
  }

  .content-wrapper {
    display: grid;
    grid-template-columns: 1fr 300px;
    gap: 2rem;
  }
}

.article-list {
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;

  .filters {
    margin-bottom: 1.5rem;
  }

  .articles {
    .article-card {
      border-bottom: 1px solid #eee;
      padding: 1.5rem 0;

      &:last-child {
        border-bottom: none;
      }

      .article-link {
        text-decoration: none;
        color: inherit;

        &:hover .article-title {
          color: #409eff;
        }
      }

      .article-title {
        font-size: 1.25rem;
        margin-bottom: 0.75rem;
        transition: color 0.2s;
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
        gap: 1rem;
        font-size: 0.85rem;
        color: #999;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 0.25rem;
        }
      }
    }
  }

  .pagination {
    margin-top: 2rem;
    display: flex;
    justify-content: center;
  }
}

.sidebar {
  .sidebar-widget {
    background: #fff;
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 1.5rem;

    h3 {
      font-size: 1rem;
      margin-bottom: 1rem;
      padding-bottom: 0.5rem;
      border-bottom: 1px solid #eee;
    }
  }

  .tag-cloud {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;

    .tag-item {
      cursor: pointer;
    }
  }

  .category-list {
    list-style: none;

    li {
      display: flex;
      justify-content: space-between;
      padding: 0.5rem 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      a {
        color: #333;
        text-decoration: none;

        &:hover {
          color: #409eff;
        }
      }

      .count {
        color: #999;
        font-size: 0.85rem;
      }
    }
  }
}

.site-footer {
  background: #333;
  color: #fff;
  padding: 1.5rem 0;
  text-align: center;
}

.empty-state {
  padding: 3rem 0;
  text-align: center;
}
</style>
