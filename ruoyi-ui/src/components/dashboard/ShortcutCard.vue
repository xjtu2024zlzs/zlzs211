<template>
  <el-card class="shortcut-card" shadow="hover" @click="emit('navigate', item)">
    <div class="shortcut-card__head">
      <div class="shortcut-card__icon">{{ item.icon }}</div>
      <el-tag size="small" effect="dark" :type="item.tagType || 'info'">{{ item.badge }}</el-tag>
    </div>
    <div class="shortcut-card__title">{{ item.title }}</div>
    <p class="shortcut-card__desc">{{ item.description }}</p>
    <div class="shortcut-card__footer">
      <span class="shortcut-card__meta">{{ item.meta }}</span>
      <span class="shortcut-card__arrow">→</span>
    </div>
  </el-card>
</template>

<script setup>
const props = defineProps({
  item: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['navigate'])
</script>

<style scoped lang="scss">
.shortcut-card {
  cursor: pointer;
  height: 100%;
  border-radius: 18px;
  border: 1px solid rgba(87, 137, 198, 0.34);
  background:
    linear-gradient(180deg, #ffffff, #f4f8fd),
    rgba(245, 249, 255, 0.98);
  box-shadow: 0 14px 32px rgba(74, 109, 152, 0.16);
  transition: transform 0.24s ease, border-color 0.24s ease, box-shadow 0.24s ease;

  &:hover {
    transform: translateY(-3px);
    border-color: rgba(36, 124, 214, 0.68);
    box-shadow: 0 18px 36px rgba(56, 95, 139, 0.22);
  }

  :deep(.el-card__body) {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-height: 140px;
  }
}

.shortcut-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.shortcut-card__icon {
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: linear-gradient(180deg, #2c90e8, #0a5ca2);
  color: #fff;
  font-size: 15px;
  box-shadow: 0 0 0 4px rgba(36, 129, 216, 0.16);
}

.shortcut-card__title {
  color: #112840;
  font-size: 16px;
  font-weight: 700;
}

.shortcut-card__desc {
  margin: 0;
  color: #6c7891;
  font-size: 12px;
  line-height: 1.5;
}

.shortcut-card__footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #2771b8;
  font-size: 12px;
  font-weight: 600;
}

.shortcut-card__arrow {
  color: #1e6da9;
  font-size: 16px;
}
</style>
