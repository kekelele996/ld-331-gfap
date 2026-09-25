<template>
  <el-container class="page">
    <el-header class="topbar">
      <h1>{{ APP_TITLE }}</h1>
      <el-select v-model="department" @change="load">
        <el-option label="急诊科" value="急诊科" />
        <el-option label="心内科" value="心内科" />
      </el-select>
    </el-header>
    <el-main class="main">
      <el-alert title="规则引擎已加载：连续工作上限、周末轮循、夜班后禁接白班、节假日优先级。" type="info" show-icon />
      <section class="grid">
        <el-card shadow="never">
          <template #header>排班规则</template>
          <el-tag v-for="rule in data.rules" :key="rule" class="tag">{{ rule }}</el-tag>
        </el-card>
        <el-card shadow="never">
          <template #header>冲突检测</template>
          <el-timeline>
            <el-timeline-item v-for="alert in data.conflicts" :key="`${alert.staffName}-${alert.date}`" :type="alert.level === 'danger' ? 'danger' : 'warning'">
              {{ alert.date }} {{ alert.staffName }}：{{ alert.message }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </section>

      <el-card shadow="never">
        <template #header>
          <div class="card-head">
            <span>可视化排班表</span>
            <el-button type="primary">一键生成</el-button>
          </div>
        </template>
        <ScheduleBoard :items="data.schedule" />
      </el-card>

      <section class="grid">
        <el-card shadow="never">
          <template #header>调班与替班申请</template>
          <el-table :data="data.requests" height="240">
            <el-table-column prop="applicant" label="申请人" />
            <el-table-column prop="replacement" label="替班人" />
            <el-table-column prop="date" label="日期" />
            <el-table-column prop="status" label="状态" />
          </el-table>
        </el-card>
        <el-card shadow="never">
          <template #header>出勤与工时统计</template>
          <el-table :data="data.stats" height="240">
            <el-table-column prop="staffName" label="人员" />
            <el-table-column prop="dayShift" label="白班" />
            <el-table-column prop="nightShift" label="夜班" />
            <el-table-column prop="overtimeHours" label="加班小时" />
          </el-table>
        </el-card>
      </section>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { fetchDashboard } from '../api/schedule';
import ScheduleBoard from '../components/ScheduleBoard.vue';
import { APP_TITLE } from '../constants/app';
import type { DashboardData } from '../types/schedule';

const department = ref('急诊科');
const data = reactive<DashboardData>({ rules: [], schedule: [], conflicts: [], requests: [], stats: [] });

async function load() {
  Object.assign(data, await fetchDashboard(department.value));
}

onMounted(load);
</script>
