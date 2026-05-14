<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 class="text-center mb-4">注册</h2>
      <el-form :model="form" @submit.prevent="handleRegister" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="3-20个字符" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="6-32个字符" />
        </el-form-item>
        <el-form-item label="昵称（选填）">
          <el-input v-model="form.nickname" placeholder="显示名称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" class="w-100" :loading="loading">注册</el-button>
        </el-form-item>
      </el-form>
      <p class="text-center">已有账号？<router-link to="/login">去登录</router-link></p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: '', password: '', nickname: '' })

async function handleRegister() {
  loading.value = true
  try {
    await auth.register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally { loading.value = false }
}
</script>

<style scoped>
.register-container { display: flex; justify-content: center; align-items: center; min-height: calc(100vh - 60px); background: #f5f7fa; }
.register-card { width: 400px; }
.w-100 { width: 100%; }
</style>
