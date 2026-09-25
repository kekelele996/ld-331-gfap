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
          <el-table :data="data.requests" height="280">
            <el-table-column prop="applicant" label="申请人" width="80" />
            <el-table-column prop="replacement" label="替班人" width="80" />
            <el-table-column prop="date" label="日期" width="110" />
            <el-table-column prop="reason" label="原因" min-width="130" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审批操作" width="160">
              <template #default="{ row }">
                <template v-if="isPending(row)">
                  <el-button
                    type="success"
                    size="small"
                    :loading="processingId === row.id"
                    :disabled="processingId !== null"
                    @click="handleDecision(row, true)"
                  >同意</el-button>
                  <el-button
                    type="danger"
                    size="small"
                    :loading="processingId === row.id"
                    :disabled="processingId !== null"
                    @click="handleDecision(row, false)"
                  >驳回</el-button>
                </template>
                <span v-else class="handled-text">已处理</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
        <el-card shadow="never">
          <template #header>出勤与工时统计</template>
          <el-table :data="data.stats" height="280">
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
import { APP_TITLE, REQUEST_STATUS_APPROVED, REQUEST_STATUS_PENDING, REQUEST_STATUS_REJECTED } from '../constants/app';
import type { DashboardData, ShiftRequest } from '../types/schedule';

const department = ref('急诊科');
const data = reactive<DashboardData>({ rules: [], schedule: [], conflicts: [], requests: [], stats: [] });
// 正在处理的申请 id：处理入口据此收起并禁用，防止主管重复点击或页面重试提交。
const processingId = ref<number | null>(null);

async function load() {
  Object.assign(data, await fetchDashboard(department.value));
}

function isPending(row: ShiftRequest) {
  return row.status === REQUEST_STATUS_PENDING;
}

function statusTagType(status: string) {
  if (status === REQUEST_STATUS_APPROVED) return 'success';
  if (status === REQUEST_STATUS_REJECTED) return 'danger';
  return 'warning';
}

async function handleDecision(row: ShiftRequest, approved: boolean) {
  processingId.value = row.id;
  try {
    const updated = await decideShiftRequest(row.id, approved);
    ElMessage.success(
      updated.status === REQUEST_STATUS_APPROVED
        ? '已同意，申请当天班次已互换'
        : '已驳回，原排班保持不变',
    );
    // 重新拉取：申请列表显示最新审批状态，当天排班表显示互换后的班次。
    await load();
  } catch (error: unknown) {
    const message =
      typeof error === 'object' && error !== null && 'response' in error
        ? ((error as { response?: { data?: { message?: string } } }).response?.data?.message ?? '审批提交失败，请重试')
        : '审批提交失败，请重试';
    ElMessage.error(message);
  } finally {
    processingId.value = null;
  }
}

onMounted(load);
</script>

<style scoped>
.handled-text {
  color: #909399;
  font-size: 13px;
}
</style>
