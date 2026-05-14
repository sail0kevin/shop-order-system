<template>
  <div class="skeleton-grid">
    <div v-for="n in count" :key="n" class="skeleton-card">
      <div class="skeleton-img" />
      <div class="skeleton-line w-75" />
      <div class="skeleton-line w-50" />
    </div>
  </div>
</template>

<script setup>
defineProps({ count: { type: Number, default: 8 } })
</script>

<style scoped>
.skeleton-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.skeleton-card { border: 1px solid #eee; border-radius: 8px; padding: 16px; }
.skeleton-img { height: 180px; background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: 4px; margin-bottom: 12px; }
.skeleton-line { height: 14px; background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: 4px; margin-bottom: 8px; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
</style>
