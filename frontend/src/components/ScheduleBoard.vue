<template>
  <div class="schedule-board">
    <div v-for="item in items" :key="`${item.date}-${item.staffName}`" class="shift-card" :style="{ borderColor: item.color }">
      <div class="date">{{ item.date }}</div>
      <strong>{{ item.staffName }}</strong>
      <span>{{ item.position }} · {{ item.shift }}</span>
      <el-tag v-if="item.holiday" type="danger" size="small">特殊日期</el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ScheduleItem } from '../types/schedule';

defineProps<{ items: ScheduleItem[] }>();
</script>
