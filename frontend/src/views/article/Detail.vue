<template>
  <div class="article-detail">
    <!-- 头部导航 -->
    <header class="site-header">
      <div class="container">
        <router-link to="/" class="back-link">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </router-link>
        <div class="header-actions">
          <el-button text @click="handleShare">
            <el-icon><Share /></el-icon>
            分享
          </el-button>
        </div>
      </div>
    </header>

    <!-- 文章内容 -->
    <main class="article-content" v-loading="loading">
      <div class="container" v-if="article">
        <!-- 文章头部信息卡片 -->
        <div class="article-header-card">
          <!-- 封面图 -->
          <div v-if="article.coverImage" class="article-cover">
            <img :src="article.coverImage" alt="封面" />
          </div>
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <div class="meta-item author">
              <el-icon><User /></el-icon>
              <span>{{ article.authorName || '未知作者' }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(article.createdAt || '') }}</span>
            </div>
            <div class="meta-item">
              <el-icon><View /></el-icon>
              <span>{{ article.viewCount }} 次阅读</span>
            </div>
            <div class="meta-item likes">
              <el-icon><Star /></el-icon>
              <span>{{ article.likeCount }} 人点赞</span>
            </div>
          </div>
          <!-- 标签 -->
          <div v-if="article.tags && article.tags.length > 0" class="article-tags">
            <router-link
              v-for="tag in article.tags"
              :key="tag.id"
              :to="`/tag/${tag.id}`"
              class="tag-item"
            >
              #{{ tag.name }}
            </router-link>
          </div>
        </div>

        <!-- 文章正文 -->
        <article class="article-body-card">
          <div class="article-body" v-html="renderedContent"></div>
        </article>

        <!-- 文章操作栏 -->
        <div class="article-actions-card">
          <div class="actions-wrapper">
            <el-button
              :type="isLiked ? 'danger' : 'default'"
              :class="['like-btn', { liked: isLiked }]"
              @click="handleLike"
              size="large"
            >
              <el-icon><Star /></el-icon>
              <span>{{ isLiked ? '已点赞' : '点赞' }}</span>
            </el-button>
            <span class="like-count">{{ article.likeCount }}</span>
          </div>
          <div class="actions-tip">如果你觉得这篇文章对你有帮助，请点赞支持一下</div>
        </div>

        <!-- 评论区域 -->
        <section class="comments-section">
          <div class="comments-header">
            <h2>
              <el-icon><ChatDotRound /></el-icon>
              评论 <span class="count">{{ comments.length }}</span>
            </h2>
          </div>

          <div class="comment-form">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="4"
              placeholder="写下你的评论..."
            />
            <div class="form-footer">
              <span class="hint">支持 Markdown 格式</span>
              <el-button type="primary" @click="handlePostComment" :loading="posting" :disabled="!commentContent.trim()">
                发表评论
              </el-button>
            </div>
          </div>

          <div class="comments-list">
            <template v-for="comment in rootComments" :key="comment.id">
              <div class="comment-item">
                <div class="comment-avatar">
                  <el-icon><User /></el-icon>
                </div>
                <div class="comment-main">
                  <div class="comment-header">
                    <span class="comment-author">用户 #{{ comment.userId }}</span>
                    <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                  <div class="comment-actions">
                    <el-button type="primary" link size="small" @click="handleReply(comment.id)">
                      <el-icon><ChatLineRound /></el-icon>
                      回复
                    </el-button>
                  </div>

                  <!-- 回复表单 -->
                  <div v-if="replyingTo === comment.id" class="reply-form">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="3"
                      placeholder="写下你的回复..."
                    />
                    <div class="reply-actions">
                      <el-button size="small" @click="cancelReply">取消</el-button>
                      <el-button type="primary" size="small" @click="handlePostReply(comment.id)" :loading="posting">回复</el-button>
                    </div>
                  </div>

                  <!-- 子评论 -->
                  <div v-if="getChildComments(comment.id).length > 0" class="replies">
                    <div v-for="child in getChildComments(comment.id)" :key="child.id" class="comment-item reply">
                      <div class="comment-avatar small">
                        <el-icon><User /></el-icon>
                      </div>
                      <div class="comment-main">
                        <div class="comment-header">
                          <span class="comment-author">用户 #{{ child.userId }}</span>
                          <span class="comment-time">{{ formatDate(child.createdAt) }}</span>
                        </div>
                        <div class="comment-content">{{ child.content }}</div>
                        <div class="comment-actions">
                          <el-button type="primary" link size="small" @click="handleReply(child.id)">
                            <el-icon><ChatLineRound /></el-icon>
                            回复
                          </el-button>
                        </div>

                        <!-- 子评论回复表单 -->
                        <div v-if="replyingTo === child.id" class="reply-form">
                          <el-input
                            v-model="replyContent"
                            type="textarea"
                            :rows="3"
                            placeholder="写下你的回复..."
                          />
                          <div class="reply-actions">
                            <el-button size="small" @click="cancelReply">取消</el-button>
                            <el-button type="primary" size="small" @click="handlePostReply(child.id)" :loading="posting">回复</el-button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
            <el-empty v-if="comments.length === 0" description="暂无评论，快来抢沙发吧~" />
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
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
const replyingTo = ref<number | null>(null)
const replyContent = ref('')

const renderedContent = computed(() => {
  if (article.value?.content) {
    return marked.parse(article.value.content)
  }
  return ''
})

// 获取根评论（没有父评论的）
const rootComments = computed(() => {
  return comments.value.filter(c => !c.parentId)
})

// 获取子评论
const getChildComments = (parentId: number) => {
  return comments.value.filter(c => c.parentId === parentId)
}

const handleShare = () => {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

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
  if (!authStore.isAuthenticated) {
    return
  }
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

const handleReply = (commentId: number) => {
  if (!authStore.isAuthenticated) {
    return
  }
  replyingTo.value = commentId
  replyContent.value = ''
}

const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

const handlePostReply = async (parentId: number) => {
  if (!replyContent.value.trim()) return

  posting.value = true
  try {
    await createCommentApi(Number(route.params.id), replyContent.value, parentId)
    replyContent.value = ''
    replyingTo.value = null
    await loadComments()
  } catch (error) {
    console.error('回复评论失败:', error)
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
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.site-header {
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 1rem 0;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(10px);

  .container {
    max-width: 800px;
    margin: 0 auto;
    padding: 0 1.5rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .back-link {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    color: #333;
    text-decoration: none;
    font-weight: 500;
    padding: 0.5rem 1rem;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      color: #409eff;
      background: #ecf5ff;
    }
  }

  .header-actions {
    .el-button {
      font-weight: 500;
    }
  }
}

.article-content {
  padding: 2rem 0 4rem;

  .container {
    max-width: 800px;
    margin: 0 auto;
    padding: 0 1rem;
  }
}

// 文章头部卡片
.article-header-card {
  background: #fff;
  border-radius: 16px;
  padding: 2.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.article-cover {
  margin-bottom: 1.5rem;
  border-radius: 12px;
  overflow: hidden;

  img {
    width: 100%;
    max-height: 400px;
    object-fit: cover;
    border-radius: 12px;
  }
}

.article-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  color: #1a1a1a;
  line-height: 1.4;
  letter-spacing: -0.02em;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: #666;
    font-size: 0.9rem;
    padding: 0.4rem 0.8rem;
    background: #f5f7fa;
    border-radius: 20px;
    transition: all 0.3s ease;

    .el-icon {
      font-size: 1rem;
      color: #909399;
    }

    &.author {
      color: #409eff;
      background: #ecf5ff;
      .el-icon { color: #409eff; }
    }

    &.likes {
      color: #f56c6c;
      background: #fef0f0;
      .el-icon { color: #f56c6c; }
    }
  }
}

.article-tags {
  margin-top: 1.25rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;

  .tag-item {
    display: inline-block;
    padding: 0.35rem 0.85rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 20px;
    font-size: 0.85rem;
    text-decoration: none;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
  }
}

// 文章正文卡片
.article-body-card {
  background: #fff;
  border-radius: 16px;
  padding: 2.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.article-body {
  font-size: 1.1rem;
  line-height: 1.9;
  color: #333;

  :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
    margin-top: 2rem;
    margin-bottom: 1rem;
    font-weight: 600;
    color: #1a1a1a;
    letter-spacing: -0.01em;
  }

  :deep(h1) { font-size: 1.75rem; }
  :deep(h2) { font-size: 1.5rem; }
  :deep(h3) { font-size: 1.25rem; }

  :deep(p) {
    margin-bottom: 1.25rem;
  }

  :deep(pre) {
    background: #282c34;
    border-radius: 12px;
    padding: 1.25rem;
    overflow-x: auto;
    margin-bottom: 1.5rem;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  :deep(code) {
    font-family: 'JetBrains Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
    font-size: 0.9em;
  }

  :deep(pre code) {
    background: transparent;
    padding: 0;
    color: #abb2bf;
  }

  :deep(p code) {
    background: #f5f7fa;
    padding: 0.2em 0.5em;
    border-radius: 4px;
    color: #e96900;
    font-size: 0.85em;
  }

  :deep(ul), :deep(ol) {
    padding-left: 1.5rem;
    margin-bottom: 1.25rem;
  }

  :deep(li) {
    margin-bottom: 0.5rem;
  }

  :deep(blockquote) {
    border-left: 4px solid #409eff;
    padding: 1rem 1.5rem;
    margin: 1.5rem 0;
    color: #666;
    background: #f5f7fa;
    border-radius: 0 8px 8px 0;
    font-style: italic;
  }

  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 8px;
    margin: 1rem 0;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  :deep(a) {
    color: #409eff;
    text-decoration: none;
    border-bottom: 1px dashed #409eff;
    transition: all 0.3s ease;

    &:hover {
      color: #66b1ff;
      border-bottom-color: #66b1ff;
    }
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 1.5rem;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    th, td {
      border: 1px solid #ebeef5;
      padding: 0.75rem 1rem;
      text-align: left;
    }

    th {
      background: #f5f7fa;
      font-weight: 600;
      color: #333;
    }

    tr:hover td {
      background: #fafafa;
    }
  }

  :deep(hr) {
    border: none;
    height: 2px;
    background: linear-gradient(90deg, transparent, #ddd, transparent);
    margin: 2rem 0;
  }
}

// 文章操作栏
.article-actions-card {
  background: #fff;
  border-radius: 16px;
  padding: 2rem 2.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  text-align: center;

  .actions-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 1rem;
  }

  .like-btn {
    border-radius: 24px;
    padding: 0.75rem 2rem;
    font-weight: 500;
    transition: all 0.3s ease;

    &.liked {
      background: #f56c6c;
      border-color: #f56c6c;
      color: #fff;

      &:hover {
        background: #f78989;
        border-color: #f78989;
      }
    }
  }

  .like-count {
    font-size: 1.5rem;
    font-weight: 700;
    color: #f56c6c;
  }

  .actions-tip {
    color: #909399;
    font-size: 0.9rem;
  }
}

// 评论区域
.comments-section {
  background: #fff;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);

  .comments-header {
    h2 {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 1.25rem;
      margin-bottom: 1.5rem;
      color: #333;

      .el-icon {
        color: #409eff;
      }

      .count {
        background: #409eff;
        color: #fff;
        font-size: 0.85rem;
        padding: 0.1rem 0.6rem;
        border-radius: 10px;
      }
    }
  }

  .comment-form {
    margin-bottom: 2rem;
    padding: 1.5rem;
    background: #f5f7fa;
    border-radius: 12px;

    .el-textarea {
      --el-input-bg-color: #fff;
    }

    .form-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 1rem;

      .hint {
        color: #909399;
        font-size: 0.85rem;
      }
    }
  }

  .comments-list {
    .comment-item {
      display: flex;
      gap: 1rem;
      padding: 1.25rem 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      &.reply {
        padding: 1rem 0;
      }
    }

    .comment-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: linear-gradient(135deg, #409eff, #66b1ff);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;

      &.small {
        width: 32px;
        height: 32px;
      }
    }

    .comment-main {
      flex: 1;
      min-width: 0;
    }

    .comment-header {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      margin-bottom: 0.5rem;

      .comment-author {
        font-weight: 600;
        color: #333;
      }

      .comment-time {
        font-size: 0.8rem;
        color: #909399;
      }
    }

    .comment-content {
      color: #555;
      line-height: 1.6;
      margin-bottom: 0.5rem;
    }

    .comment-actions {
      .el-button {
        padding: 0;
        font-size: 0.85rem;
      }
    }

    .reply-form {
      margin-top: 1rem;
      padding: 1rem;
      background: #f9fafc;
      border-radius: 8px;
      border: 1px solid #ebeef5;

      .el-textarea {
        margin-bottom: 0.75rem;
      }

      .reply-actions {
        display: flex;
        justify-content: flex-end;
        gap: 0.5rem;
      }
    }

    .replies {
      margin-top: 1rem;
      padding-left: 1rem;
      border-left: 2px solid #e4e7ed;
      margin-left: 0.5rem;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .article-header-card,
  .article-body-card,
  .article-actions-card {
    padding: 1.5rem;
    border-radius: 12px;
  }

  .article-title {
    font-size: 1.5rem;
  }

  .article-meta {
    gap: 0.75rem;
    font-size: 0.85rem;
  }

  .article-body {
    font-size: 1rem;
  }

  .comments-section {
    padding: 1.5rem;

    .comment-form {
      padding: 1rem;
    }
  }
}
</style>