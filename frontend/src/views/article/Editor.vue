<template>
  <div class="article-editor">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item label=" Slug " prop="slug">
        <el-input v-model="form.slug" placeholder="自定义 URL 路径（可选）" />
      </el-form-item>

      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="标签">
        <el-select
          v-model="form.tagIds"
          multiple
          placeholder="请选择标签（可多选）"
          style="width: 100%"
        >
          <el-option
            v-for="tag in tags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="摘要" prop="summary">
        <el-input
          v-model="form.summary"
          type="textarea"
          :rows="3"
          placeholder="文章摘要，用于列表页展示"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="封面图" prop="coverImage">
        <ImageUploader v-model="form.coverImage" @change="handleCoverImageChange" />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="0">草稿</el-radio>
          <el-radio :label="1">已发布</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="内容" prop="content">
        <div class="editor-container">
          <div class="editor-toolbar">
            <el-button @click="insertMarkdown('# ')" title="一级标题"><strong>H1</strong></el-button>
            <el-button @click="insertMarkdown('## ')" title="二级标题"><strong>H2</strong></el-button>
            <el-button @click="insertMarkdown('### ')" title="三级标题"><strong>H3</strong></el-button>
            <el-divider direction="vertical" />
            <el-button @click="insertMarkdown('**', '**')" title="加粗"><strong>B</strong></el-button>
            <el-button @click="insertMarkdown('*', '*')" title="斜体"><em>I</em></el-button>
            <el-button @click="insertMarkdown('[', '](url)')" title="链接"><u>链接</u></el-button>
            <el-divider direction="vertical" />
            <el-button @click="insertMarkdown('- ')" title="列表">列表</el-button>
            <el-button @click="insertMarkdown('> ')" title="引用">引用</el-button>
            <el-button @click="insertMarkdown('```\n', '\n```')" title="代码块">代码</el-button>
            <el-divider direction="vertical" />
            <el-button type="info" @click="showPreview = !showPreview">
              {{ showPreview ? '编辑' : '预览' }}
            </el-button>
          </div>
          <div class="editor-content" :class="{ 'preview-mode': showPreview }">
            <div v-show="!showPreview" class="editor-wrapper">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="20"
                placeholder="支持 Markdown 语法"
                class="markdown-editor"
              />
            </div>
            <div v-show="showPreview" class="preview-wrapper" v-html="renderedContent"></div>
          </div>
        </div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEdit ? '保存修改' : '创建文章' }}
        </el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createArticleApi, updateArticleApi, getArticleApi, getCategoriesApi, getTagsApi } from '@/api'
import ImageUploader from '@/components/upload/ImageUploader.vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import type { Tag } from '@/types'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const loading = ref(false)
const showPreview = ref(false)
const categories = ref<any[]>([])
const tags = ref<Tag[]>([])

const form = reactive({
  title: '',
  slug: '',
  categoryId: undefined as number | undefined,
  tagIds: [] as number[],
  summary: '',
  coverImage: '',
  content: '',
  status: 0
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度 2-200 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' }
  ]
}

const isEdit = computed(() => !!route.params.id)

const renderedContent = computed(() => {
  // 简单的 Markdown 渲染（生产环境建议使用 marked 等库）
  return form.content
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    .replace(/\*\*(.*)\*\*/gim, '<strong>$1</strong>')
    .replace(/\*(.*)\*/gim, '<em>$1</em>')
    .replace(/\n/gim, '<br>')
})

const loadCategories = async () => {
  try {
    const res = await getCategoriesApi()
    categories.value = res.data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadTags = async () => {
  try {
    const res = await getTagsApi()
    tags.value = res.data
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const loadArticle = async () => {
  if (!route.params.id) return

  try {
    const res = await getArticleApi(Number(route.params.id))
    const article = res.data
    form.title = article.title
    form.slug = article.slug || ''
    form.categoryId = article.categoryId
    form.tagIds = article.tagIds || []
    form.summary = article.summary || ''
    form.coverImage = article.coverImage || ''
    form.content = article.content
    form.status = article.status
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败')
  }
}

const handleCoverImageChange = (url: string | string[]) => {
  form.coverImage = typeof url === 'string' ? url : ''
}

const insertMarkdown = (prefix: string, suffix = '') => {
  const textarea = document.querySelector('.markdown-editor textarea') as HTMLTextAreaElement
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = form.content.substring(start, end)

  const before = form.content.substring(0, start)
  const after = form.content.substring(end)

  form.content = before + prefix + selectedText + suffix + after

  // 恢复光标位置
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + prefix.length, end + prefix.length)
  }, 0)
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      if (isEdit.value) {
        await updateArticleApi(Number(route.params.id), form)
        ElMessage.success('文章更新成功')
      } else {
        await createArticleApi(form)
        ElMessage.success('文章创建成功')
      }
      router.push('/')
    } catch (error) {
      console.error('保存文章失败:', error)
      ElMessage.error('保存文章失败')
    } finally {
      loading.value = false
    }
  })
}

const handleCancel = () => {
  router.back()
}

onMounted(() => {
  loadCategories()
  loadTags()
  loadArticle()
})
</script>

<style scoped lang="scss">
.article-editor {
  background: #fff;
  border-radius: 8px;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;

  .editor-container {
    width: 100%;

    .editor-toolbar {
      margin-bottom: 0.5rem;
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
      padding: 0.75rem;
      background: #f5f7fa;
      border-radius: 4px 4px 0 0;
      border: 1px solid #e4e7ed;
      border-bottom: none;
    }

    .editor-content {
      display: flex;
      min-height: 400px;
      position: relative;

      &.preview-mode {
        .editor-wrapper,
        .preview-wrapper {
          width: 100%;
        }
      }

      .editor-wrapper,
      .preview-wrapper {
        border: 1px solid #dcdfe6;
        border-radius: 0 0 4px 4px;
        padding: 1rem;
        min-height: 400px;
        flex: 1;
        width: 100%;
      }

      .editor-wrapper {
        .markdown-editor {
          width: 100%;
          :deep(textarea) {
            font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
            font-size: 14px;
            line-height: 1.6;
            min-height: 400px;
            resize: vertical;
            border: none;
          }
        }
      }

      .preview-wrapper {
        background: #fafafa;
        font-size: 14px;
        line-height: 1.8;
        overflow-y: auto;

        :deep(h1), :deep(h2), :deep(h3) {
          margin-top: 1.5rem;
          margin-bottom: 1rem;
          color: #333;
        }

        :deep(h1) { font-size: 1.5rem; }
        :deep(h2) { font-size: 1.25rem; }
        :deep(h3) { font-size: 1rem; }

        :deep(strong) { font-weight: bold; }
        :deep(em) { font-style: italic; }
        :deep(code) {
          background: #f0f0f0;
          padding: 2px 6px;
          border-radius: 3px;
          font-family: monospace;
        }
        :deep(pre) {
          background: #2d2d2d;
          color: #f8f8f2;
          padding: 1rem;
          border-radius: 4px;
          overflow-x: auto;
          margin: 1rem 0;
          code {
            background: transparent;
            padding: 0;
          }
        }
        :deep(blockquote) {
          border-left: 4px solid #409eff;
          padding-left: 1rem;
          margin: 1rem 0;
          color: #666;
        }
        :deep(ul), :deep(ol) {
          padding-left: 1.5rem;
          margin: 0.5rem 0;
        }
      }
    }
  }
}
</style>
