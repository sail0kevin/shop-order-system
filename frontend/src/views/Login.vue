<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="text-center mb-4">登录</h2>
      <el-form :model="form" @submit.prevent="handleLogin" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" class="w-100" :loading="loading">登录</el-button>
        </el-form-item>
      </el-form>
      <p class="text-center">没有账号？<router-link to="/register">立即注册</router-link></p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  loading.value = true
  try {
    await auth.login(form)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/products')
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; min-height: calc(100vh - 60px); background: #f5f7fa; }
.login-card { width: 400px; }
.w-100 { width: 100%; }
</style>
