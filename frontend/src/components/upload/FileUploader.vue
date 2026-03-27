<template>
  <div class="file-uploader">
    <div class="upload-area" @click="triggerUpload" @dragover.prevent @drop.prevent="handleDrop">
      <input
        ref="fileInput"
        type="file"
        class="file-input"
        :accept="accept"
        @change="handleFileChange"
      />
      <div class="upload-placeholder">
        <el-icon class="upload-icon"><Upload /></el-icon>
        <p class="upload-text">点击或拖拽文件到此处上传</p>
        <p class="upload-hint" v-if="hint">{{ hint }}</p>
      </div>
    </div>

    <!-- 上传列表 -->
    <div v-if="fileList.length > 0" class="file-list">
      <div v-for="(file, index) in fileList" :key="index" class="file-item">
        <div class="file-info">
          <el-icon class="file-icon"><Document /></el-icon>
          <span class="file-name">{{ file.file.name }}</span>
          <span class="file-size">{{ formatFileSize(file.file.size) }}</span>
        </div>
        <div class="file-status">
          <span v-if="file.status === 'uploading'" class="status-text">
            上传中 {{ file.progress }}%
          </span>
          <span v-else-if="file.status === 'success'" class="status-success">
            <el-icon><Check /></el-icon>
          </span>
          <span v-else-if="file.status === 'error'" class="status-error">
            上传失败
          </span>
          <el-button type="danger" size="small" @click="removeFile(index)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getPresignedUrlApi, confirmUploadApi } from '@/api'
import type { UploadResponse, PresignedUrlResponse } from '@/types'

interface FileItem {
  file: File
  status: 'pending' | 'uploading' | 'success' | 'error'
  progress: number
  url?: string
}

const props = defineProps({
  accept: {
    type: String,
    default: '*/*'
  },
  hint: {
    type: String,
    default: '支持图片、文档等文件类型'
  },
  multiple: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits<{
  change: [files: Array<{ name: string; url: string; size: number }>]
}>()

const fileInput = ref<HTMLInputElement | null>(null)
const fileList = ref<FileItem[]>([])

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (files) {
    handleFiles(files)
  }
}

const handleDrop = (event: DragEvent) => {
  const files = event.dataTransfer?.files
  if (files) {
    handleFiles(files)
  }
}

const handleFiles = (files: FileList) => {
  const newFiles: FileItem[] = Array.from(files).map(file => ({
    file,
    status: 'pending' as const,
    progress: 0
  }))

  if (props.multiple) {
    fileList.value.push(...newFiles)
  } else {
    fileList.value = newFiles
  }

  // 开始上传
  newFiles.forEach(uploadFile)
}

const uploadFile = async (fileItem: FileItem) => {
  const { file } = fileItem
  fileItem.status = 'uploading'

  try {
    // 1. 获取预签名 URL
    const presignedUrlRes = await getPresignedUrlApi(file.name, file.size, file.type, 'uploads')
    const presignedData = presignedUrlRes.data as PresignedUrlResponse
    const presignedUrl = presignedData.presignedUrl
    const objectName = presignedData.objectName

    // 2. 上传文件到 MinIO
    const uploadResponse = await fetch(presignedUrl, {
      method: 'PUT',
      body: file,
      headers: {
        'Content-Type': file.type
      }
    })

    if (!uploadResponse.ok) {
      throw new Error('上传失败')
    }

    // 3. 确认上传完成
    const confirmResponse = await confirmUploadApi(file.name, file.size, file.type, objectName)
    const uploadData = confirmResponse.data as UploadResponse

    fileItem.status = 'success'
    fileItem.url = uploadData.fileUrl
    fileItem.progress = 100

    emitChange()
  } catch (error) {
    console.error('上传失败:', error)
    fileItem.status = 'error'
  }
}

const removeFile = (index: number) => {
  fileList.value.splice(index, 1)
  emitChange()
}

const emitChange = () => {
  const uploadedFiles = fileList.value
    .filter(f => f.status === 'success')
    .map(f => ({
      name: f.file.name,
      url: f.url!,
      size: f.file.size
    }))
  emit('change', uploadedFiles)
}

const formatFileSize = (bytes: number): string => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}
</script>

<style scoped lang="scss">
.file-uploader {
  .upload-area {
    border: 2px dashed #dcdfe6;
    border-radius: 8px;
    padding: 2rem;
    text-align: center;
    cursor: pointer;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }

    .file-input {
      display: none;
    }

    .upload-placeholder {
      .upload-icon {
        font-size: 48px;
        color: #409eff;
        margin-bottom: 1rem;
      }

      .upload-text {
        color: #606266;
        margin-bottom: 0.5rem;
      }

      .upload-hint {
        color: #909399;
        font-size: 0.875rem;
      }
    }
  }

  .file-list {
    margin-top: 1rem;

    .file-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem;
      background: #f5f7fa;
      border-radius: 4px;
      margin-bottom: 0.5rem;

      .file-info {
        display: flex;
        align-items: center;
        gap: 0.5rem;

        .file-icon {
          color: #909399;
        }

        .file-name {
          color: #606266;
          flex: 1;
        }

        .file-size {
          color: #909399;
          font-size: 0.875rem;
        }
      }

      .file-status {
        display: flex;
        align-items: center;
        gap: 0.5rem;

        .status-text {
          color: #409eff;
          font-size: 0.875rem;
        }

        .status-success {
          color: #67c23a;
        }

        .status-error {
          color: #f56c6c;
        }
      }
    }
  }
}
</style>
