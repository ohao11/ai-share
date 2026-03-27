<template>
  <div class="home-page">
    <!-- 头部导航 -->
    <header class="site-header">
      <div class="header-container">
        <div class="header-left">
          <router-link to="/" class="logo">
            <span class="logo-icon">✦</span>
            <span class="logo-text">实战AI</span>
          </router-link>
        </div>
        <nav class="main-nav">
          <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>
          <template v-if="authStore.isAuthenticated">
            <router-link to="/article/create" class="nav-item create-btn">
              <el-icon><Plus /></el-icon>
              <span>写文章</span>
            </router-link>
            <router-link to="/user" class="nav-item user-menu">
              <el-avatar :size="28" v-if="authStore.user?.avatar" :src="authStore.user.avatar" />
              <span v-else class="user-avatar-placeholder">{{ userInitial }}</span>
              <span class="user-name">{{ authStore.user?.username }}</span>
            </router-link>
            <a href="#" @click.prevent="handleLogout" class="nav-item logout-btn">
              <el-icon><SwitchButton /></el-icon>
              <span>退出</span>
            </a>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-item login-btn">登录</router-link>
            <router-link to="/register" class="nav-item register-btn">注册</router-link>
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
            <!-- 分类标签导航 -->
            <div class="category-tabs">
              <span
                class="category-tab"
                :class="{ active: selectedCategoryId === null }"
                @click="selectCategory(null)"
              >
                全部
              </span>
              <span
                v-for="cat in categories"
                :key="cat.id"
                class="category-tab"
                :class="{ active: selectedCategoryId === cat.id }"
                @click="selectCategory(cat.id)"
              >
                {{ cat.name }}
              </span>
            </div>

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
                  <!-- 封面图 -->
                  <div v-if="article.coverImage" class="article-cover">
                    <img :src="article.coverImage" alt="封面" />
                  </div>
                  <div class="article-content-wrapper">
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
                      <span class="meta-item time">{{ formatDate(article.createdAt || '') }}</span>
                    </div>
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
                  :class="{ active: selectedTagId === tag.id }"
                  @click="selectTag(tag.id)"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
            </div>

            <div class="sidebar-widget">
              <h3>分类</h3>
              <ul class="category-list">
                <li v-for="cat in categories" :key="cat.id" :class="{ active: selectedCategoryId === cat.id }">
                  <a href="#" @click.prevent="selectCategory(cat.id)">{{ cat.name }}</a>
                  <span class="count">{{ cat.articleCount || 0 }} 篇</span>
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
        <p>&copy; 2026 实战AI. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getArticlesApi, getHotTagsApi, getCategoriesApi, logoutApi, getArticlesByTagApi } from '@/api'
import type { Article, Tag, Category } from '@/types'
import { formatDate } from '@/utils/format'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const articles = ref<Article[]>([])
const tags = ref<Tag[]>([])
const categories = ref<Category[]>([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedCategoryId = ref<number | null>(null)
const selectedTagId = ref<number | null>(null)

const userInitial = computed(() => {
  const username = authStore.user?.username || 'U'
  return username.charAt(0).toUpperCase()
})

const loadArticles = async () => {
  loading.value = true
  try {
    if (selectedTagId.value) {
      // 按标签筛选
      const res = await getArticlesByTagApi(selectedTagId.value, currentPage.value, pageSize.value)
      articles.value = res.data
      total.value = res.total
    } else {
      // 正常加载或按分类筛选
      const res = await getArticlesApi(currentPage.value, pageSize.value, selectedCategoryId.value ?? undefined, keyword.value)
      articles.value = res.data
      total.value = res.total
    }
  } catch (error) {
    console.error('加载文章失败:', error)
  } finally {
    loading.value = false
  }
}

const selectCategory = (categoryId: number | null) => {
  selectedCategoryId.value = categoryId
  selectedTagId.value = null // 清除标签筛选
  currentPage.value = 1
  loadArticles()
}

const selectTag = (tagId: number | null) => {
  if (selectedTagId.value === tagId) {
    // 点击已选中的标签，取消选中
    selectedTagId.value = null
  } else {
    selectedTagId.value = tagId
    selectedCategoryId.value = null // 清除分类筛选
  }
  currentPage.value = 1
  loadArticles()
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
  background: linear-gradient(135deg, #fff 0%, #f8f9ff 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 0.75rem 0;
  position: sticky;
  top: 0;
  z-index: 1000;

  .header-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-left {
    display: flex;
    align-items: center;
  }

  .logo {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    text-decoration: none;
    font-size: 1.35rem;
    font-weight: 700;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    transition: all 0.3s ease;
    padding: 0.5rem 0.75rem;
    border-radius: 8px;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
    }

    .logo-icon {
      font-size: 1.5rem;
      animation: pulse 2s ease-in-out infinite;
    }

    .logo-text {
      letter-spacing: 0.5px;
    }
  }

  .main-nav {
    display: flex;
    gap: 0.5rem;
    align-items: center;

    .nav-item {
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
      padding: 0.5rem 0.875rem;
      border-radius: 8px;
      text-decoration: none;
      color: #4a5568;
      font-size: 0.925rem;
      font-weight: 500;
      transition: all 0.25s ease;
      cursor: pointer;
      border: none;
      background: transparent;

      .el-icon {
        font-size: 1.1rem;
      }

      &:hover {
        background: rgba(102, 126, 234, 0.08);
        color: #667eea;
      }

      &.active {
        background: rgba(102, 126, 234, 0.12);
        color: #667eea;
      }

      .user-avatar-placeholder {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.75rem;
        font-weight: 600;
      }

      .user-name {
        margin-left: 0.25rem;
      }
    }

    .create-btn {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        color: #fff;
      }
    }

    .login-btn, .register-btn {
      font-weight: 600;

      &:hover {
        background: rgba(102, 126, 234, 0.12);
      }
    }

    .login-btn {
      color: #667eea;
    }

    .register-btn {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      color: #fff;
      box-shadow: 0 2px 8px rgba(245, 87, 108, 0.3);

      &:hover {
        box-shadow: 0 4px 12px rgba(245, 87, 108, 0.4);
        color: #fff;
      }
    }

    .logout-btn {
      color: #e53e3e;

      &:hover {
        background: rgba(229, 62, 62, 0.1);
        color: #e53e3e;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
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

  .category-tabs {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #eee;

    .category-tab {
      padding: 0.5rem 1rem;
      border-radius: 20px;
      font-size: 0.9rem;
      cursor: pointer;
      transition: all 0.25s ease;
      color: #666;
      background: #f5f7fa;
      border: 1px solid transparent;

      &:hover {
        color: #667eea;
        background: rgba(102, 126, 234, 0.08);
      }

      &.active {
        color: #fff;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
      }
    }
  }

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
        display: flex;
        gap: 1.25rem;

        &:hover .article-title {
          color: #409eff;
        }
      }

      .article-cover {
        flex-shrink: 0;
        width: 180px;
        height: 120px;
        border-radius: 8px;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .article-content-wrapper {
        flex: 1;
        min-width: 0;
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
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
      }

      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        border-color: transparent;
      }
    }
  }

  .category-list {
    list-style: none;

    li {
      display: flex;
      justify-content: space-between;
      padding: 0.5rem 0;
      border-bottom: 1px solid #f0f0f0;
      transition: all 0.2s ease;

      &:last-child {
        border-bottom: none;
      }

      &.active {
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
        padding: 0.5rem 0.75rem;
        margin: 0 -0.75rem;
        border-radius: 6px;

        a {
          color: #667eea;
          font-weight: 600;
        }
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
