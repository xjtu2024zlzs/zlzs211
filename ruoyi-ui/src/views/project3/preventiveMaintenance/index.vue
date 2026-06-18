<template>
  <div class="app-container maintenance-page">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div>
          <h2>设备预防性维护优化</h2>
          <p>基于蒙特卡洛模拟与 EMBKA / BKA 多目标优化，平衡单位成本费率、设备可用度与可靠度约束。</p>
        </div>
        <el-tag type="primary" effect="plain" size="large">算法任务</el-tag>
      </div>
    </el-card>

    <el-row :gutter="16" class="main-row">
      <el-col :xs="24" :xl="15">
        <el-card class="panel-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <span class="card-title">算法输入</span>
                <span class="card-subtitle">维护策略参数与算法约束配置</span>
              </div>
              <div class="header-actions">
                <el-button :disabled="running" @click="resetParameters">重置参数</el-button>
                <el-button v-if="running" type="danger" plain @click="cancelTask">取消任务</el-button>
                <el-button type="primary" :loading="running" @click="runAlgorithm">运行算法</el-button>
              </div>
            </div>
          </template>

          <el-form :model="form" label-position="top" class="parameter-form">
            <section class="form-section">
              <div class="section-title">关联对象输入</div>
              <el-row :gutter="12">
                <el-col v-for="item in relationFields" :key="item.key" :xs="24" :sm="12" :lg="6">
                  <el-form-item :label="item.label">
                    <el-input v-model="form[item.key]" :placeholder="item.placeholder" />
                  </el-form-item>
                </el-col>
              </el-row>
            </section>

            <section class="form-section">
              <div class="section-title">决策变量输入</div>
              <el-alert
                title="该组决策变量作为 EMBKA 初始种群中的种子策略，必须满足 T1 ≥ T2 ≥ T3，N1、N2、N3 为正整数。"
                type="info"
                :closable="false"
                show-icon
                class="constraint-alert"
              />
              <el-row :gutter="12">
                <el-col v-for="key in intervalKeys" :key="key" :xs="12" :sm="8" :lg="4">
                  <el-form-item :label="`${key} 维护间隔`">
                    <el-input-number v-model="form[key]" :min="50" :max="100" :step="1" controls-position="right" />
                  </el-form-item>
                </el-col>
                <el-col v-for="key in countKeys" :key="key" :xs="12" :sm="8" :lg="4">
                  <el-form-item :label="`${key} 维护次数`">
                    <el-input-number v-model="form[key]" :min="1" :max="12" :step="1" :precision="0" controls-position="right" />
                  </el-form-item>
                </el-col>
              </el-row>
            </section>

            <section class="form-section">
              <div class="section-title">蒙特卡洛模拟输入</div>
              <el-row :gutter="12">
                <el-col v-for="item in monteCarloFields" :key="item.key" :xs="12" :sm="8" :lg="6">
                  <el-form-item :label="item.label">
                    <el-input-number
                      v-model="form[item.key]"
                      :min="item.min"
                      :step="item.step"
                      :precision="item.precision"
                      controls-position="right"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </section>

            <el-row :gutter="16">
              <el-col :xs="24" :lg="12">
                <section class="form-section compact-section">
                  <div class="section-title">成本参数输入</div>
                  <el-row :gutter="12">
                    <el-col v-for="item in costFields" :key="item.key" :span="12">
                      <el-form-item :label="item.label">
                        <el-input-number v-model="form[item.key]" :min="0" :step="item.step" controls-position="right" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </section>
              </el-col>
              <el-col :xs="24" :lg="12">
                <section class="form-section compact-section">
                  <div class="section-title">可靠度与退化模型输入</div>
                  <el-row :gutter="12">
                    <el-col v-for="item in reliabilityFields" :key="item.key" :span="12">
                      <el-form-item :label="item.label">
                        <el-input-number
                          v-model="form[item.key]"
                          :min="item.min"
                          :max="item.max"
                          :step="item.step"
                          :precision="item.precision"
                          controls-position="right"
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </section>
              </el-col>
            </el-row>

            <section class="form-section">
              <div class="section-title">BKA 优化参数输入</div>
              <el-row :gutter="12">
                <el-col v-for="item in bkaFields" :key="item.key" :xs="12" :sm="6">
                  <el-form-item :label="item.label">
                    <el-input-number
                      v-model="form[item.key]"
                      :min="item.min"
                      :max="item.max"
                      :step="item.step"
                      :precision="item.precision"
                      :disabled="item.readonly"
                      controls-position="right"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </section>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="9">
        <el-card class="panel-card output-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div>
                <span class="card-title">算法输出概览</span>
                <span class="card-subtitle">优化任务运行结果</span>
              </div>
              <el-tag :type="statusTagType" effect="light">{{ statusText }}</el-tag>
            </div>
          </template>

          <div v-if="taskState.taskId" class="task-progress">
            <div class="task-progress-head">
              <span>{{ taskState.message || '任务处理中' }}</span>
              <strong>{{ taskState.progress }}%</strong>
            </div>
            <el-progress :percentage="taskState.progress" :status="progressStatus" />
          </div>

          <el-row :gutter="12" class="metric-grid">
            <el-col v-for="metric in metrics" :key="metric.label" :span="12">
              <div class="metric-card" :class="metric.tone">
                <div class="metric-label">{{ metric.label }}</div>
                <div class="metric-value">{{ metric.value }}</div>
                <div class="metric-unit">{{ metric.unit }}</div>
              </div>
            </el-col>
          </el-row>

          <div class="result-section-title">推荐维护策略</div>
          <el-table :data="recommended ? [recommended] : []" border size="small" empty-text="暂无算法结果">
            <el-table-column v-for="key in [...intervalKeys, ...countKeys]" :key="key" :prop="key" :label="key" width="58" align="center" />
            <el-table-column prop="totalCount" label="总次数" min-width="76" align="center" />
          </el-table>
          <el-table :data="recommended ? [recommended] : []" border size="small" class="second-table" empty-text="暂无算法结果">
            <el-table-column label="C_u" min-width="96" align="center">
              <template #default="{ row }">{{ formatCost(row.Cu) }}</template>
            </el-table-column>
            <el-table-column label="A" min-width="80" align="center">
              <template #default="{ row }">{{ formatRate(row.A) }}</template>
            </el-table-column>
            <el-table-column label="minR" min-width="80" align="center">
              <template #default="{ row }">{{ formatRate(row.minR) }}</template>
            </el-table-column>
            <el-table-column label="可靠度约束" min-width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.meetsReliability ? 'success' : 'danger'" size="small">
                  {{ row.meetsReliability ? '满足' : '不满足' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <div class="run-summary">
            <div><span>算法任务编号</span><strong>{{ form.taskNo || '-' }}</strong></div>
            <div><span>运行任务编号</span><strong>{{ taskState.taskId || '-' }}</strong></div>
            <div><span>模拟样本数</span><strong>{{ samples.tm.length || '-' }}</strong></div>
            <div><span>优化算法</span><strong>EMBKA / BKA</strong></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <span class="card-title">蒙特卡洛模拟过程</span>
            <span class="card-subtitle">维修时间样本与联合分布</span>
          </div>
          <el-tag :type="samples.tm.length ? 'primary' : 'info'" effect="plain">
            {{ samples.tm.length ? `${samples.tm.length} 次采样` : '暂无数据' }}
          </el-tag>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :xs="24" :lg="14">
          <div class="chart-panel">
            <div class="chart-title">维修时间样本折线图</div>
            <div v-show="samples.tm.length" ref="sampleLineRef" class="chart"></div>
            <el-empty v-if="!samples.tm.length" description="暂无蒙特卡洛样本" />
          </div>
        </el-col>
        <el-col :xs="24" :lg="10">
          <div class="chart-panel">
            <div class="chart-title">蒙特卡洛样本散点图</div>
            <div v-show="samples.tm.length" ref="sampleScatterRef" class="chart"></div>
            <el-empty v-if="!samples.tm.length" description="暂无蒙特卡洛样本" />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="panel-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <span class="card-title">多目标优化结果</span>
            <span class="card-subtitle">Pareto 非支配解与推荐策略序列</span>
          </div>
          <el-tag :type="paretoResults.length ? 'success' : 'info'" effect="plain">
            {{ paretoResults.length ? `${paretoResults.length} 个非支配解` : '暂无数据' }}
          </el-tag>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-title">Pareto 非支配解散点图</div>
            <div v-show="paretoResults.length" ref="paretoRef" class="chart"></div>
            <el-empty v-if="!paretoResults.length" description="暂无 Pareto 优化结果" />
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="chart-panel">
            <div class="chart-title">推荐维护间隔序列</div>
            <div v-show="intervalSequence.length" ref="intervalRef" class="chart"></div>
            <el-empty v-if="!intervalSequence.length" description="暂无推荐维护策略" />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="panel-card table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <span class="card-title">Pareto 非支配解集</span>
            <span class="card-subtitle">候选维护策略与推荐结果</span>
          </div>
        </div>
      </template>
      <el-table :data="paretoResults" border stripe size="small" empty-text="暂无 Pareto 非支配解">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column v-for="key in [...intervalKeys, ...countKeys]" :key="key" :prop="key" :label="key" min-width="62" align="center" />
        <el-table-column label="单位成本费率 C_u" min-width="140" align="center">
          <template #default="{ row }">{{ formatCost(row.Cu) }}</template>
        </el-table-column>
        <el-table-column label="可用度 A" min-width="110" align="center">
          <template #default="{ row }">{{ formatRate(row.A) }}</template>
        </el-table-column>
        <el-table-column label="最小可靠度 minR" min-width="130" align="center">
          <template #default="{ row }">{{ formatRate(row.minR) }}</template>
        </el-table-column>
        <el-table-column label="解状态" min-width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-tag :type="row.status === 'RECOMMENDED' ? 'success' : 'info'" size="small">
              {{ row.status === 'RECOMMENDED' ? '推荐解' : '候选解' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="panel-card history-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <span class="card-title">算法执行历史</span>
            <span class="card-subtitle">查看已提交的预防性维护优化任务</span>
          </div>
          <el-button :loading="historyLoading" @click="loadHistory">刷新</el-button>
        </div>
      </template>

      <div class="history-toolbar">
        <el-input
          v-model="historyQuery.keyword"
          clearable
          placeholder="搜索任务编号、设备、零件或工序"
          @keyup.enter="searchHistory"
        />
        <el-select v-model="historyQuery.status" clearable placeholder="运行状态">
          <el-option label="等待执行" value="PENDING" />
          <el-option label="运行中" value="RUNNING" />
          <el-option label="运行成功" value="SUCCESS" />
          <el-option label="运行失败" value="FAILED" />
          <el-option label="已取消" value="CANCELED" />
        </el-select>
        <el-button type="primary" @click="searchHistory">查询</el-button>
        <el-button @click="resetHistoryQuery">重置</el-button>
      </div>

      <el-table
        v-loading="historyLoading"
        :data="historyRows"
        border
        stripe
        size="small"
        empty-text="暂无算法执行历史"
      >
        <el-table-column prop="taskNo" label="算法任务编号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="taskId" label="运行任务编号" min-width="190" show-overflow-tooltip />
        <el-table-column prop="equipmentId" label="设备编号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="partInstanceId" label="零件实例编号" min-width="130" show-overflow-tooltip />
        <el-table-column prop="processExecutionId" label="工序实例编号" min-width="130" show-overflow-tooltip />
        <el-table-column label="运行状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="taskStatusTagType(row.status)" size="small">{{ taskStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="单位成本费率 C_u" min-width="130" align="center">
          <template #default="{ row }">{{ row.Cu == null ? '-' : formatCost(row.Cu) }}</template>
        </el-table-column>
        <el-table-column label="可用度 A" width="100" align="center">
          <template #default="{ row }">{{ row.A == null ? '-' : formatRate(row.A) }}</template>
        </el-table-column>
        <el-table-column label="最小可靠度" width="110" align="center">
          <template #default="{ row }">{{ row.minR == null ? '-' : formatRate(row.minR) }}</template>
        </el-table-column>
        <el-table-column prop="paretoCount" label="非支配解数" width="100" align="center" />
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="完成时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.finishedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :disabled="row.status !== 'SUCCESS'"
              :loading="historyDetailTaskId === row.taskId"
              @click="viewHistoryResult(row)"
            >
              查看结果
            </el-button>
            <el-button
              type="danger"
              link
              :disabled="row.status === 'PENDING' || row.status === 'RUNNING'"
              :loading="deletingTaskId === row.taskId"
              @click="deleteHistory(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="historyTotal > 0"
        :total="historyTotal"
        v-model:page="historyQuery.pageNum"
        v-model:limit="historyQuery.pageSize"
        @pagination="loadHistory"
      />
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  cancelPreventiveMaintenanceTask,
  deletePreventiveMaintenanceHistory,
  getPreventiveMaintenanceHistory,
  getPreventiveMaintenanceTask,
  listPreventiveMaintenanceHistory,
  startPreventiveMaintenanceTask
} from '@/api/project3/preventiveMaintenance'
import { finitePairs, finiteValues } from '@/utils/project3Validation'

defineOptions({ name: 'PreventiveMaintenance' })

const intervalKeys = ['T1', 'T2', 'T3']
const countKeys = ['N1', 'N2', 'N3']
const relationFields = [
  { key: 'equipmentId', label: '设备编号 equipmentId', placeholder: '请输入设备编号' },
  { key: 'partInstanceId', label: '零件实例编号 partInstanceId', placeholder: '请输入零件实例编号' },
  { key: 'processExecutionId', label: '工序实例编号 processExecutionId', placeholder: '请输入工序实例编号' },
  { key: 'taskNo', label: '算法任务编号 taskNo', placeholder: '请输入算法任务编号' }
]
const monteCarloFields = [
  { key: 'sampleCount', label: '样本数量 n', min: 1, step: 1, precision: 0 },
  { key: 'meanTm', label: '故障维修时间均值 meanTm', min: 0, step: 0.1, precision: 4 },
  { key: 'stdTm', label: '故障维修时间标准差 stdTm', min: 0, step: 0.0001, precision: 4 },
  { key: 'meanTpm', label: '预防维护时间均值 meanTpm', min: 0, step: 0.1, precision: 4 },
  { key: 'stdTpm', label: '预防维护时间标准差 stdTpm', min: 0, step: 0.0001, precision: 4 },
  { key: 'meanTr', label: '更换时间均值 meanTr', min: 0, step: 0.1, precision: 4 },
  { key: 'stdTr', label: '更换时间标准差 stdTr', min: 0, step: 0.0001, precision: 4 }
]
const costFields = [
  { key: 'cm', label: '故障维修成本 c_m', step: 100 },
  { key: 'cpm', label: '预防维护成本 c_pm', step: 100 },
  { key: 'cp', label: '停机损失成本 c_p', step: 100 },
  { key: 'cr', label: '更换成本 c_r', step: 1000 }
]
const reliabilityFields = [
  { key: 'Rm', label: '可靠度阈值 R_m', min: 0, max: 1, step: 0.01, precision: 2 },
  { key: 'a', label: '役龄递减因子 a', min: 0, step: 0.01, precision: 2 },
  { key: 'b', label: '故障率递增因子 b', min: 0, step: 0.01, precision: 2 },
  { key: 'alpha', label: 'Weibull 参数 alpha', min: 1, step: 1, precision: 0 },
  { key: 'beta', label: 'Weibull 参数 beta', min: 0.1, step: 0.1, precision: 1 }
]
const bkaFields = [
  { key: 'population', label: '种群规模 N', min: 4, step: 1, precision: 0 },
  { key: 'iterations', label: '迭代次数 T', min: 1, step: 10, precision: 0 },
  { key: 'attackThreshold', label: '攻击行为阈值 p', min: 0, max: 1, step: 0.1, precision: 1 },
  { key: 'objNo', label: '目标数量 obj_no', min: 2, max: 2, step: 1, precision: 0, readonly: true }
]

const defaultForm = () => ({
  equipmentId: '',
  partInstanceId: '',
  processExecutionId: '',
  taskNo: `PMO-${new Date().toISOString().slice(0, 10).replaceAll('-', '')}`,
  T1: 90,
  T2: 75,
  T3: 60,
  N1: 3,
  N2: 4,
  N3: 5,
  sampleCount: 30,
  meanTm: 0.5,
  stdTm: 0.0005,
  meanTpm: 1.5,
  stdTpm: 0.0015,
  meanTr: 2,
  stdTr: 0.002,
  cm: 7000,
  cpm: 8000,
  cp: 4000,
  cr: 600000,
  Rm: 0.75,
  a: 0.15,
  b: 1.15,
  alpha: 190,
  beta: 3,
  population: 20,
  iterations: 100,
  attackThreshold: 0.3,
  objNo: 2
})

const form = reactive(defaultForm())
const running = ref(false)
const recommended = ref(null)
const paretoResults = ref([])
const intervalSequence = ref([])
const samples = reactive({ tm: [], tpm: [], tr: [] })
const taskState = reactive({ taskId: '', status: '', progress: 0, message: '', error: '' })
const historyLoading = ref(false)
const historyDetailTaskId = ref('')
const deletingTaskId = ref('')
const historyRows = ref([])
const historyTotal = ref(0)
const historyQuery = reactive({ keyword: '', status: '', pageNum: 1, pageSize: 10 })
let pollingTimer = null

const sampleLineRef = ref(null)
const sampleScatterRef = ref(null)
const paretoRef = ref(null)
const intervalRef = ref(null)
let sampleLineChart
let sampleScatterChart
let paretoChart
let intervalChart

const metrics = computed(() => [
  { label: '推荐单位成本费率 C_u', value: recommended.value ? formatCost(recommended.value.Cu) : '--', unit: '元 / 时间单位', tone: 'blue' },
  { label: '推荐可用度 A', value: recommended.value ? formatRate(recommended.value.A) : '--', unit: '运行可用比例', tone: 'green' },
  { label: '最小可靠度 minR', value: recommended.value ? formatRate(recommended.value.minR) : '--', unit: `阈值 ${form.Rm}`, tone: 'orange' },
  { label: 'Pareto 非支配解数量', value: paretoResults.value.length || '--', unit: '组候选策略', tone: 'purple' }
])
const statusText = computed(() => ({
  PENDING: '等待执行',
  RUNNING: '运行中',
  SUCCESS: '运行成功',
  FAILED: '运行失败',
  CANCELED: '已取消'
}[taskState.status] || '暂无任务'))
const statusTagType = computed(() => ({
  RUNNING: 'primary',
  SUCCESS: 'success',
  FAILED: 'danger',
  CANCELED: 'warning'
}[taskState.status] || 'info'))
const progressStatus = computed(() => taskState.status === 'SUCCESS' ? 'success' : taskState.status === 'FAILED' ? 'exception' : '')

function validateForm() {
  if (!(form.T1 >= form.T2 && form.T2 >= form.T3)) {
    ElMessage.error('决策变量必须满足 T1 ≥ T2 ≥ T3。')
    return false
  }
  if (countKeys.some(key => !Number.isInteger(form[key]) || form[key] <= 0)) {
    ElMessage.error('N1、N2、N3 必须为正整数。')
    return false
  }
  if (!Number.isInteger(form.sampleCount) || form.sampleCount <= 0) {
    ElMessage.error('蒙特卡洛样本数量必须为正整数。')
    return false
  }
  if (form.Rm < 0 || form.Rm > 1) {
    ElMessage.error('可靠度阈值 R_m 必须在 0 到 1 之间。')
    return false
  }
  return true
}

async function runAlgorithm() {
  if (running.value || !validateForm()) return
  clearResult()
  running.value = true
  try {
    const response = await startPreventiveMaintenanceTask({ ...form })
    const task = response?.data || {}
    Object.assign(taskState, {
      taskId: task.taskId || '',
      status: task.status || 'PENDING',
      progress: Number(task.progress || 0),
      message: task.message || '任务已提交',
      error: ''
    })
    if (!taskState.taskId) throw new Error('未返回运行任务编号')
    loadHistory()
    pollTask()
    ElMessage.success('预防性维护优化任务已提交。')
  } catch (error) {
    running.value = false
    ElMessage.error(error?.response?.data?.msg || error?.message || '任务提交失败')
  }
}

async function pollTask() {
  clearPolling()
  const query = async () => {
    try {
      const response = await getPreventiveMaintenanceTask(taskState.taskId)
      const state = response?.data || {}
      Object.assign(taskState, {
        status: state.status || taskState.status,
        progress: Number(state.progress || 0),
        message: state.message || '',
        error: state.error || ''
      })
      if (state.status === 'SUCCESS') {
        applyResult(state.result || {})
        running.value = false
        clearPolling()
        loadHistory()
        ElMessage.success('设备预防性维护优化完成。')
      } else if (state.status === 'FAILED' || state.status === 'CANCELED') {
        running.value = false
        clearPolling()
        loadHistory()
        if (state.status === 'FAILED') ElMessage.error(state.error || state.message || '算法任务执行失败')
      }
    } catch (error) {
      running.value = false
      clearPolling()
      ElMessage.error(error?.response?.data?.msg || error?.message || '任务状态查询失败')
    }
  }
  await query()
  if (running.value) pollingTimer = window.setInterval(query, 1500)
}

async function cancelTask() {
  if (!taskState.taskId) return
  try {
    const response = await cancelPreventiveMaintenanceTask(taskState.taskId)
    const state = response?.data || {}
    Object.assign(taskState, {
      status: state.status || 'CANCELED',
      message: state.message || '任务已取消'
    })
    running.value = false
    clearPolling()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.message || '取消任务失败')
  }
}

function applyResult(result) {
  recommended.value = result.recommended || null
  paretoResults.value = Array.isArray(result.paretoResults) ? result.paretoResults : []
  intervalSequence.value = Array.isArray(result.intervalSequence) ? result.intervalSequence : []
  const monteCarlo = result.monteCarloSamples || {}
  samples.tm = Array.isArray(monteCarlo.tm) ? monteCarlo.tm : []
  samples.tpm = Array.isArray(monteCarlo.tpm) ? monteCarlo.tpm : []
  samples.tr = Array.isArray(monteCarlo.tr) ? monteCarlo.tr : []
  nextTick(renderCharts)
}

function clearResult() {
  recommended.value = null
  paretoResults.value = []
  intervalSequence.value = []
  samples.tm = []
  samples.tpm = []
  samples.tr = []
}

function resetParameters() {
  clearPolling()
  running.value = false
  Object.assign(form, defaultForm())
  Object.assign(taskState, { taskId: '', status: '', progress: 0, message: '', error: '' })
  clearResult()
}

function renderCharts() {
  renderSampleLine()
  renderSampleScatter()
  renderPareto()
  renderInterval()
}

function chartAxis(name, direction) {
  const horizontal = direction === 'x'
  return {
    name,
    nameLocation: 'middle',
    nameGap: horizontal ? 34 : 48,
    nameRotate: horizontal ? 0 : 90,
    nameTextStyle: {
      color: '#606266',
      fontSize: 12,
      align: 'center'
    },
    axisLine: { lineStyle: { color: '#c8d0dc' } },
    axisLabel: {
      color: '#606266',
      margin: 10,
      hideOverlap: true
    },
    splitLine: { lineStyle: { color: '#edf0f5' } }
  }
}

function chartGrid(top = 32) {
  return {
    left: 24,
    right: 28,
    top,
    bottom: 20,
    containLabel: true
  }
}

function renderSampleLine() {
  if (!sampleLineRef.value || !samples.tm.length) return
  const tm = finiteValues(samples.tm)
  const tpm = finiteValues(samples.tpm)
  const tr = finiteValues(samples.tr)
  if (!tm.length && !tpm.length && !tr.length) return
  sampleLineChart ||= echarts.init(sampleLineRef.value)
  sampleLineChart.setOption({
    color: ['#409eff', '#67c23a', '#e6a23c'],
    tooltip: { trigger: 'axis' },
    legend: { top: 4 },
    grid: chartGrid(52),
    xAxis: { ...chartAxis('样本序号', 'x'), type: 'category', data: tm.map((_, index) => index + 1) },
    yAxis: { ...chartAxis('时间', 'y'), type: 'value', scale: true },
    series: [
      { name: 't_m 故障维修', type: 'line', smooth: true, symbol: 'none', data: tm },
      { name: 't_pm 预防维护', type: 'line', smooth: true, symbol: 'none', data: tpm },
      { name: 't_r 更换', type: 'line', smooth: true, symbol: 'none', data: tr }
    ]
  }, true)
}

function renderSampleScatter() {
  if (!sampleScatterRef.value || !samples.tpm.length) return
  const data = finitePairs(samples.tpm, samples.tr)
  if (!data.length) return
  sampleScatterChart ||= echarts.init(sampleScatterRef.value)
  sampleScatterChart.setOption({
    tooltip: { trigger: 'item' },
    grid: chartGrid(),
    xAxis: { ...chartAxis('t_pm', 'x'), type: 'value', scale: true },
    yAxis: { ...chartAxis('t_r', 'y'), type: 'value', scale: true },
    series: [{
      type: 'scatter',
      symbolSize: 10,
      itemStyle: { color: '#409eff', opacity: 0.75 },
      data
    }]
  }, true)
}

function renderPareto() {
  if (!paretoRef.value || !paretoResults.value.length) return
  paretoChart ||= echarts.init(paretoRef.value)
  const candidates = paretoResults.value
    .filter(item => item.status !== 'RECOMMENDED')
    .map(item => [Number(item.Cu), Number(item.A)])
    .filter(([cost, availability]) => Number.isFinite(cost) && Number.isFinite(availability))
  const selected = recommended.value
    ? finitePairs([recommended.value.Cu], [recommended.value.A])
    : []
  paretoChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { top: 4 },
    grid: chartGrid(52),
    xAxis: { ...chartAxis('单位成本费率 C_u', 'x'), type: 'value', scale: true },
    yAxis: { ...chartAxis('可用度 A', 'y'), type: 'value', scale: true },
    series: [
      { name: '候选非支配解', type: 'scatter', symbolSize: 12, data: candidates, itemStyle: { color: '#409eff' } },
      { name: '推荐解', type: 'scatter', symbolSize: 20, data: selected, itemStyle: { color: '#f56c6c', borderColor: '#fff', borderWidth: 2 } }
    ]
  }, true)
}

function renderInterval() {
  if (!intervalRef.value || !intervalSequence.value.length) return
  const values = finiteValues(intervalSequence.value)
  if (!values.length) return
  intervalChart ||= echarts.init(intervalRef.value)
  const firstEnd = recommended.value?.N1 || 0
  const secondEnd = firstEnd + (recommended.value?.N2 || 0)
  intervalChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: chartGrid(),
    xAxis: { ...chartAxis('维护序号', 'x'), type: 'category', data: values.map((_, index) => index + 1) },
    yAxis: { ...chartAxis('维护间隔', 'y'), type: 'value', min: 0 },
    series: [{
      type: 'bar',
      barMaxWidth: 34,
      data: values.map((value, index) => ({
        value,
        itemStyle: { color: index < firstEnd ? '#409eff' : index < secondEnd ? '#67c23a' : '#e6a23c' }
      }))
    }]
  }, true)
}

function formatCost(value) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatRate(value) {
  return Number(value || 0).toFixed(4)
}

function taskStatusText(status) {
  return {
    PENDING: '等待执行',
    RUNNING: '运行中',
    SUCCESS: '运行成功',
    FAILED: '运行失败',
    CANCELED: '已取消'
  }[status] || status || '-'
}

function taskStatusTagType(status) {
  return {
    RUNNING: 'primary',
    SUCCESS: 'success',
    FAILED: 'danger',
    CANCELED: 'warning'
  }[status] || 'info'
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ') : '-'
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const response = await listPreventiveMaintenanceHistory({
      keyword: historyQuery.keyword.trim(),
      status: historyQuery.status,
      page_num: historyQuery.pageNum,
      page_size: historyQuery.pageSize
    })
    const data = response?.data || {}
    historyRows.value = Array.isArray(data.rows) ? data.rows : []
    historyTotal.value = Number(data.total || 0)
  } catch (error) {
    historyRows.value = []
    historyTotal.value = 0
    ElMessage.error(error?.response?.data?.msg || error?.message || '算法执行历史加载失败')
  } finally {
    historyLoading.value = false
  }
}

function searchHistory() {
  historyQuery.pageNum = 1
  loadHistory()
}

function resetHistoryQuery() {
  Object.assign(historyQuery, { keyword: '', status: '', pageNum: 1 })
  loadHistory()
}

async function viewHistoryResult(row) {
  if (!row?.taskId || historyDetailTaskId.value) return
  historyDetailTaskId.value = row.taskId
  try {
    await loadHistoryResult(row)
    ElMessage.success('历史算法结果已加载')
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.message || '历史算法结果加载失败')
  } finally {
    historyDetailTaskId.value = ''
  }
}

async function loadHistoryResult(row) {
  const response = await getPreventiveMaintenanceHistory(row.taskId)
  const state = response?.data || {}
  if (state.status !== 'SUCCESS' || !state.result) throw new Error('该任务暂无可查看的算法结果')
  Object.assign(taskState, {
    taskId: state.taskId || row.taskId,
    status: state.status,
    progress: Number(state.progress || 100),
    message: state.message || '任务完成',
    error: state.error || ''
  })
  Object.assign(form, {
    taskNo: row.taskNo || form.taskNo,
    equipmentId: row.equipmentId || '',
    partInstanceId: row.partInstanceId || '',
    processExecutionId: row.processExecutionId || ''
  })
  applyResult(state.result)
}

async function loadLatestResult() {
  try {
    const response = await listPreventiveMaintenanceHistory({
      status: 'SUCCESS',
      page_num: 1,
      page_size: 1
    })
    const latest = response?.data?.rows?.[0]
    if (latest?.taskId) await loadHistoryResult(latest)
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.message || '上一次算法结果加载失败')
  }
}

async function deleteHistory(row) {
  if (!row?.taskId || deletingTaskId.value) return
  try {
    await ElMessageBox.confirm(
      `确认删除算法执行历史 ${row.taskId} 吗？对应的算法结果文件和数据库记录将同步删除，且无法恢复。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }
    )
    deletingTaskId.value = row.taskId
    await deletePreventiveMaintenanceHistory(row.taskId)
    if (taskState.taskId === row.taskId) {
      Object.assign(taskState, { taskId: '', status: '', progress: 0, message: '', error: '' })
      clearResult()
    }
    if (historyRows.value.length === 1 && historyQuery.pageNum > 1) {
      historyQuery.pageNum -= 1
    }
    await loadHistory()
    ElMessage.success('算法执行历史及结果文件已删除')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.response?.data?.msg || error?.message || '算法执行历史删除失败')
    }
  } finally {
    deletingTaskId.value = ''
  }
}

function clearPolling() {
  if (pollingTimer) window.clearInterval(pollingTimer)
  pollingTimer = null
}

function resizeCharts() {
  sampleLineChart?.resize()
  sampleScatterChart?.resize()
  paretoChart?.resize()
  intervalChart?.resize()
}

window.addEventListener('resize', resizeCharts)
onMounted(() => {
  loadHistory()
  loadLatestResult()
})
onBeforeUnmount(() => {
  clearPolling()
  window.removeEventListener('resize', resizeCharts)
  ;[sampleLineChart, sampleScatterChart, paretoChart, intervalChart].forEach(chart => chart?.dispose())
})
</script>

<style scoped>
.maintenance-page { padding: 16px; background: #f3f6f9; }
.page-header, .panel-card { border: 1px solid #e4e7ed; border-radius: 8px; }
.page-header { margin-bottom: 16px; background: linear-gradient(135deg, #fff 0%, #f2f8ff 100%); }
.header-content, .card-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.header-content h2 { margin: 0 0 8px; color: #303133; font-size: 24px; }
.header-content p { margin: 0; color: #606266; line-height: 1.6; }
.main-row { align-items: stretch; }
.main-row > .el-col { margin-bottom: 16px; }
.panel-card { margin-bottom: 16px; }
.main-row .panel-card { height: 100%; margin-bottom: 0; }
.card-title { color: #303133; font-size: 16px; font-weight: 600; }
.card-subtitle { margin-left: 10px; color: #909399; font-size: 13px; }
.header-actions { display: flex; gap: 8px; }
.header-actions .el-button { margin-left: 0; }
.form-section { padding: 14px 14px 2px; margin-bottom: 12px; border: 1px solid #ebeef5; border-radius: 6px; background: #fbfcfe; }
.compact-section { height: calc(100% - 12px); }
.section-title, .result-section-title, .chart-title { color: #303133; font-weight: 600; }
.section-title { padding-left: 9px; margin-bottom: 14px; border-left: 3px solid #409eff; font-size: 14px; }
.constraint-alert { margin-bottom: 14px; }
.parameter-form :deep(.el-form-item) { margin-bottom: 14px; }
.parameter-form :deep(.el-form-item__label) { padding-bottom: 6px; color: #606266; line-height: 1.3; }
.parameter-form :deep(.el-input-number) { width: 100%; }
.task-progress { padding: 12px 14px; margin-bottom: 16px; border-radius: 6px; background: #f5f7fa; }
.task-progress-head { display: flex; justify-content: space-between; margin-bottom: 8px; color: #606266; font-size: 13px; }
.metric-grid { margin-bottom: 18px; }
.metric-grid .el-col { margin-bottom: 12px; }
.metric-card { min-height: 116px; padding: 18px; border: 1px solid #e4e7ed; border-radius: 7px; background: #fff; }
.metric-card.blue { border-top: 3px solid #409eff; }
.metric-card.green { border-top: 3px solid #67c23a; }
.metric-card.orange { border-top: 3px solid #e6a23c; }
.metric-card.purple { border-top: 3px solid #8e6cef; }
.metric-label { color: #606266; font-size: 13px; }
.metric-value { margin: 12px 0 5px; color: #303133; font-size: 25px; font-weight: 700; line-height: 1; }
.metric-unit { color: #909399; font-size: 12px; }
.result-section-title { margin: 4px 0 12px; font-size: 15px; }
.second-table { margin-top: 10px; }
.run-summary { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; margin-top: 18px; }
.run-summary > div { padding: 12px; border-radius: 6px; background: #f5f7fa; }
.run-summary span, .run-summary strong { display: block; }
.run-summary span { margin-bottom: 6px; color: #909399; font-size: 12px; }
.run-summary strong { overflow: hidden; color: #303133; font-size: 14px; text-overflow: ellipsis; white-space: nowrap; }
.chart-panel { padding: 12px; border: 1px solid #ebeef5; border-radius: 6px; background: #fff; }
.chart-title { margin: 2px 0 8px; font-size: 14px; }
.chart { width: 100%; height: 340px; }
.chart-panel :deep(.el-empty) { height: 340px; padding: 0; }
.table-card { margin-bottom: 0; }
.history-card { margin-top: 16px; margin-bottom: 0; }
.history-toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.history-toolbar .el-input { width: 320px; }
.history-toolbar .el-select { width: 150px; }
@media (max-width: 1200px) { .main-row .panel-card { height: auto; } }
@media (max-width: 768px) {
  .maintenance-page { padding: 10px; }
  .header-content, .card-header { align-items: flex-start; flex-direction: column; }
  .header-actions { width: 100%; flex-wrap: wrap; }
  .card-subtitle { display: block; margin: 5px 0 0; }
  .run-summary { grid-template-columns: 1fr; }
  .chart { height: 300px; }
  .history-toolbar { flex-wrap: wrap; }
  .history-toolbar .el-input, .history-toolbar .el-select { width: 100%; }
}
</style>
