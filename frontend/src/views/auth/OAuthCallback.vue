<template>
  <div class="oauth-callback-page">
    <div class="loading-container" v-if="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>正在登录中...</p>
    </div>
    <div class="error-container" v-else>
      <el-result icon="error" title="登录失败" :sub-title="errorMessage">
        <template #extra>
          <el-button type="primary" @click="goToLogin">返回登录</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import type { User } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const errorMessage = ref('')

onMounted(() => {
  handleOAuthCallback()
})

const handleOAuthCallback = () => {
  const urlParams = new URLSearchParams(window.location.search)
  const accessToken = urlParams.get('accessToken')
  const userStr = urlParams.get('user')

  if (!accessToken || !userStr) {
    errorMessage.value = '缺少必要参数'
    loading.value = false
    return
  }

  try {
    const user = JSON.parse(decodeURIComponent(userStr)) as User
    authStore.setAuth(accessToken, user)
    router.push('/')
  } catch (e) {
    console.error('解析用户信息失败:', e)
    errorMessage.value = '用户信息解析失败'
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped lang="scss">
.oauth-callback-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .loading-container {
    text-align: center;
    color: #fff;

    .el-icon {
      font-size: 48px;
      margin-bottom: 1rem;
    }

    p {
      font-size: 18px;
    }
  }

  .error-container {
    background: #fff;
    border-radius: 12px;
    padding: 2rem;
  }
}
</style>
