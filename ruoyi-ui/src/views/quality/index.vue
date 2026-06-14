<template>
  <div class="quality-center-page">
    <!-- 标题区 -->
    <div class="page-title">
      <div>
        <h1>质量问题管理中心</h1>
        <p>用于统一创建质量问题、选择处理模块、跟踪处理进度并完成问题闭环。</p>
      </div>

      <div class="page-stat">
        <div>
          <span>问题总数</span>
          <strong>{{ problemList.length }}</strong>
        </div>
        <div>
          <span>处理中</span>
          <strong>{{ processingCount }}</strong>
        </div>
        <div>
          <span>已结束</span>
          <strong>{{ finishedCount }}</strong>
        </div>
      </div>
    </div>

    <!-- 上方：历史问题清单 + 新问题填报 -->
    <div class="top-layout">
      <!-- 左侧历史问题清单 -->
      <section class="panel history-panel" v-loading="loading">
        <div class="panel-header">
          <div>
            <h2>历史问题清单</h2>
            <p>点击问题查看对应工作流</p>
          </div>

          <div class="history-header-actions">
            <el-tag size="small" type="info">{{ problemList.length }} 条</el-tag>
          </div>
        </div>

        <el-input
          v-model="searchKeyword"
          placeholder="搜索编号/标题"
          clearable
          class="history-search"
        />

        <div class="problem-list">
          <div
            v-for="item in filteredProblemList"
            :key="item.problemId"
            class="problem-item"
            :class="{ active: currentProblem && currentProblem.problemId === item.problemId }"
            @click="selectProblem(item)"
          >
            <div class="problem-item-top">
              <span class="problem-code">{{ item.problemCode }}</span>
              <el-tag size="small" :type="getProblemStatusType(item.status)">
                {{ getProblemStatusText(item.status) }}
              </el-tag>
            </div>

            <div class="problem-title-text">{{ item.title }}</div>

            <div class="problem-meta">
              <span>{{ item.severity || '-' }}</span>
              <span>{{ item.currentModule || '待分派' }}</span>
            </div>

            <div class="problem-time">{{ item.createTime }}</div>
          </div>

          <el-empty
            v-if="filteredProblemList.length === 0"
            description="暂无匹配问题"
            :image-size="80"
          />
        </div>
      </section>

      <!-- 右侧新问题填报 -->
      <section class="panel report-panel">
        <div class="panel-header">
          <div>
            <h2>新问题填报栏</h2>
            <p>填写质量问题基础信息，提交后进入工作流分派环节</p>
          </div>
          <el-button plain @click="resetForm">清空表单</el-button>
        </div>

        <el-form
          ref="problemFormRef"
          :model="problemForm"
          :rules="problemRules"
          label-width="110px"
          class="problem-form"
        >
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="问题标题" prop="title">
                <el-input v-model="problemForm.title" placeholder="请输入质量问题标题" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="发生时间" prop="occurTime">
                <el-date-picker
                  v-model="problemForm.occurTime"
                  type="datetime"
                  placeholder="选择发生时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="产品型号" prop="productModel">
                <el-input v-model="problemForm.productModel" placeholder="请输入产品型号" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="涉及系统" prop="involvedSystem">
                <el-select
                  v-model="problemForm.involvedSystem"
                  placeholder="请选择涉及系统"
                  clearable
                  filterable
                  style="width: 100%"
                >
                  <el-option label="液压系统" value="液压系统" />
                  <el-option label="电气系统" value="电气系统" />
                  <el-option label="燃油系统" value="燃油系统" />
                  <el-option label="起落架系统" value="起落架系统" />
                  <el-option label="飞控系统" value="飞控系统" />
                  <el-option label="环控系统" value="环控系统" />
                  <el-option label="结构系统" value="结构系统" />
                  <el-option label="动力系统" value="动力系统" />
                  <el-option label="其他系统" value="其他系统" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="发生部位" prop="occurPart">
                <el-input v-model="problemForm.occurPart" placeholder="请输入发生部位" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="部件编号">
                <el-input v-model="problemForm.componentCode" placeholder="如 C010" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="严重程度" prop="severity">
                <el-select v-model="problemForm.severity" placeholder="请选择" style="width: 100%">
                  <el-option label="一般" value="一般" />
                  <el-option label="重要" value="重要" />
                  <el-option label="严重" value="严重" />
                  <el-option label="紧急" value="紧急" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="问题来源" prop="source">
                <el-select v-model="problemForm.source" placeholder="请选择" style="width: 100%">
                  <el-option label="人工填报" value="人工填报" />
                  <el-option label="检测发现" value="检测发现" />
                  <el-option label="试验反馈" value="试验反馈" />
                  <el-option label="运行反馈" value="运行反馈" />
                  <el-option label="客户反馈" value="客户反馈" />
                  <el-option label="系统预警" value="系统预警" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="填报人员">
                <el-input v-model="problemForm.reporter" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="问题描述" prop="description">
                <el-input
                  v-model="problemForm.description"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入问题现象、发生位置、影响范围、异常表现等信息"
                />
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="影响范围">
                <el-input
                  v-model="problemForm.influenceScope"
                  type="textarea"
                  :rows="3"
                  placeholder="选填，例如：影响某批次产品、某试验环节或某部件功能"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="form-actions">
            <el-button type="primary" :loading="submitLoading" @click="createProblem">
              新建质量问题
            </el-button>

            <el-button @click="resetForm">
              重置
            </el-button>

            <el-button
              type="danger"
              plain
              :loading="deleteLoading"
              :disabled="!currentProblem"
              @click="deleteSelectedProblem"
            >
              删除选中问题
            </el-button>
          </div>
        </el-form>
      </section>
    </div>

    <!-- 下方工作流 -->
    <section class="panel workflow-panel">
      <div class="panel-header">
        <div>
          <h2>具体工作流</h2>
          <p>当前为超级管理员操作流程：填报问题、分派模块、处理确认、结束问题。</p>
        </div>
      </div>

      <el-empty
        v-if="!currentProblem"
        description="请先在左侧选择一个问题，或在右侧新建质量问题"
      />

      <template v-else>
        <div class="current-problem">
          <div class="current-problem-title">
            <div>
              <span>当前问题</span>
              <h3>{{ currentProblem.problemCode }}｜{{ currentProblem.title }}</h3>
            </div>
            <el-tag :type="getProblemStatusType(currentProblem.status)" size="large">
              {{ getProblemStatusText(currentProblem.status) }}
            </el-tag>
          </div>

          <el-descriptions :column="4" border size="small" class="problem-desc-table">
            <el-descriptions-item label="发生时间">
              {{ currentProblem.occurTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="严重程度">
              {{ currentProblem.severity || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="问题状态">
              {{ getProblemStatusText(currentProblem.status) }}
            </el-descriptions-item>
            <el-descriptions-item label="涉及系统">
              {{ currentProblem.involvedSystem || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="workflow-middle-layout">
          <div class="workflow-section module-section">
            <div class="sub-title">
              <h3>选择工作模块</h3>
              <p>每次只能选择一个课题模块，分派后仍保持选中状态，可立即进入对应模块。</p>
            </div>

            <div class="module-list-vertical">
              <div
                v-for="module in workModules"
                :key="module.moduleCode"
                class="module-item-vertical"
                :class="{ selected: selectedModuleCode === module.moduleCode }"
                @click="selectModule(module.moduleCode)"
              >
                <div class="module-name">{{ getModuleDisplayName(module) }}</div>
                <div class="module-desc">{{ module.description }}</div>
              </div>
            </div>

            <el-input
              v-model="dispatchOpinion"
              type="textarea"
              :rows="4"
              placeholder="请输入本轮分派说明"
            />

            <div class="workflow-button-row">
              <el-button
                type="primary"
                :loading="dispatchLoading"
                :disabled="currentProblem.status === 'FINISHED'"
                @click="dispatchSelectedModule"
              >
                分派到选中模块
              </el-button>

              <el-button
                type="success"
                plain
                :disabled="!selectedModuleCode"
                @click="goToSelectedModule"
              >
                进入选中模块
              </el-button>

            </div>

            
          </div>

          <div class="log-flow-layout">
            <div class="workflow-section log-section">
              <div class="sub-title">
                <h3>流程日志</h3>
                <p>按照分派任务展示处理状态，结果确认后该条记录完成闭环。</p>
              </div>

              <div class="log-scroll">
                <div v-if="taskFlowRecords.length > 0" class="dispatch-log-list">
                  <div
                    v-for="record in taskFlowRecords"
                    :key="record.taskId"
                    class="dispatch-log-card"
                    :class="getFlowRecordCardClass(record.taskStatus)"
                  >
                    <div class="dispatch-log-main">
                      <div class="dispatch-log-top">
                        <div>
                          <span class="log-time">{{ record.dispatchTime || record.createTime || '-' }}</span>
                          <el-tag size="small" type="info" effect="plain">已分派</el-tag>
                        </div>

                        <span
                          class="process-state"
                          :class="getProcessStateClass(record.taskStatus)"
                        >
                          {{ getProcessStateText(record.taskStatus) }}
                        </span>
                      </div>

                      <div class="dispatch-log-content">
                        <div class="log-line">
                          <span class="log-label">处理模块</span>
                          <strong>{{ getModuleDisplayName(record) }}</strong>
                        </div>

                        <div class="log-line">
                          <span class="log-label">处理状态</span>
                          <span
                            class="module-process-text"
                            :class="getProcessStateClass(record.taskStatus)"
                          >
                            {{ getModuleProcessSentence(record.taskStatus) }}
                          </span>
                        </div>

                        <div class="log-line">
                          <span class="log-label">分派说明</span>
                          <span>{{ record.dispatchOpinion || '-' }}</span>
                        </div>

                        <div v-if="record.processResult" class="log-line result-line">
                          <span class="log-label">处理结果</span>
                          <span>{{ record.processResult }}</span>
                        </div>
                      </div>
                    </div>

                    <div class="dispatch-log-side">
                      <el-button
                        v-if="record.taskStatus === 'SUBMITTED'"
                        size="small"
                        type="warning"
                        plain
                        @click="confirmTask(record)"
                      >
                        待确认
                      </el-button>

                      <div v-else class="side-status">
                        <span
                          class="confirm-state"
                          :class="getConfirmStateClass(record.taskStatus)"
                        >
                          {{ getConfirmStateText(record.taskStatus) }}
                        </span>

                        <span v-if="record.confirmTime" class="confirm-time">
                          {{ record.confirmTime }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>

                <el-empty
                  v-else
                  description="暂无模块分派日志"
                  :image-size="90"
                />
              </div>
            </div>

            <div class="workflow-section flow-section">
              <div class="sub-title">
                <h3>流程状态</h3>
                <p>当前问题处理阶段</p>
              </div>

              <el-steps
                :active="workflowActiveStep"
                direction="vertical"
                finish-status="success"
                process-status="process"
                class="vertical-steps"
              >
                <el-step title="问题填报" description="问题已创建" />
                <el-step title="问题处理" description="模块分派与处理" />
                <el-step title="问题结束" description="质量问题闭环" />
              </el-steps>
            </div>
          </div>
        </div>

        <div class="workflow-section task-section">
          <div class="task-section-header">
            <div class="sub-title">
              <h3>任务处理记录</h3>
              <p>共 {{ currentProblem.tasks ? currentProblem.tasks.length : 0 }} 条任务</p>
            </div>

            <div class="task-action-buttons">
              <el-button
                type="success"
                plain
                :loading="finishLoading"
                :disabled="currentProblem.status === 'FINISHED'"
                @click="finishProblem"
              >
                结束问题
              </el-button>

              <el-button
                type="primary"
                plain
                :loading="reportLoading"
                :disabled="!currentProblem"
                @click="generateProblemReport"
              >
                生成报告
              </el-button>
            </div>
          </div>

          <el-table :data="currentProblem.tasks || []" border stripe>
            <el-table-column label="模块名称" width="240">
              <template #default="scope">
                <span class="module-name-nowrap">{{ getModuleDisplayName(scope.row) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="taskStatus" label="任务状态" width="120">
              <template #default="scope">
                <el-tag size="small" :type="getTaskStatusType(scope.row.taskStatus)">
                  {{ getTaskStatusText(scope.row.taskStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="dispatchOpinion" label="分派说明" show-overflow-tooltip />
            <el-table-column prop="processResult" label="处理结果" show-overflow-tooltip>
              <template #default="scope">
                {{ scope.row.processResult || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="scope">
                <el-button
                  size="small"
                  type="warning"
                  plain
                  :disabled="scope.row.taskStatus !== 'PROCESSING'"
                  @click="simulateTaskSubmit(scope.row)"
                >
                  模块处理完成
                </el-button>

                <el-button
                  size="small"
                  type="success"
                  plain
                  :disabled="scope.row.taskStatus !== 'SUBMITTED'"
                  @click="confirmTask(scope.row)"
                >
                结果确认
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { listProblem, addProblem, updateProblem, delProblem, exportProblemReport } from '@/api/quality/problem'
import { listModule } from '@/api/quality/module'
import { listTask, addTask, updateTask } from '@/api/quality/task'
import { listLog, addLog } from '@/api/quality/log'

const router = useRouter()

const searchKeyword = ref('')
const currentProblem = ref(null)
const selectedModuleCode = ref('')
const dispatchOpinion = ref('')
const problemFormRef = ref(null)

const loading = ref(false)
const submitLoading = ref(false)
const dispatchLoading = ref(false)
const finishLoading = ref(false)
const deleteLoading = ref(false)
const reportLoading = ref(false)

const problemForm = reactive({
  title: '',
  occurTime: '',
  productModel: '',
  involvedSystem: '',
  occurPart: '',
  componentCode: '',
  severity: '',
  source: '',
  reporter: '',
  description: '',
  influenceScope: ''
})

const problemRules = {
  title: [{ required: true, message: '请输入问题标题', trigger: 'blur' }],
  occurTime: [{ required: true, message: '请选择发生时间', trigger: 'change' }],
  productModel: [{ required: true, message: '请输入产品型号', trigger: 'blur' }],
  involvedSystem: [{ required: true, message: '请选择涉及系统', trigger: 'change' }],
  occurPart: [{ required: true, message: '请输入发生部位', trigger: 'blur' }],
  severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }],
  source: [{ required: true, message: '请选择问题来源', trigger: 'change' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }]
}

const defaultModules = [
  {
    moduleCode: 'PROJECT_1',
    moduleName: '全域异构信息集成系统',
    moduleType: 'TOPIC',
    route: '/project_1'
  },
  {
    moduleCode: 'PROJECT_2',
    moduleName: '复杂产品设计制造协同优化平台',
    moduleType: 'TOPIC',
    route: '/project_2'
  },
  {
    moduleCode: 'PROJECT_3',
    moduleName: '复杂产品质量监管与故障预防',
    moduleType: 'TOPIC',
    route: '/project_3'
  },
  {
    moduleCode: 'PROJECT_4',
    moduleName: '智能故障诊断与根源性分析',
    moduleType: 'TOPIC',
    route: '/project_4'
  },
  {
    moduleCode: 'PROJECT_5',
    moduleName: '质量自反馈追溯系统',
    moduleType: 'TOPIC',
    route: '/project_5'
  }
]

const moduleDisplayNames = {
  PROJECT_1: '全域异构信息集成系统',
  PROJECT_2: '复杂产品设计制造协同优化平台',
  PROJECT_3: '复杂产品质量监管与故障预防',
  PROJECT_4: '智能故障诊断与根源性分析',
  PROJECT_5: '质量自反馈追溯系统'
}

const getModuleDisplayName = (module) => {
  if (!module) return '课题模块'

  return moduleDisplayNames[module.moduleCode] || module.moduleName || '课题模块'
}

const workModules = ref([])
const problemList = ref([])

onMounted(() => {
  initPage()
})

const initPage = async () => {
  await loadModules()
  await loadProblemList({
    keepCurrent: false,
    preserveSelected: false
  })
}

const loadModules = async () => {
  try {
    const res = await listModule({ status: '0' })
    const rows = Array.isArray(res?.rows) ? res.rows : []

    if (rows.length > 0) {
      workModules.value = rows.map((item) => {
        return {
          moduleCode: item.moduleCode,
          moduleName: item.moduleName,
          moduleType: item.moduleType || 'TOPIC',
          description: item.moduleDesc || item.description || '',
          route: item.moduleRoute || item.route || ''
        }
      })
    } else {
      workModules.value = defaultModules
    }
  } catch (error) {
    console.error('加载工作模块失败：', error)
    workModules.value = defaultModules
  }
}

const loadProblemList = async (options = {}) => {
  const keepCurrent = options.keepCurrent !== false
  const preserveSelected = options.preserveSelected !== false
  const oldProblemId = currentProblem.value?.problemId
  const oldSelectedModuleCode = selectedModuleCode.value

  loading.value = true

  try {
    const res = await listProblem({})
    const rows = Array.isArray(res?.rows) ? res.rows : []

    problemList.value = rows.map((item) => normalizeProblem(item))

    if (problemList.value.length > 0) {
      let targetProblem = null

      if (keepCurrent && oldProblemId) {
        targetProblem = problemList.value.find((item) => item.problemId === oldProblemId)
      }

      if (!targetProblem) {
        targetProblem = problemList.value[0]
      }

      if (keepCurrent && targetProblem) {
        await loadProblemDetail(targetProblem)
      } else {
        await selectProblem(targetProblem)
      }

      if (preserveSelected && oldSelectedModuleCode) {
        selectedModuleCode.value = oldSelectedModuleCode
      }
    } else {
      currentProblem.value = null
      selectedModuleCode.value = ''
    }
  } catch (error) {
    console.error('加载质量问题列表失败：', error)

    const realMsg =
      error?.response?.data?.msg ||
      error?.data?.msg ||
      error?.msg ||
      error?.message ||
      String(error)

    ElMessage.error(`加载质量问题列表失败：${realMsg}`)
  } finally {
    loading.value = false
  }
}

const normalizeProblem = (item) => {
  return {
    ...item,
    currentModule: item.currentModuleName || item.currentModule || '',
    tasks: item.tasks || [],
    logs: item.logs || []
  }
}

const normalizeTask = (item) => {
  return {
    ...item,
    createTime: item.createTime || item.dispatchTime || '',
    dispatchTime: item.dispatchTime || item.createTime || '',
    submitTime: item.submitTime || '',
    confirmTime: item.confirmTime || '',
    processResult: item.processResult || '',
    dispatchOpinion: item.dispatchOpinion || ''
  }
}

const loadProblemDetail = async (problem) => {
  if (!problem || !problem.problemId) return

  const detail = normalizeProblem(problem)

  try {
    const taskRes = await listTask({
      problemId: problem.problemId
    })

    detail.tasks = (taskRes.rows || []).map((item) => normalizeTask(item))
  } catch (error) {
    console.error('加载任务记录失败：', error)
    detail.tasks = []
  }

  try {
    const logRes = await listLog({
      problemId: problem.problemId
    })

    detail.logs = (logRes.rows || []).map((item) => {
      return {
        ...item,
        time: item.createTime || '',
        operator: item.operatorName || item.operator || '-',
        content: item.actionContent || item.content || ''
      }
    })
  } catch (error) {
    console.error('加载流程日志失败：', error)
    detail.logs = []
  }

  currentProblem.value = detail
}

const filteredProblemList = computed(() => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return problemList.value

  return problemList.value.filter((item) => {
    return (item.problemCode || '').includes(keyword) || (item.title || '').includes(keyword)
  })
})

const processingCount = computed(() => {
  return problemList.value.filter((item) => ['PROCESSING', 'WAIT_CONFIRM'].includes(item.status)).length
})

const finishedCount = computed(() => {
  return problemList.value.filter((item) => item.status === 'FINISHED').length
})

const workflowActiveStep = computed(() => {
  if (!currentProblem.value) return 0

  const status = currentProblem.value.status
  const tasks = Array.isArray(currentProblem.value.tasks) ? currentProblem.value.tasks : []

  // 点击“结束问题”后，三个环节全部绿色
  if (status === 'FINISHED') {
    return 3
  }

  // 只要已经分派过任务，就保持在“问题处理”阶段
  // 效果：问题填报绿色，问题处理蓝色，问题结束灰色
  if (
    status === 'PROCESSING' ||
    status === 'WAIT_CONFIRM' ||
    status === 'CONFIRMED' ||
    tasks.length > 0
  ) {
    return 1
  }

  // 新建后、未分派前
  // 效果：问题填报蓝色，问题处理灰色，问题结束灰色
  return 0
})

const selectedModule = computed(() => {
  return workModules.value.find((item) => item.moduleCode === selectedModuleCode.value) || null
})

const taskFlowRecords = computed(() => {
  if (!currentProblem.value || !Array.isArray(currentProblem.value.tasks)) {
    return []
  }

  return [...currentProblem.value.tasks]
    .map((item) => normalizeTask(item))
    .sort((a, b) => {
      const at = a.dispatchTime || a.createTime || ''
      const bt = b.dispatchTime || b.createTime || ''
      return bt.localeCompare(at)
    })
})

const selectProblem = async (problem) => {
  selectedModuleCode.value = ''
  dispatchOpinion.value = ''
  await loadProblemDetail(problem)
}

const deleteSelectedProblem = async () => {
  if (!currentProblem.value || !currentProblem.value.problemId) {
    ElMessage.warning('请先在历史问题清单中选择一条问题记录')
    return
  }

  const problem = currentProblem.value

  try {
    await ElMessageBox.confirm(
      `确认删除当前选中的质量问题？\n${problem.problemCode || ''}｜${problem.title || ''}`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    deleteLoading.value = true

    await delProblem(problem.problemId)

    ElMessage.success('选中问题已删除')

    currentProblem.value = null
    selectedModuleCode.value = ''
    dispatchOpinion.value = ''

    await loadProblemList({
      keepCurrent: false,
      preserveSelected: false
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除选中问题失败：', error)

      const realMsg =
        error?.response?.data?.msg ||
        error?.data?.msg ||
        error?.msg ||
        error?.message ||
        String(error)

      ElMessage.error(`删除选中问题失败：${realMsg}`)
    }
  } finally {
    deleteLoading.value = false
  }
}

const resetForm = () => {
  Object.keys(problemForm).forEach((key) => {
    problemForm[key] = ''
  })
  problemFormRef.value?.clearValidate()
}

const createProblem = async () => {
  await problemFormRef.value.validate()

  submitLoading.value = true

  const now = getNowTime()
  const problemCode = generateProblemCode()

  const data = {
    problemCode,
    title: problemForm.title,
    occurTime: problemForm.occurTime,
    productModel: problemForm.productModel,
    involvedSystem: problemForm.involvedSystem,
    occurPart: problemForm.occurPart,
    componentCode: problemForm.componentCode,
    severity: problemForm.severity,
    source: problemForm.source,
    reporter: problemForm.reporter,
    description: problemForm.description,
    influenceScope: problemForm.influenceScope,
    status: 'DISPATCHING',
    currentModuleCode: '',
    currentModuleName: ''
  }

  try {
    await addProblem(data)

    ElMessage.success('质量问题已新建，等待分派模块')
    resetForm()

    await loadProblemList({
      keepCurrent: false,
      preserveSelected: false
    })

    const newProblem = problemList.value.find((item) => item.problemCode === problemCode)
    if (newProblem) {
      await selectProblem(newProblem)

      await createFlowLog({
        problemId: newProblem.problemId,
        problemCode: newProblem.problemCode,
        actionType: 'CREATE',
        actionName: '问题填报',
        fromStatus: '',
        toStatus: 'DISPATCHING',
        actionContent: `新建质量问题：${newProblem.title}。`,
        createTime: now
      })

      await loadProblemDetail(newProblem)
    }
  } catch (error) {
    console.error('新建质量问题失败：', error)
    ElMessage.error('新建质量问题失败，请检查后端接口或字段配置')
  } finally {
    submitLoading.value = false
  }
}

const selectModule = (moduleCode) => {
  selectedModuleCode.value = selectedModuleCode.value === moduleCode ? '' : moduleCode
}

const clearSelectedModule = () => {
  selectedModuleCode.value = ''
}

const dispatchSelectedModule = async () => {
  if (!currentProblem.value) {
    ElMessage.warning('请先选择或新建质量问题')
    return
  }

  if (currentProblem.value.status === 'FINISHED') {
    ElMessage.warning('该问题已结束，不能继续分派')
    return
  }

  if (!selectedModuleCode.value) {
    ElMessage.warning('请选择一个工作模块')
    return
  }

  const module = selectedModule.value

  if (!module) {
    ElMessage.warning('选中的模块不存在')
    return
  }

  dispatchLoading.value = true

  const now = getNowTime()
  const keepSelectedModuleCode = module.moduleCode
  const opinion = dispatchOpinion.value || `请处理该质量问题。`

  try {
    await addTask({
      problemId: currentProblem.value.problemId,
      problemCode: currentProblem.value.problemCode,
      moduleCode: module.moduleCode,
      moduleName: module.moduleName,
      taskStatus: 'PROCESSING',
      dispatchOpinion: opinion,
      processResult: '',
      dispatchTime: now,
      createTime: now
    })

    await updateProblem({
      ...currentProblem.value,
      status: 'PROCESSING',
      currentModuleCode: module.moduleCode,
      currentModuleName: module.moduleName
    })
    currentProblem.value.status = 'PROCESSING'
    currentProblem.value.currentModuleCode = module.moduleCode
    currentProblem.value.currentModuleName = module.moduleName

    await createFlowLog({
      problemId: currentProblem.value.problemId,
      problemCode: currentProblem.value.problemCode,
      actionType: 'DISPATCH',
      actionName: '模块分派',
      fromStatus: currentProblem.value.status,
      toStatus: 'PROCESSING',
      actionContent: `已分派至${module.moduleName}，${module.moduleName}正在处理。分派说明：${opinion}`,
      createTime: now
    })

    dispatchOpinion.value = ''
    selectedModuleCode.value = keepSelectedModuleCode

    ElMessage.success(`已分派到${module.moduleName}，可直接进入选中模块`)

    await loadProblemList({
      keepCurrent: true,
      preserveSelected: true
    })

    selectedModuleCode.value = keepSelectedModuleCode
  } catch (error) {
    console.error('分派模块失败：', error)
    ElMessage.error('分派模块失败，请检查 task、log、problem 接口')
  } finally {
    dispatchLoading.value = false
  }
}

const goToSelectedModule = () => {
  if (!selectedModule.value) {
    ElMessage.warning('请先选择一个模块')
    return
  }

  if (!selectedModule.value.route) {
    ElMessage.warning('该模块未配置前端路由')
    return
  }

  if (/^https?:\/\//.test(selectedModule.value.route)) {
    window.open(selectedModule.value.route, '_blank')
    return
  }

  router.push(selectedModule.value.route)
}

const simulateTaskSubmit = async (task) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入模拟处理结果', '模块处理完成', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '例如：已完成分析，初步判断该问题与装配偏差或传感器异常有关。'
    })

    const now = getNowTime()
    const processResult = value || '已完成处理并提交结果。'

    await updateTask({
      ...task,
      taskStatus: 'SUBMITTED',
      processResult,
      submitTime: now
    })

    await updateProblem({
      ...currentProblem.value,
      status: 'WAIT_CONFIRM'
    })

    await createFlowLog({
      problemId: currentProblem.value.problemId,
      problemCode: currentProblem.value.problemCode,
      taskId: task.taskId,
      actionType: 'SUBMIT',
      actionName: '模块处理完成',
      fromStatus: currentProblem.value.status,
      toStatus: 'WAIT_CONFIRM',
      actionContent: `${task.moduleName}已完成处理，等待确认。处理结果：${processResult}`,
      createTime: now
    })

    ElMessage.success('模块处理结果已提交，等待确认')

    await loadProblemList({
      keepCurrent: true,
      preserveSelected: true
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交处理结果失败：', error)
      ElMessage.error('提交处理结果失败')
    }
  }
}

const confirmTask = async (task) => {
  if (!task || task.taskStatus !== 'SUBMITTED') {
    return
  }

  const now = getNowTime()

  try {
    await updateTask({
      ...task,
      taskStatus: 'CONFIRMED',
      confirmTime: now,
      confirmOpinion: '已确认处理结果。'
    })

    // 管确认后，不再把问题状态退回 DISPATCHING
    // 只要问题没有点击“结束问题”，流程状态始终保持在“问题处理”阶段
    const nextStatus = 'PROCESSING'

    await updateProblem({
      ...currentProblem.value,
      status: nextStatus,
      currentModuleCode: currentProblem.value.currentModuleCode,
      currentModuleName: currentProblem.value.currentModuleName
    })

    await createFlowLog({
      problemId: currentProblem.value.problemId,
      problemCode: currentProblem.value.problemCode,
      taskId: task.taskId,
      actionType: 'CONFIRM',
      actionName: '中心确认',
      fromStatus: currentProblem.value.status,
      toStatus: nextStatus,
      actionContent: `中心已确认${task.moduleName}的处理结果，该条分派日志已完成。`,
      createTime: now
    })

    ElMessage.success('处理结果已确认，该条流程日志已完成')

    await loadProblemList({
      keepCurrent: true,
      preserveSelected: true
    })
  } catch (error) {
    console.error('确认处理结果失败：', error)
    ElMessage.error('确认处理结果失败')
  }
}

const finishProblem = async () => {
  if (!currentProblem.value) return

  try {
    await ElMessageBox.confirm('确认结束当前质量问题？结束后将不再继续分派模块。', '结束问题', {
      confirmButtonText: '确认结束',
      cancelButtonText: '取消',
      type: 'warning'
    })

    finishLoading.value = true

    const now = getNowTime()

    const updateTaskPromises = (currentProblem.value.tasks || []).map((task) => {
      if (task.taskStatus !== 'CONFIRMED') {
        return updateTask({
          ...task,
          taskStatus: 'CONFIRMED',
          confirmTime: now,
          confirmOpinion: '问题结束时自动确认。'
        })
      }
      return Promise.resolve()
    })

    await Promise.all(updateTaskPromises)

    await updateProblem({
      ...currentProblem.value,
      status: 'FINISHED',
      currentModuleCode: '',
      currentModuleName: ''
    })

    await createFlowLog({
      problemId: currentProblem.value.problemId,
      problemCode: currentProblem.value.problemCode,
      actionType: 'FINISH',
      actionName: '问题结束',
      fromStatus: currentProblem.value.status,
      toStatus: 'FINISHED',
      actionContent: '该质量问题已完成全部处理流程并结束。',
      createTime: now
    })

    ElMessage.success('质量问题已结束')

    await loadProblemList({
      keepCurrent: true,
      preserveSelected: false
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('结束问题失败：', error)
      ElMessage.error('结束问题失败')
    }
  } finally {
    finishLoading.value = false
  }
}

const generateProblemReport = async () => {
  if (!currentProblem.value || !currentProblem.value.problemId) {
    ElMessage.warning('请先选择一个质量问题')
    return
  }

  reportLoading.value = true

  try {
    const blob = await exportProblemReport(currentProblem.value.problemId)

    const problemCode = currentProblem.value.problemCode || 'quality_problem'
    const fileName = `质量问题报告_${problemCode}.docx`

    downloadBlob(blob, fileName)

    ElMessage.success('质量问题报告已生成')
  } catch (error) {
    console.error('生成质量问题报告失败：', error)
    ElMessage.error('生成质量问题报告失败，请检查后端报告接口')
  } finally {
    reportLoading.value = false
  }
}

const downloadBlob = (blobData, fileName) => {
  const blob = new Blob([blobData], {
    type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  })

  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')

  link.href = url
  link.download = fileName
  link.style.display = 'none'

  document.body.appendChild(link)
  link.click()

  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

const createFlowLog = async (data) => {
  return addLog({
    problemId: data.problemId,
    problemCode: data.problemCode,
    taskId: data.taskId || null,
    actionType: data.actionType,
    actionName: data.actionName,
    operatorName: data.operatorName || '超级管理员',
    fromStatus: data.fromStatus || '',
    toStatus: data.toStatus || '',
    actionContent: data.actionContent || '',
    createTime: data.createTime || getNowTime()
  })
}

const getProblemStatusText = (status) => {
  const map = {
    CREATED: '已填报',
    DISPATCHING: '待处理',
    PROCESSING: '处理中',
    WAIT_CONFIRM: '处理中',
    FINISHED: '已结束'
  }
  return map[status] || status
}

const getProblemStatusType = (status) => {
  const map = {
    CREATED: 'info',
    DISPATCHING: 'warning',
    PROCESSING: 'primary',
    WAIT_CONFIRM: 'primary',
    FINISHED: 'success'
  }
  return map[status] || 'info'
}

const getTaskStatusText = (status) => {
  const map = {
    PROCESSING: '处理中',
    SUBMITTED: '待确认',
    CONFIRMED: '已确认'
  }
  return map[status] || status
}

const getTaskStatusType = (status) => {
  const map = {
    PROCESSING: 'primary',
    SUBMITTED: 'warning',
    CONFIRMED: 'success'
  }
  return map[status] || 'info'
}

const getProcessStateText = (status) => {
  if (status === 'PROCESSING') return '正在处理'
  if (status === 'SUBMITTED') return '已完成'
  if (status === 'CONFIRMED') return '已完成'
  return '-'
}

const getModuleProcessSentence = (status) => {
  if (status === 'PROCESSING') return '正在处理'
  if (status === 'SUBMITTED') return '已完成处理，等待确认'
  if (status === 'CONFIRMED') return '已完成处理，结果已确认'
  return ''
}

const getProcessStateClass = (status) => {
  if (status === 'PROCESSING') return 'state-processing'
  if (status === 'SUBMITTED') return 'state-finished'
  if (status === 'CONFIRMED') return 'state-finished'
  return ''
}

const getConfirmStateText = (status) => {
  if (status === 'PROCESSING') return '等待模块提交'
  if (status === 'CONFIRMED') return '结果已确认'
  if (status === 'SUBMITTED') return '待确认'
  return '-'
}

const getConfirmStateClass = (status) => {
  if (status === 'PROCESSING') return 'confirm-wait'
  if (status === 'SUBMITTED') return 'confirm-pending'
  if (status === 'CONFIRMED') return 'confirm-finished'
  return ''
}

const getFlowRecordCardClass = (status) => {
  if (status === 'PROCESSING') return 'card-processing'
  if (status === 'SUBMITTED') return 'card-submitted'
  if (status === 'CONFIRMED') return 'card-confirmed'
  return ''
}

const generateProblemCode = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const suffix = String(Date.now()).slice(-5)
  return `QF-${y}${m}${d}-${suffix}`
}

const getNowTime = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const h = String(now.getHours()).padStart(2, '0')
  const min = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}:${s}`
}
</script>

<style scoped lang="scss">
.quality-center-page {
  min-height: calc(100vh - 24px);
  padding: 16px;
  box-sizing: border-box;
  background: #f5f7fa;
  color: #303133;
}

.task-action-buttons {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.vertical-steps {
  :deep(.el-step__head.is-process) {
    color: #409eff;
    border-color: #409eff;
  }

  :deep(.el-step__head.is-process .el-step__icon) {
    color: #409eff;
    border-color: #409eff;
  }

  :deep(.el-step__title.is-process) {
    color: #409eff;
    font-weight: 600;
  }

  :deep(.el-step__description.is-process) {
    color: #409eff;
  }
}

.page-title {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  margin-bottom: 16px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.page-title h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  color: #303133;
}

.page-title p {
  margin: 8px 0 0;
  font-size: 14px;
  color: #606266;
}

.page-stat {
  display: flex;
  gap: 12px;
}

.page-stat div {
  width: 90px;
  padding: 10px;
  text-align: center;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #fafafa;
}

.page-stat span {
  display: block;
  font-size: 12px;
  color: #909399;
}

.page-stat strong {
  display: block;
  margin-top: 4px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.top-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.panel {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.panel-header p {
  margin: 6px 0 0;
  font-size: 13px;
  color: #909399;
}

.history-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.history-search {
  margin-bottom: 12px;
}

.problem-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 520px;
  overflow-y: auto;
}

.problem-item {
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #ffffff;
  cursor: pointer;
}

.problem-item:hover,
.problem-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.problem-item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.problem-code {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
}

.problem-title-text {
  margin-top: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.problem-meta {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  font-size: 12px;
  color: #606266;
}

.problem-time {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.report-panel {
  min-height: 520px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 6px;
}

.workflow-panel {
  margin-bottom: 16px;
}

.current-problem {
  padding: 14px;
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #fafafa;
}

.current-problem-title {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.current-problem-title span {
  font-size: 13px;
  color: #909399;
}

.current-problem-title h3 {
  margin: 4px 0 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.problem-desc-table {
  background: #ffffff;
}

.workflow-middle-layout {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 16px;
  margin-bottom: 16px;
  align-items: stretch;
}

.log-flow-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: 16px;
  align-items: stretch;
}

.workflow-section {
  padding: 14px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #ffffff;
}

.module-section,
.log-section,
.flow-section {
  min-height: 500px;
}

.log-section {
  display: flex;
  flex-direction: column;
}

.flow-section {
  display: flex;
  flex-direction: column;
}

.task-section {
  margin-bottom: 0;
}

.task-section-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 12px;
}

.sub-title {
  margin-bottom: 12px;
}

.sub-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.sub-title p {
  margin: 5px 0 0;
  font-size: 13px;
  color: #909399;
}

.module-list-vertical {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.module-item-vertical {
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #ffffff;
  cursor: pointer;
}

.module-item-vertical:hover,
.module-item-vertical.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.module-item-vertical.selected {
  box-shadow: inset 3px 0 0 #409eff;
}

.module-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.module-name-nowrap {
  display: inline-block;
  max-width: 100%;
  white-space: nowrap;
}

.module-desc {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.workflow-button-row {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.selected-module-tip {
  margin-top: 12px;
  padding: 8px 10px;
  font-size: 13px;
  color: #606266;
  background: #f5f7fa;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
}

.selected-module-tip strong {
  color: #409eff;
}

.log-scroll {
  flex: 1;
  min-height: 420px;
  max-height: 560px;
  overflow-y: auto;
  padding-right: 6px;
}

.dispatch-log-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dispatch-log-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 170px;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #ffffff;
}

.dispatch-log-card.card-processing {
  border-left: 4px solid #f56c6c;
}

.dispatch-log-card.card-submitted {
  border-left: 4px solid #e6a23c;
}

.dispatch-log-card.card-confirmed {
  border-left: 4px solid #67c23a;
}

.dispatch-log-main {
  min-width: 0;
}

.dispatch-log-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.dispatch-log-top > div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.log-time {
  font-size: 12px;
  color: #909399;
}

.dispatch-log-content {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.log-line {
  display: flex;
  gap: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
}

.log-label {
  flex: 0 0 64px;
  color: #909399;
}

.result-line span:last-child {
  color: #303133;
}

.process-state,
.module-process-text,
.confirm-state {
  font-weight: 600;
}

.state-processing {
  color: #f56c6c;
}

.state-finished {
  color: #67c23a;
}

.dispatch-log-side {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-left: 12px;
  border-left: 1px solid #ebeef5;
}

.side-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  text-align: center;
}

.confirm-wait {
  color: #909399;
}

.confirm-pending {
  color: #e6a23c;
}

.confirm-finished {
  color: #67c23a;
}

.confirm-time {
  font-size: 12px;
  color: #909399;
}

.vertical-steps {
  flex: 1;
  min-height: 360px;
  margin-top: 8px;
}

@media (max-width: 1200px) {
  .top-layout {
    grid-template-columns: 1fr;
  }

  .workflow-middle-layout,
  .log-flow-layout {
    grid-template-columns: 1fr;
  }

  .problem-list {
    max-height: 300px;
  }

  .log-scroll {
    max-height: 420px;
  }

  .vertical-steps {
    height: auto;
    min-height: 260px;
  }

  .dispatch-log-card {
    grid-template-columns: 1fr;
  }

  .dispatch-log-side {
    padding-left: 0;
    padding-top: 10px;
    border-left: none;
    border-top: 1px solid #ebeef5;
  }
}

@media (max-width: 780px) {
  .quality-center-page {
    padding: 8px;
  }

  .page-title,
  .panel-header,
  .current-problem-title,
  .task-section-header {
    flex-direction: column;
  }

  .page-stat {
    flex-direction: column;
  }

  .page-stat div {
    width: auto;
  }

  .workflow-button-row {
    flex-direction: column;
  }
}
</style>