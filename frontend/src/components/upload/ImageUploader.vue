<template>
  <div class="image-uploader">
    <div class="upload-list">
      <!-- 上传按钮 -->
      <div v-if="!imageSrc || multiple" class="upload-item" @click="triggerUpload">
        <input
          ref="fileInput"
          type="file"
          class="file-input"
          accept="image/*"
          @change="handleFileChange"
        />
        <div class="upload-placeholder">
          <el-icon class="upload-icon"><Plus /></el-icon>
        </div>
      </div>

      <!-- 已上传的图片预览 -->
      <div v-if="imageSrc && !multiple" class="upload-item image-preview">
        <img :src="imageSrc" alt="预览" class="preview-image" />
        <div class="image-actions">
          <el-button type="danger" size="small" @click="removeImage">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import { uploadFileApi } from '@/api'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  multiple: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits<{
  'update:modelValue': [url: string]
  change: [url: string | string[]]
}>()

const fileInput = ref<HTMLInputElement | null>(null)
const imageSrc = ref<string>(props.modelValue || '')

// 监听外部传入的值变化
watch(() => props.modelValue, (newVal) => {
  imageSrc.value = newVal || ''
})


const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  try {
    const result = await uploadFileApi(file, 'images')
    imageSrc.value = result.data.fileUrl
    emit('update:modelValue', result.data.fileUrl)
    emit('change', result.data.fileUrl)
  } catch (error) {
    console.error('上传失败:', error)
  }

  // 清空 input 以便重新上传同一文件
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const removeImage = () => {
  imageSrc.value = ''
  emit('update:modelValue', '')
  emit('change', '')
}
</script>

<style scoped lang="scss">
.image-uploader {
  .upload-list {
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
  }

  .upload-item {
    width: 150px;
    height: 150px;
    border: 2px dashed #dcdfe6;
    border-radius: 8px;
    cursor: pointer;
    transition: border-color 0.3s;
    position: relative;

    &:hover {
      border-color: #409eff;
    }

    .file-input {
      display: none;
    }

    .upload-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;

      .upload-icon {
        font-size: 32px;
        color: #8c939d;
      }
    }

    &.image-preview {
      border: 2px solid #eee;

      .preview-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 8px;
      }

      .image-actions {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s;
        border-radius: 8px;

        &:hover {
          opacity: 1;
        }
      }
    }
  }
}
</style>
