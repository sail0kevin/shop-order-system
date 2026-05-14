<template>
  <div class="container" style="padding-top: 20px;">
    <el-button text @click="$router.push('/products')">
      <el-icon><ArrowLeft /></el-icon>返回商品列表
    </el-button>

    <el-row :gutter="40" class="mt-3" v-loading="loading">
      <el-col :span="10">
        <el-image :src="product.image || placeholderImg" class="detail-img" fit="contain" lazy />
      </el-col>
      <el-col :span="14">
        <h3>{{ product.name }}</h3>
        <el-tag>{{ product.category }}</el-tag>
        <h2 class="price mt-3">¥{{ product.price }}</h2>
        <p class="text-muted mt-3">{{ product.description || '暂无描述' }}</p>
        <p>库存：<el-tag type="info">{{ product.stock }}</el-tag></p>

        <div class="d-flex align-items-center gap-3 mb-3">
          <span>数量：</span>
          <el-input-number v-model="qty" :min="1" :max="product.stock" size="large" />
        </div>
        <el-button type="danger" size="large" @click="addToCart" :disabled="!product.stock">
          <el-icon><ShoppingCart /></el-icon>加入购物车
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getProductDetail } from '@/api/product'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'

const route = useRoute()
const cartStore = useCartStore()
const placeholderImg = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300"><rect fill="%23f0f0f0" width="400" height="300"/><text x="50%25" y="50%25" fill="%23ccc" text-anchor="middle" dy=".3em" font-size="16">暂无图片</text></svg>'

const loading = ref(false)
const product = ref({})
const qty = ref(1)

async function addToCart() {
  try {
    await cartStore.addCart(product.value.id, qty.value)
    ElMessage.success('已加入购物车')
  } catch (_) {}
}

onMounted(async () => {
  loading.value = true
  try { product.value = await getProductDetail(route.params.id) }
  finally { loading.value = false }
})
</script>

<style scoped>
.detail-img { height: 350px; background: #f8f9fa; border-radius: 8px; }
.price { color: #f56c6c; font-weight: 700; }
.gap-3 { gap: 12px; }
</style>
