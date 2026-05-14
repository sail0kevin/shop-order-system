<template>
  <div class="container" style="padding-top: 20px;">
    <h3 class="mb-4">全部商品</h3>

    <el-row :gutter="16" class="mb-4">
      <el-col :span="8">
        <el-input v-model="keyword" placeholder="搜索商品名称..." clearable @keyup.enter="search" @clear="search" />
      </el-col>
      <el-col :span="5">
        <el-select v-model="category" placeholder="全部分类" clearable @change="search" class="w-100">
          <el-option label="电子产品" value="电子产品" />
          <el-option label="服装" value="服装" />
          <el-option label="食品" value="食品" />
        </el-select>
      </el-col>
      <el-col :span="3">
        <el-button type="primary" @click="search">搜索</el-button>
      </el-col>
    </el-row>

    <Skeleton v-if="loading" />
    <template v-else>
      <el-row :gutter="20" v-if="products.length">
        <el-col :span="6" v-for="p in products" :key="p.id" class="mb-4">
          <el-card :body-style="{ padding: '0' }" shadow="hover" class="product-card" @click="$router.push('/product/' + p.id)">
            <el-image :src="p.image || placeholderImg" class="product-img" fit="contain" lazy>
              <template #placeholder><div class="img-placeholder" /></template>
            </el-image>
            <div class="p-3">
              <el-tag size="small" effect="plain">{{ p.category }}</el-tag>
              <h6 class="mt-2 mb-1 text-ellipsis">{{ p.name }}</h6>
              <div class="d-flex justify-content-between align-items-center">
                <span class="price">¥{{ p.price }}</span>
                <small class="text-muted">库存 {{ p.stock }}</small>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="没有找到商品" />

      <div class="flex-center my-4" v-if="totalPages > 1">
        <el-pagination background layout="prev,pager,next" :total="total" :page-size="size" v-model:current-page="page" @current-change="loadData" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProductPage } from '@/api/product'
import Skeleton from '@/components/Skeleton.vue'

const placeholderImg = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 300 200"><rect fill="%23f0f0f0" width="300" height="200"/><text x="50%25" y="50%25" fill="%23ccc" text-anchor="middle" dy=".3em" font-size="14">暂无图片</text></svg>'

const loading = ref(false)
const products = ref([])
const page = ref(1)
const size = ref(8)
const total = ref(0)
const totalPages = ref(0)
const keyword = ref('')
const category = ref('')

function search() { page.value = 1; loadData() }

async function loadData() {
  loading.value = true
  try {
    const data = await getProductPage({ page: page.value, size: size.value, keyword: keyword.value || undefined, category: category.value || undefined })
    products.value = data.records || []
    total.value = data.total || 0
    totalPages.value = data.pages || 1
  } finally { loading.value = false }
}

onMounted(loadData)
</script>

<style scoped>
.product-card { cursor: pointer; transition: transform .15s; border-radius: 8px; overflow: hidden; }
.product-card:hover { transform: translateY(-3px); }
.product-img { height: 200px; background: #f8f9fa; }
.img-placeholder { height: 200px; background: #f0f0f0; }
.price { color: #f56c6c; font-weight: 700; font-size: 1.1rem; }
.text-ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.flex-center { display: flex; justify-content: center; }
.w-100 { width: 100%; }
</style>
