<template>
  <el-menu mode="horizontal" :ellipsis="false" router>
    <el-menu-item index="/products">
      <el-icon><Goods /></el-icon>商品
    </el-menu-item>
    <el-menu-item index="/cart" v-if="auth.isLoggedIn">
      <el-icon><ShoppingCart /></el-icon>购物车
      <el-badge :value="cart.totalCount" :hidden="cart.isEmpty" class="ml-1" />
    </el-menu-item>
    <el-menu-item index="/orders" v-if="auth.isLoggedIn">
      <el-icon><List /></el-icon>订单
    </el-menu-item>

    <div class="flex-grow" />

    <template v-if="auth.isLoggedIn">
      <el-sub-menu index="user">
        <template #title><el-icon><User /></el-icon>{{ auth.nickname }}</template>
        <el-menu-item index="/login" @click="logout">退出登录</el-menu-item>
      </el-sub-menu>
    </template>
    <template v-else>
      <el-menu-item index="/login">登录</el-menu-item>
      <el-menu-item index="/register">注册</el-menu-item>
    </template>
  </el-menu>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const auth = useAuthStore()
const cart = useCartStore()

function logout() {
  auth.logout()
  cart.clearCart()
}

onMounted(() => {
  if (auth.isLoggedIn) cart.fetchCart()
})
</script>

<style scoped>
.flex-grow { flex-grow: 1 }
.ml-1 { margin-left: 4px; }
.el-menu { padding: 0 20px; }
</style>
