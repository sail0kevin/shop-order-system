<template>
  <div class="container" style="padding-top: 20px;">
    <h3 class="mb-4">购物车</h3>

    <div v-if="cart.isEmpty && !cart.loading" class="flex-center py-5">
      <el-empty description="购物车是空的">
        <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
      </el-empty>
    </div>

    <div v-else v-loading="cart.loading">
      <el-table :data="cart.items" style="width:100%">
        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <div class="d-flex align-items-center gap-2">
              <el-image :src="row.image || placeholderImg" style="width:60px;height:60px" fit="contain" />
              <div>
                <strong>{{ row.productName }}</strong>
                <div class="text-muted small">{{ row.category }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="数量" width="180">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" size="small" @change="val => cart.updateCart(row.productId, val)" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.subtotal }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-popconfirm title="确认删除？" @confirm="cart.removeCart(row.productId)">
              <template #reference>
                <el-button type="danger" size="small" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex-between mt-4">
        <h4>总计：<span class="price">¥{{ cart.totalAmount.toFixed(2) }}</span></h4>
        <el-button type="danger" size="large" @click="checkout">去结算</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { createOrder } from '@/api/order'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cart = useCartStore()
const placeholderImg = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 60 60"><rect fill="%23f0f0f0" width="60" height="60"/></svg>'

async function checkout() {
  try {
    await createOrder()
    ElMessage.success('下单成功')
    await cart.fetchCart()
    router.push('/orders')
  } catch (_) {}
}

onMounted(() => cart.fetchCart())
</script>

<style scoped>
.price { color: #f56c6c; font-weight: 700; }
.flex-center { display: flex; justify-content: center; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.gap-2 { gap: 10px; }
</style>
