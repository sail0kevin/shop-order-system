<template>
  <div class="container" style="padding-top: 20px;">
    <h3 class="mb-4">我的订单</h3>

    <div v-if="!orders.length && !loading" class="flex-center py-5">
      <el-empty description="暂无订单">
        <el-button type="primary" @click="$router.push('/products')">去购物</el-button>
      </el-empty>
    </div>

    <div v-else v-loading="loading">
      <el-card v-for="o in orders" :key="o.id" class="mb-3">
        <div class="flex-between">
          <div>
            <strong>订单号：{{ o.orderNo }}</strong>
            <div class="text-muted small">{{ o.createTime }}</div>
          </div>
          <el-tag :type="statusTag(o.status)">{{ statusMap[o.status] || '未知' }}</el-tag>
        </div>
        <div class="flex-between mt-2">
          <h4 class="price">¥{{ o.totalAmount }}</h4>
          <el-button v-if="o.status === 0" type="primary" @click="pay(o.orderNo)">立即支付</el-button>
        </div>
      </el-card>

      <div class="flex-center my-4" v-if="totalPages > 1">
        <el-pagination background layout="prev,pager,next" :total="total" :page-size="size" v-model:current-page="page" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrderPage, payOrder as payOrderApi } from '@/api/order'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'

const cart = useCartStore()
const loading = ref(false)
const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const totalPages = ref(0)

const statusMap = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成' }
const statusTag = s => ({ 0: 'warning', 1: 'success', 2: 'primary', 3: 'info' })[s] || 'info'

async function loadData() {
  loading.value = true
  try {
    const data = await getOrderPage({ page: page.value, size: size.value })
    orders.value = data.records || []
    total.value = data.total || 0
    totalPages.value = data.pages || 1
  } finally { loading.value = false }
}

async function pay(orderNo) {
  try {
    await payOrderApi({ orderNo })
    ElMessage.success('支付成功')
    await cart.fetchCart()
    loadData()
  } catch (_) {}
}

onMounted(loadData)
</script>

<style scoped>
.price { color: #f56c6c; font-weight: 700; }
.flex-center { display: flex; justify-content: center; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
</style>
