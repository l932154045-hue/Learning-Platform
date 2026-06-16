<script setup lang="ts">
const props = defineProps<{
  modelValue: number
  readonly?: boolean
  size?: 'small' | 'default' | 'large'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: number]
}>()

function setRating(star: number) {
  if (!props.readonly) {
    emit('update:modelValue', star)
  }
}

const starSize = {
  small: 16,
  default: 22,
  large: 28,
}
</script>

<template>
  <div class="rating-stars" :class="[`size-${size || 'default'}`, { readonly }]">
    <span
      v-for="star in 5"
      :key="star"
      :class="['star', { filled: star <= modelValue }]"
      @click="setRating(star)"
    >
      {{ star <= modelValue ? '★' : '☆' }}
    </span>
  </div>
</template>

<style scoped>
.rating-stars {
  display: inline-flex;
  gap: 2px;
}
.star {
  color: #ddd;
  transition: color 0.15s, transform 0.15s;
  user-select: none;
}
.star.filled {
  color: var(--color-accent);
}
.rating-stars:not(.readonly) .star {
  cursor: pointer;
}
.rating-stars:not(.readonly) .star:hover {
  transform: scale(1.15);
}
.size-small .star { font-size: 16px; }
.size-default .star { font-size: 22px; }
.size-large .star { font-size: 28px; }
</style>
