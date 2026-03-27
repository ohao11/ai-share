<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <h1 class="title">用户登录</h1>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">
              登录
            </el-button>
          </el-form-item>

          <div class="oauth-section">
            <div class="divider">
              <span>或使用以下方式登录</span>
            </div>

            <div class="oauth-buttons">
              <el-button @click="handleOAuthLogin('google')">
                <svg class="icon" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                Google
              </el-button>

              <el-button @click="handleOAuthLogin('github')">
                <svg class="icon" viewBox="0 0 24 24">
                  <path fill="#333" d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                </svg>
                GitHub
              </el-button>
            </div>
          </div>

          <div class="links">
            <router-link to="/register">还没有账号？立即注册</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { loginApi } from '@/api'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  email: '',
  password: ''
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ]
}

// 处理 URL 中的错误参数
onMounted(() => {
  const error = route.query.error as string
  if (error) {
    ElMessage.error(decodeURIComponent(error))
  }
})

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await loginApi(form.email, form.password)
      authStore.setAuth(res.data.accessToken, res.data.user)
      router.push('/')
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}

const handleOAuthLogin = (provider: string) => {
  // OAuth2 登录由后端处理，直接跳转到后端授权 URL
  window.location.href = `/oauth2/authorization/${provider}`
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8ecf1 100%);
}

.login-container {
  width: 100%;
  max-width: 420px;
  padding: 2rem;
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 2.5rem;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.12);

  .title {
    text-align: center;
    font-size: 1.75rem;
    font-weight: 600;
    color: #2d3748;
    margin-bottom: 2rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  :deep(.el-form-item) {
    margin-bottom: 1.5rem;

    .el-form-item__label {
      font-weight: 500;
      color: #4a5568;
      font-size: 0.9rem;
    }
  }

  :deep(.el-input__wrapper) {
    border: 1px solid #d1d5db;
    border-radius: 8px;
    padding: 0.75rem 1rem;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
    transition: all 0.2s;
    background: #fff;

    &:hover {
      border-color: #9ca3af;
      box-shadow: 0 2px 6px rgba(102, 126, 234, 0.1);
    }

    &.is-focus {
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
    }
  }

  :deep(.el-button--primary) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 8px;
    padding: 0.75rem;
    font-weight: 500;
    font-size: 1rem;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
    transition: all 0.3s;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

.oauth-section {
  margin-top: 1.5rem;

  .divider {
    text-align: center;
    position: relative;
    margin: 1.5rem 0;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      width: 100%;
      height: 1px;
      background: linear-gradient(to right, transparent, #e2e8f0, transparent);
    }

    span {
      position: relative;
      background: #fff;
      padding: 0 1rem;
      color: #94a3b8;
      font-size: 0.875rem;
    }
  }

  .oauth-buttons {
    display: flex;
    gap: 1rem;

    .el-button {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      border-radius: 8px;
      padding: 0.625rem 1rem;
      border: 1px solid #e2e8f0;
      background: #fff;
      color: #4a5568;
      font-weight: 500;
      transition: all 0.2s;

      &:hover {
        border-color: #667eea;
        color: #667eea;
        background: rgba(102, 126, 234, 0.05);
        transform: translateY(-1px);
      }

      .icon {
        width: 20px;
        height: 20px;
      }
    }
  }
}

.links {
  text-align: center;
  margin-top: 1.25rem;

  a {
    color: #667eea;
    text-decoration: none;
    font-size: 0.875rem;
    font-weight: 500;
    transition: all 0.2s;

    &:hover {
      color: #764ba2;
      text-decoration: underline;
    }
  }
}
</style>
