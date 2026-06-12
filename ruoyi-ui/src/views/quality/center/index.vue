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
      <section class="panel history-panel">
        <div class="panel-header">
          <div>
            <h2>历史问题清单</h2>
            <p>点击问题查看对应工作流</p>
          </div>
          <el-tag size="small" type="info">{{ problemList.length }} 条</el-tag>
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
              <span>{{ item.level }}</span>
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
              <el-form-item label="问题类型" prop="type">
                <el-select v-model="problemForm.type" placeholder="请选择" style="width: 100%">
                  <el-option label="设计问题" value="设计问题" />
                  <el-option label="制造问题" value="制造问题" />
                  <el-option label="装配问题" value="装配问题" />
                  <el-option label="检测问题" value="检测问题" />
                  <el-option label="试验问题" value="试验问题" />
                  <el-option label="运行问题" value="运行问题" />
                  <el-option label="其他问题" value="其他问题" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="问题等级" prop="level">
                <el-select v-model="problemForm.level" placeholder="请选择" style="width: 100%">
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
              <el-form-item label="发生阶段" prop="stage">
                <el-select v-model="problemForm.stage" placeholder="请选择" style="width: 100%">
                  <el-option label="设计阶段" value="设计阶段" />
                  <el-option label="制造阶段" value="制造阶段" />
                  <el-option label="装配阶段" value="装配阶段" />
                  <el-option label="检测阶段" value="检测阶段" />
                  <el-option label="试验阶段" value="试验阶段" />
                  <el-option label="运行阶段" value="运行阶段" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="紧急程度">
                <el-select v-model="problemForm.urgency" placeholder="请选择" style="width: 100%">
                  <el-option label="普通" value="普通" />
                  <el-option label="优先" value="优先" />
                  <el-option label="紧急" value="紧急" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="发现时间">
                <el-date-picker
                  v-model="problemForm.discoverTime"
                  type="datetime"
                  placeholder="选择时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="产品型号">
                <el-input v-model="problemForm.productModel" placeholder="如 XX-001" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="产品批次">
                <el-input v-model="problemForm.batchNo" placeholder="如 B20260601" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="关联部件">
                <el-input v-model="problemForm.component" placeholder="如 节流阀" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="部件编码">
                <el-input v-model="problemForm.componentCode" placeholder="如 C010" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="产品编号">
                <el-input v-model="problemForm.productSn" placeholder="产品序列号" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="发现部门">
                <el-input v-model="problemForm.department" placeholder="请输入部门" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="填报人员">
                <el-input v-model="problemForm.reporter" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item label="联系电话">
                <el-input v-model="problemForm.phone" placeholder="选填" />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="建议模块">
                <el-select
                  v-model="problemForm.suggestModules"
                  multiple
                  collapse-tags
                  collapse-tags-tooltip
                  placeholder="可选择建议参与处理的模块"
                  style="width: 100%"
                >
                  <el-option
                    v-for="module in workModules"
                    :key="module.moduleCode"
                    :label="module.moduleName"
                    :value="module.moduleCode"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="问题描述" prop="description">
                <el-input
                  v-model="problemForm.description"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入问题现象、发生位置、时间、影响范围、初步判断等信息"
                />
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="初步处理建议">
                <el-input
                  v-model="problemForm.suggestion"
                  type="textarea"
                  :rows="3"
                  placeholder="选填，例如：建议优先由课题三进行故障诊断，再由课题五开展追溯分析"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="form-actions">
            <el-button type="primary" @click="createProblem">新建质量问题</el-button>
            <el-button @click="resetForm">重置</el-button>
          </div>
        </el-form>
      </section>
    </div>

    <!-- 下方工作流 -->
    <section class="panel workflow-panel">
      <div class="panel-header">
        <div>
          <h2>具体工作流</h2>
          <p>当前版本为超级管理员简化操作流程：填报问题、分派模块、模拟处理、管理确认、结束问题。</p>
        </div>

        <div v-if="currentProblem" class="workflow-actions">
          <el-button
            type="success"
            plain
            :disabled="currentProblem.status === 'FINISHED'"
            @click="finishProblem"
          >
            结束问题
          </el-button>
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
            <el-descriptions-item label="问题类型">{{ currentProblem.type }}</el-descriptions-item>
            <el-descriptions-item label="问题等级">{{ currentProblem.level }}</el-descriptions-item>
            <el-descriptions-item label="问题来源">{{ currentProblem.source || '-' }}</el-descriptions-item>
            <el-descriptions-item label="发生阶段">{{ currentProblem.stage || '-' }}</el-descriptions-item>
            <el-descriptions-item label="产品型号">{{ currentProblem.productModel || '-' }}</el-descriptions-item>
            <el-descriptions-item label="产品批次">{{ currentProblem.batchNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="关联部件">{{ currentProblem.component || '-' }}</el-descriptions-item>
            <el-descriptions-item label="部件编码">{{ currentProblem.componentCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="当前模块">{{ currentProblem.currentModule || '待分派' }}</el-descriptions-item>
            <el-descriptions-item label="发现部门">{{ currentProblem.department || '-' }}</el-descriptions-item>
            <el-descriptions-item label="填报人员">{{ currentProblem.reporter || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ currentProblem.createTime }}</el-descriptions-item>
            <el-descriptions-item label="问题描述" :span="4">
              {{ currentProblem.description || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="workflow-steps">
          <el-steps :active="workflowActiveStep" finish-status="success" align-center>
            <el-step title="问题填报" />
            <el-step title="模块分派" />
            <el-step title="模块处理" />
            <el-step title="管理确认" />
            <el-step title="问题结束" />
          </el-steps>
        </div>

        <div class="workflow-section">
          <div class="sub-title">
            <h3>选择工作模块</h3>
            <p>可选择课题一至课题五，也可添加自定义模块。</p>
          </div>

          <div class="module-toolbar">
            <el-input
              v-model="customModuleName"
              placeholder="自定义模块名称"
              style="width: 220px"
              clearable
            />
            <el-button @click="addCustomModule">添加模块</el-button>
          </div>

          <div class="module-list">
            <div
              v-for="module in workModules"
              :key="module.moduleCode"
              class="module-item"
              :class="{ selected: selectedModuleCodes.includes(module.moduleCode) }"
              @click="toggleModule(module.moduleCode)"
            >
              <div>{{ module.moduleName }}</div>
              <span>{{ module.moduleType === 'TOPIC' ? '课题模块' : '自定义模块' }}</span>
            </div>
          </div>

          <el-input
            v-model="dispatchOpinion"
            type="textarea"
            :rows="3"
            placeholder="请输入本轮分派说明"
          />

          <div class="workflow-button-row">
            <el-button
              type="primary"
              :disabled="currentProblem.status === 'FINISHED'"
              @click="dispatchModules"
            >
              分派到选中模块
            </el-button>
            <el-button @click="clearSelectedModules">取消选择</el-button>
          </div>
        </div>

        <div class="workflow-section">
          <div class="sub-title">
            <h3>模块处理任务记录</h3>
            <p>共 {{ currentProblem.tasks.length }} 条任务</p>
          </div>

          <el-table :data="currentProblem.tasks" border stripe>
            <el-table-column prop="moduleName" label="模块名称" width="140" />
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
                  模拟处理完成
                </el-button>

                <el-button
                  size="small"
                  type="success"
                  plain
                  :disabled="scope.row.taskStatus !== 'SUBMITTED'"
                  @click="confirmTask(scope.row)"
                >
                  管理确认
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="workflow-section">
          <div class="sub-title">
            <h3>流程日志</h3>
            <p>记录问题从填报到结束的全过程</p>
          </div>

          <el-timeline>
            <el-timeline-item
              v-for="log in currentProblem.logs"
              :key="log.logId"
              :timestamp="log.time"
              placement="top"
            >
              <div class="log-item">
                <div class="log-title">
                  <strong>{{ log.actionName }}</strong>
                  <el-tag size="small" effect="plain">{{ log.operator }}</el-tag>
                </div>
                <p>{{ log.content }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchKeyword = ref('')
const currentProblem = ref(null)
const selectedModuleCodes = ref([])
const customModuleName = ref('')
const dispatchOpinion = ref('')
const problemFormRef = ref(null)

const problemForm = reactive({
  title: '',
  type: '',
  level: '',
  source: '',
  stage: '',
  urgency: '',
  discoverTime: '',
  productModel: '',
  batchNo: '',
  component: '',
  componentCode: '',
  productSn: '',
  department: '',
  reporter: '',
  phone: '',
  suggestModules: [],
  description: '',
  suggestion: ''
})

const problemRules = {
  title: [{ required: true, message: '请输入问题标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择问题类型', trigger: 'change' }],
  level: [{ required: true, message: '请选择问题等级', trigger: 'change' }],
  source: [{ required: true, message: '请选择问题来源', trigger: 'change' }],
  stage: [{ required: true, message: '请选择发生阶段', trigger: 'change' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }]
}

const workModules = ref([
  { moduleCode: 'PROJECT_1', moduleName: '课题一', moduleType: 'TOPIC' },
  { moduleCode: 'PROJECT_2', moduleName: '课题二', moduleType: 'TOPIC' },
  { moduleCode: 'PROJECT_3', moduleName: '课题三', moduleType: 'TOPIC' },
  { moduleCode: 'PROJECT_4', moduleName: '课题四', moduleType: 'TOPIC' },
  { moduleCode: 'PROJECT_5', moduleName: '课题五', moduleType: 'TOPIC' }
])

const problemList = ref([
  {
    problemId: 1,
    problemCode: 'QF-00001',
    title: '液压系统压力异常',
    type: '运行问题',
    level: '严重',
    source: '试验反馈',
    stage: '试验阶段',
    urgency: '紧急',
    productModel: '液压系统样机',
    batchNo: 'B20260601',
    component: '节流阀',
    componentCode: 'C010',
    productSn: 'SN-0001',
    department: '试验部',
    reporter: '超级管理员',
    description: '试验过程中出现压力波动超限现象，需要进一步分析异常原因。',
    suggestion: '建议先由课题三进行故障诊断，再由课题五进行追溯分析。',
    status: 'PROCESSING',
    currentModule: '课题五',
    createTime: '2026-06-12 09:20',
    tasks: [
      {
        taskId: 1,
        moduleCode: 'PROJECT_5',
        moduleName: '课题五',
        taskStatus: 'PROCESSING',
        dispatchOpinion: '请结合知识图谱进行质量问题追溯分析。',
        processResult: '',
        createTime: '2026-06-12 09:25'
      }
    ],
    logs: [
      {
        logId: 1,
        actionName: '问题填报',
        operator: '超级管理员',
        content: '新建质量问题：液压系统压力异常。',
        time: '2026-06-12 09:20'
      },
      {
        logId: 2,
        actionName: '模块分派',
        operator: '超级管理员',
        content: '将问题分派至课题五。',
        time: '2026-06-12 09:25'
      }
    ]
  },
  {
    problemId: 2,
    problemCode: 'QF-00002',
    title: '作动筒响应迟滞',
    type: '检测问题',
    level: '重要',
    source: '检测发现',
    stage: '检测阶段',
    urgency: '优先',
    productModel: '起落架液压作动系统',
    batchNo: 'B20260602',
    component: '作动筒',
    componentCode: 'C006',
    productSn: 'SN-0002',
    department: '检测部',
    reporter: '超级管理员',
    description: '检测过程中发现作动筒响应存在明显迟滞，需要开展故障诊断分析。',
    suggestion: '',
    status: 'WAIT_CONFIRM',
    currentModule: '课题三',
    createTime: '2026-06-11 16:40',
    tasks: [
      {
        taskId: 2,
        moduleCode: 'PROJECT_3',
        moduleName: '课题三',
        taskStatus: 'SUBMITTED',
        dispatchOpinion: '请对作动筒响应迟滞进行故障模式诊断。',
        processResult: '初步判断为管路压力不足或密封件老化导致。',
        createTime: '2026-06-11 16:45'
      }
    ],
    logs: [
      {
        logId: 1,
        actionName: '问题填报',
        operator: '超级管理员',
        content: '新建质量问题：作动筒响应迟滞。',
        time: '2026-06-11 16:40'
      },
      {
        logId: 2,
        actionName: '模块处理完成',
        operator: '课题三',
        content: '课题三已提交故障诊断结果，等待管理中心确认。',
        time: '2026-06-11 17:30'
      }
    ]
  }
])

currentProblem.value = problemList.value[0]

const filteredProblemList = computed(() => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return problemList.value

  return problemList.value.filter((item) => {
    return item.problemCode.includes(keyword) || item.title.includes(keyword)
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

  if (status === 'CREATED' || status === 'DISPATCHING') return 1
  if (status === 'PROCESSING') return 2
  if (status === 'WAIT_CONFIRM') return 3
  if (status === 'FINISHED') return 5

  return 0
})

const selectProblem = (problem) => {
  currentProblem.value = problem
  selectedModuleCodes.value = []
  dispatchOpinion.value = ''
}

const resetForm = () => {
  Object.keys(problemForm).forEach((key) => {
    problemForm[key] = Array.isArray(problemForm[key]) ? [] : ''
  })
  problemFormRef.value?.clearValidate()
}

const createProblem = async () => {
  await problemFormRef.value.validate()

  const now = getNowTime()
  const problemId = Date.now()
  const problemCode = generateProblemCode()

  const newProblem = {
    problemId,
    problemCode,
    title: problemForm.title,
    type: problemForm.type,
    level: problemForm.level,
    source: problemForm.source,
    stage: problemForm.stage,
    urgency: problemForm.urgency,
    discoverTime: problemForm.discoverTime,
    productModel: problemForm.productModel,
    batchNo: problemForm.batchNo,
    component: problemForm.component,
    componentCode: problemForm.componentCode,
    productSn: problemForm.productSn,
    department: problemForm.department,
    reporter: problemForm.reporter,
    phone: problemForm.phone,
    suggestModules: [...problemForm.suggestModules],
    description: problemForm.description,
    suggestion: problemForm.suggestion,
    status: 'DISPATCHING',
    currentModule: '',
    createTime: now,
    tasks: [],
    logs: [
      {
        logId: Date.now(),
        actionName: '问题填报',
        operator: '超级管理员',
        content: `新建质量问题：${problemForm.title}。`,
        time: now
      }
    ]
  }

  problemList.value.unshift(newProblem)
  currentProblem.value = newProblem

  if (problemForm.suggestModules.length > 0) {
    selectedModuleCodes.value = [...problemForm.suggestModules]
  }

  resetForm()
  ElMessage.success('质量问题已新建，等待分派模块')
}

const toggleModule = (moduleCode) => {
  const index = selectedModuleCodes.value.indexOf(moduleCode)
  if (index > -1) {
    selectedModuleCodes.value.splice(index, 1)
  } else {
    selectedModuleCodes.value.push(moduleCode)
  }
}

const clearSelectedModules = () => {
  selectedModuleCodes.value = []
}

const addCustomModule = () => {
  const name = customModuleName.value.trim()
  if (!name) {
    ElMessage.warning('请输入自定义模块名称')
    return
  }

  const moduleCode = `CUSTOM_${Date.now()}`

  workModules.value.push({
    moduleCode,
    moduleName: name,
    moduleType: 'CUSTOM'
  })

  selectedModuleCodes.value.push(moduleCode)
  customModuleName.value = ''
  ElMessage.success('自定义模块已添加')
}

const dispatchModules = () => {
  if (!currentProblem.value) {
    ElMessage.warning('请先选择或新建质量问题')
    return
  }

  if (currentProblem.value.status === 'FINISHED') {
    ElMessage.warning('该问题已结束，不能继续分派')
    return
  }

  if (selectedModuleCodes.value.length === 0) {
    ElMessage.warning('请选择至少一个工作模块')
    return
  }

  const now = getNowTime()
  const modules = workModules.value.filter((item) => selectedModuleCodes.value.includes(item.moduleCode))

  modules.forEach((module) => {
    currentProblem.value.tasks.push({
      taskId: Date.now() + Math.floor(Math.random() * 1000),
      moduleCode: module.moduleCode,
      moduleName: module.moduleName,
      taskStatus: 'PROCESSING',
      dispatchOpinion: dispatchOpinion.value || `请${module.moduleName}处理该质量问题。`,
      processResult: '',
      createTime: now
    })
  })

  currentProblem.value.status = 'PROCESSING'
  currentProblem.value.currentModule = modules.map((item) => item.moduleName).join('、')

  currentProblem.value.logs.unshift({
    logId: Date.now(),
    actionName: '模块分派',
    operator: '超级管理员',
    content: `将问题分派至：${modules.map((item) => item.moduleName).join('、')}。`,
    time: now
  })

  selectedModuleCodes.value = []
  dispatchOpinion.value = ''
  ElMessage.success('已分派到选中模块')
}

const simulateTaskSubmit = async (task) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入模拟处理结果', '模块处理完成', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '例如：已完成分析，初步判断该问题与装配偏差或传感器异常有关。'
    })

    task.taskStatus = 'SUBMITTED'
    task.processResult = value || '模块已完成处理并提交结果。'
    currentProblem.value.status = 'WAIT_CONFIRM'

    currentProblem.value.logs.unshift({
      logId: Date.now(),
      actionName: '模块处理完成',
      operator: task.moduleName,
      content: `${task.moduleName}已提交处理结果：${task.processResult}`,
      time: getNowTime()
    })

    ElMessage.success('模块处理结果已提交，等待管理中心确认')
  } catch (error) {
    // 用户取消，不处理
  }
}

const confirmTask = (task) => {
  task.taskStatus = 'CONFIRMED'

  const hasUnconfirmedTask = currentProblem.value.tasks.some((item) => {
    return ['PROCESSING', 'SUBMITTED'].includes(item.taskStatus)
  })

  currentProblem.value.status = hasUnconfirmedTask ? 'WAIT_CONFIRM' : 'DISPATCHING'
  currentProblem.value.currentModule = hasUnconfirmedTask ? currentProblem.value.currentModule : ''

  currentProblem.value.logs.unshift({
    logId: Date.now(),
    actionName: '管理中心确认',
    operator: '超级管理员',
    content: `管理中心已确认${task.moduleName}的处理结果。`,
    time: getNowTime()
  })

  ElMessage.success('处理结果已确认，可继续分派其他模块或结束问题')
}

const finishProblem = async () => {
  if (!currentProblem.value) return

  const unconfirmedTasks = currentProblem.value.tasks.filter((item) => item.taskStatus !== 'CONFIRMED')

  if (unconfirmedTasks.length > 0) {
    ElMessage.warning('仍有未确认的模块任务，不能结束问题')
    return
  }

  try {
    await ElMessageBox.confirm('确认结束当前质量问题？结束后将不再继续分派模块。', '结束问题', {
      confirmButtonText: '确认结束',
      cancelButtonText: '取消',
      type: 'warning'
    })

    currentProblem.value.status = 'FINISHED'
    currentProblem.value.currentModule = ''

    currentProblem.value.logs.unshift({
      logId: Date.now(),
      actionName: '问题结束',
      operator: '超级管理员',
      content: '该质量问题已完成全部处理流程并结束。',
      time: getNowTime()
    })

    ElMessage.success('质量问题已结束')
  } catch (error) {
    // 用户取消，不处理
  }
}

const getProblemStatusText = (status) => {
  const map = {
    CREATED: '已填报',
    DISPATCHING: '待分派',
    PROCESSING: '模块处理中',
    WAIT_CONFIRM: '待确认',
    FINISHED: '已结束'
  }
  return map[status] || status
}

const getProblemStatusType = (status) => {
  const map = {
    CREATED: 'info',
    DISPATCHING: 'warning',
    PROCESSING: 'primary',
    WAIT_CONFIRM: 'danger',
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

const generateProblemCode = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const suffix = String(problemList.value.length + 1).padStart(3, '0')
  return `QF-${y}${m}${d}-${suffix}`
}

const getNowTime = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const h = String(now.getHours()).padStart(2, '0')
  const min = String(now.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}`
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

.workflow-actions {
  display: flex;
  gap: 8px;
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

.workflow-steps {
  padding: 18px 10px;
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #ffffff;
}

.workflow-section {
  padding: 14px;
  margin-bottom: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #ffffff;
}

.workflow-section:last-child {
  margin-bottom: 0;
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

.module-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.module-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.module-item {
  width: 116px;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #ffffff;
  cursor: pointer;
}

.module-item:hover,
.module-item.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.module-item div {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.module-item span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.workflow-button-row {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.log-item {
  padding: 10px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #ffffff;
}

.log-title {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.log-title strong {
  color: #303133;
}

.log-item p {
  margin: 8px 0 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

@media (max-width: 1200px) {
  .top-layout {
    grid-template-columns: 1fr;
  }

  .problem-list {
    max-height: 300px;
  }
}

@media (max-width: 780px) {
  .quality-center-page {
    padding: 8px;
  }

  .page-title,
  .panel-header,
  .current-problem-title {
    flex-direction: column;
  }

  .page-stat {
    flex-direction: column;
  }

  .page-stat div {
    width: auto;
  }

  .module-toolbar,
  .workflow-button-row {
    flex-direction: column;
  }
}
</style>