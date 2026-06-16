<script setup lang="ts">
defineProps<{
  description?: string
}>()
</script>

<template>
  <div class="empty-state">
    <div class="empty-icon">📭</div>
    <p class="empty-text">{{ description || '暂无数据' }}</p>
  </div>
</template>

<style scoped>
.empty-state {
  text-align: center;
  padding: var(--space-3xl) 0;
}
.empty-icon {
  font-size: 48px;
  margin-bottom: var(--space-md);
}
.empty-text {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}
</style>
