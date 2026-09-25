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
            <el-table-column label="状态">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <template v-if="row.status === REQUEST_STATUS.PENDING">
                  <el-button
                    type="success"
                    size="small"
                    :loading="decidingId === row.id"
                    :disabled="decidingId !== null"
                    @click="decide(row.id, 'approve')"
                  >同意</el-button>
                  <el-button
                    type="danger"
                    size="small"
                    :loading="decidingId === row.id"
                    :disabled="decidingId !== null"
                    @click="decide(row.id, 'reject')"
                  >驳回</el-button>
                </template>
                <span v-else class="decided-hint">已处理</span>
              </template>
            </el-table-column>
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
import { ElMessage } from 'element-plus';
import { decideShiftRequest, fetchDashboard } from '../api/schedule';
import ScheduleBoard from '../components/ScheduleBoard.vue';
import { APP_TITLE } from '../constants/app';
import type { DashboardData } from '../types/schedule';

const REQUEST_STATUS = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' } as const;

const department = ref('急诊科');
const data = reactive<DashboardData>({ rules: [], schedule: [], conflicts: [], requests: [], stats: [] });
const decidingId = ref<number | null>(null);

async function load() {
  Object.assign(data, await fetchDashboard(department.value));
}

function statusTagType(status: string) {
  if (status === REQUEST_STATUS.APPROVED) return 'success';
  if (status === REQUEST_STATUS.REJECTED) return 'danger';
  return 'warning';
}

async function decide(id: number, action: 'approve' | 'reject') {
  if (decidingId.value !== null) return;
  decidingId.value = id;
  try {
    const decided = await decideShiftRequest(id, action);
    ElMessage.success(`申请已处理：${decided.status}`);
    // 重新拉取，保证申请列表与当天排班表都展示最新状态
    await load();
  } catch {
    ElMessage.error('处理失败，请稍后重试');
  } finally {
    decidingId.value = null;
  }
}

onMounted(load);
</script>
