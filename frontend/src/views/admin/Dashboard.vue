<template>
  <div class="admin-dashboard">
    <h1>管理后台</h1>
    <p>欢迎回来，{{ authStore.user?.username }}</p>

    <div class="stats-cards">
      <el-card>
        <template #header>文章管理</template>
        <router-link to="/admin/articles">
          <el-button type="primary">管理文章</el-button>
        </router-link>
      </el-card>

      <el-card>
        <template #header>用户管理</template>
        <router-link to="/admin/users">
          <el-button type="primary">管理用户</el-button>
        </router-link>
      </el-card>

      <el-card>
        <template #header>系统设置</template>
        <router-link to="/admin/settings">
          <el-button type="primary">系统设置</el-button>
        </router-link>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
</script>

<style scoped lang="scss">
.admin-dashboard {
  padding: 2rem;

  h1 {
    font-size: 1.75rem;
    margin-bottom: 0.5rem;
  }

  p {
    color: #666;
    margin-bottom: 2rem;
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1.5rem;
  }
}
</style>
