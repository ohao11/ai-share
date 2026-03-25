<template>
  <div class="article-detail">
    <!-- 头部导航 -->
    <header class="site-header">
      <div class="container">
        <router-link to="/" class="back-link">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </router-link>
      </div>
    </header>

    <!-- 文章内容 -->
    <main class="article-content" v-loading="loading">
      <div class="container" v-if="article">
        <article>
          <h1 class="article-title">{{ article.title }}</h1>

          <div class="article-meta">
            <span class="meta-item">作者：{{ article.authorName || '未知' }}</span>
            <span class="meta-item">发布于：{{ formatDate(article.createdAt || '') }}</span>
            <span class="meta-item">浏览：{{ article.viewCount }}</span>
            <span class="meta-item">点赞：{{ article.likeCount }}</span>
          </div>

          <div class="article-body" v-html="renderedContent"></div>

          <div class="article-actions">
            <el-button :type="isLiked ? 'danger' : 'primary'" @click="handleLike">
              <el-icon><Star /></el-icon>
              {{ isLiked ? '已点赞' : '点赞' }} ({{ article.likeCount }})
            </el-button>
          </div>
        </article>

        <!-- 评论区域 -->
        <section class="comments-section">
          <h2>评论</h2>

          <div class="comment-form">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="4"
              placeholder="写下你的评论..."
            />
            <el-button type="primary" @click="handlePostComment" :loading="posting">
              发表评论
            </el-button>
          </div>

          <div class="comments-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-meta">
                <span>用户 #{{ comment.userId }}</span>
                <span>{{ formatDate(comment.createdAt) }}</span>
              </div>
            </div>
            <el-empty v-if="comments.length === 0" description="暂无评论" />
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getArticleApi, likeArticleApi, checkLikedApi, getCommentsApi, createCommentApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { formatDate } from '@/utils/format'
import type { Article, Comment } from '@/types'
import { marked } from 'marked'
import hljs from 'highlight.js'

// 配置 marked (v12 使用 renderer 方式处理代码高亮)
const renderer = new marked.Renderer()
renderer.code = function(code: string, infostring: string | undefined, _escaped: boolean): string {
  const lang = infostring || ''
  const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
  const highlighted = hljs.highlight(code, { language }).value
  return `<pre><code class="hljs language-${language}">${highlighted}</code></pre>`
}
marked.use({ renderer, breaks: true, gfm: true })

const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const posting = ref(false)
const article = ref<Article | null>(null)
const comments = ref<Comment[]>([])
const commentContent = ref('')
const isLiked = ref(false)

const renderedContent = computed(() => {
  if (article.value?.content) {
    return marked.parse(article.value.content)
  }
  return ''
})

const loadArticle = async () => {
  loading.value = true
  try {
    const res = await getArticleApi(Number(route.params.id))
    article.value = res.data
  } catch (error) {
    console.error('加载文章失败:', error)
  } finally {
    loading.value = false
  }
}

const loadLikedStatus = async () => {
  if (!authStore.isAuthenticated) return
  try {
    const res = await checkLikedApi(Number(route.params.id))
    isLiked.value = res.data.liked
  } catch (error) {
    console.error('检查点赞状态失败:', error)
  }
}

const loadComments = async () => {
  try {
    const res = await getCommentsApi(Number(route.params.id))
    comments.value = res.data
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

const handleLike = async () => {
  if (!authStore.isAuthenticated) {
    return
  }
  try {
    const res = await likeArticleApi(Number(route.params.id))
    isLiked.value = res.data.liked
    if (article.value) {
      article.value.likeCount = (article.value.likeCount || 0) + (isLiked.value ? 1 : -1)
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

const handlePostComment = async () => {
  if (!commentContent.value.trim()) return

  posting.value = true
  try {
    await createCommentApi(Number(route.params.id), commentContent.value)
    commentContent.value = ''
    await loadComments()
  } catch (error) {
    console.error('发表评论失败:', error)
  } finally {
    posting.value = false
  }
}

onMounted(() => {
  loadArticle()
  loadLikedStatus()
  loadComments()
})
</script>

<style scoped lang="scss">
.article-detail {
  min-height: 100vh;
  background: #f5f7fa;
}

.site-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1rem 0;

  .container {
    max-width: 800px;
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

.article-content {
  padding: 2rem 0;

  .container {
    max-width: 800px;
    margin: 0 auto;
    padding: 0 1rem;
  }

  article {
    background: #fff;
    border-radius: 8px;
    padding: 2rem;
    margin-bottom: 2rem;
  }

  .article-title {
    font-size: 2rem;
    margin-bottom: 1rem;
    color: #333;
  }

  .article-meta {
    display: flex;
    gap: 1.5rem;
    color: #999;
    font-size: 0.9rem;
    margin-bottom: 2rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #eee;
  }

  .article-body {
    font-size: 1.1rem;
    line-height: 1.8;
    color: #333;

    :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
      margin-top: 1.5rem;
      margin-bottom: 1rem;
      font-weight: 600;
    }

    :deep(p) {
      margin-bottom: 1rem;
    }

    :deep(pre) {
      background: #f6f8fa;
      border-radius: 6px;
      padding: 1rem;
      overflow-x: auto;
      margin-bottom: 1rem;
    }

    :deep(code) {
      font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
      font-size: 0.9em;
    }

    :deep(pre code) {
      background: transparent;
      padding: 0;
    }

    :deep(p code) {
      background: #f6f8fa;
      padding: 0.2em 0.4em;
      border-radius: 3px;
    }

    :deep(ul), :deep(ol) {
      padding-left: 2rem;
      margin-bottom: 1rem;
    }

    :deep(li) {
      margin-bottom: 0.5rem;
    }

    :deep(blockquote) {
      border-left: 4px solid #dfe2e5;
      padding-left: 1rem;
      margin: 1rem 0;
      color: #6a737d;
    }

    :deep(img) {
      max-width: 100%;
      height: auto;
    }

    :deep(a) {
      color: #409eff;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }

    :deep(table) {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 1rem;

      th, td {
        border: 1px solid #dfe2e5;
        padding: 0.5rem 0.75rem;
      }

      th {
        background: #f6f8fa;
        font-weight: 600;
      }
    }
  }

  .article-actions {
    margin-top: 2rem;
    padding-top: 2rem;
    border-top: 1px solid #eee;
  }
}

.comments-section {
  background: #fff;
  border-radius: 8px;
  padding: 2rem;

  h2 {
    font-size: 1.25rem;
    margin-bottom: 1.5rem;
  }

  .comment-form {
    margin-bottom: 2rem;

    .el-input {
      margin-bottom: 1rem;
    }
  }

  .comment-item {
    padding: 1rem 0;
    border-bottom: 1px solid #eee;

    &:last-child {
      border-bottom: none;
    }

    .comment-content {
      margin-bottom: 0.5rem;
    }

    .comment-meta {
      font-size: 0.85rem;
      color: #999;
      display: flex;
      gap: 1rem;
    }
  }
}
</style>
